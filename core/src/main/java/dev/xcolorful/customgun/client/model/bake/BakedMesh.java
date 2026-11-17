package dev.xcolorful.customgun.client.model.bake;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.*;
import dev.xcolorful.customgun.client.input.player.DebugKey;
import dev.xcolorful.customgun.client.util.ClientRenderProfiler;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 模型空间的静态烘焙网格（渲染重构B1-a）
 * <ul>
 *     <li>顶点只生成一次，保留一份交错顶点副本；每帧回放进原版 {@link VertexConsumer}，不再逐节点遍历与计算姿态</li>
 *     <li>实例矩阵不进顶点：回放时用调用方的 {@code PoseStack.Pose} 变换位置与法线，同一份网格可被多个展示框共享</li>
 *     <li>光照需要原地改写（渲染重构B1-c-B）：副本里的 uv2 直接改写，不需要重新发射几何</li>
 *     <li>26.2 的几何只能在 {@code submitCustomGeometry} 的回调里写顶点，自有 {@code GpuBuffer} 无法插进原版批次；
 *         而把烘焙块拆成独立 feature type 会打乱与其它自定义几何的提交顺序（瞄具模板序列依赖严格顺序），
 *         自定义管线也让光影包找不到 program。因此 26.2 起统一走回放，代价是每帧一次顶点拷贝</li>
 * </ul>
 * 旧版本（[1.20.1, 26.2)）的 GPU 常驻网格实现见本文件的 git 历史；本分支只保留回放。
 * 失效条件由调用方放进 {@link StaticBakeCache.Key}，本类不判断
 */
@ApiStatus.Internal
public final class BakedMesh implements AutoCloseable {

    /**
     * 单次烘焙的初始顶点缓冲容量；超出时 {@link BufferBuilder} 自行扩容
     */
    private static final int BUILD_CAPACITY = 1024 * 1024;

    /**
     * 复用的 {@link BufferBuilder}，按烘焙嵌套深度取用
     * <ul>
     *     <li>{@code BufferBuilder} 在构造时经 {@code MemoryTracker} 直接申请原生内存（{@code 容量 × 6}）</li>
     *     <li>而释放路径只重置计数、不归还内存：每次烘焙新建一个就是一次几 MiB 的永久泄漏</li>
     *     <li>{@link DebugKey}采集会每帧强制重建网格，几秒内就能把页面文件耗尽（表现是原生内存不足导致的 JVM 崩溃）</li>
     * </ul>
     */
    private static final List<ByteBufferBuilder> BUILDERS = new ArrayList<>();
    private static int builderDepth;

    /**
     * {@code LightTexture.pack(15, 15)}：发光节点（{@code illuminated}）烘进顶点的固定满亮。
     * 打包格式随版本变化，走项目包装而不是写死数值
     */
    private static final int FULL_BRIGHT = ClientRenderUtils.LightTexture_.pack(15, 15);

    /**
     * 交错顶点字节的 CPU 副本：回放与光照改写的唯一数据来源
     * <ul>
     *     <li>{@code null} 只出现在空网格</li>
     *     <li>光照恰为满亮时 uv2 无法区分发光节点与普通顶点，{@code uv2PatchOffset} 置 {@code -1}，不做原地改写</li>
     * </ul>
     */
    private final @Nullable ByteBuffer vertexCopy;
    private final int vertexCount;
    private final int stride;
    /**
     * uv2 在单个顶点里的字节偏移；格式不含 uv2 时为 {@code -1}
     */
    private final int uv2Offset;
    /**
     * 光照原地改写用的 uv2 偏移：满亮网格无法区分发光节点，置 {@code -1} 表示不改写
     */
    private final int uv2PatchOffset;
    private final int bytes;

    /**
     * 回放用的各属性字节偏移；格式里没有的属性为 {@code -1}
     */
    private final int positionOffset;
    private final int colorOffset;
    private final int uv0Offset;
    private final int uv1Offset;
    private final int normalOffset;
    private final int lineWidthOffset;

    /**
     * 网格当前烘着的 packed 光照
     */
    private int light;
    private boolean closed;

    private BakedMesh(@Nullable ByteBuffer vertexCopy, VertexFormat format,
                      int vertexCount, int stride, int uv2PatchOffset, int bytes, int light) {
        this.vertexCopy = vertexCopy;
        this.vertexCount = vertexCount;
        this.stride = stride;
        this.uv2PatchOffset = uv2PatchOffset;
        this.bytes = bytes;
        this.light = light;

        this.positionOffset = elementOffset(format, "Position");
        this.colorOffset = elementOffset(format, "Color");
        this.uv0Offset = elementOffset(format, "UV0");
        this.uv1Offset = elementOffset(format, "UV1");
        this.uv2Offset = elementOffset(format, "UV2");
        this.normalOffset = elementOffset(format, "Normal");
        this.lineWidthOffset = elementOffset(format, "LineWidth");
    }

    @ApiStatus.AvailableSince("26.2")
    private static int elementOffset(VertexFormat format, String name) {
        @Nullable VertexFormatElement element = format.getElement(name);
        return element == null ? -1 : element.offset();
    }

    /**
     * 光照进缓存键的场景（整模静态）
     * <li>光照变化直接换网格</li>
     * @param light   烘进顶点的 packed 光照
     * @param emitter 在模型空间写顶点；写 0 个顶点时得到空网格，绘制为空操作
     */
    public static BakedMesh bake(PrimitiveTopology mode,
                                 VertexFormat format, int light,
                                 Consumer<VertexConsumer> emitter) {
        return bake26_2(mode, format, light, emitter);
    }

    /**
     * 单实例路径（第一人称分块 / 瞄具子网格）：光照不进缓存键，光照变化时改写副本里的 uv2
     */
    public static BakedMesh bakePatchable(PrimitiveTopology mode,
                                          VertexFormat format, int light,
                                          Consumer<VertexConsumer> emitter) {
        return bake26_2(mode, format, light, emitter);
    }

    /**
     * 旧签名（[1.20.1, 26.2) 区分是否保留 CPU 副本），26.2 起统一由 {@link #bake26_2} 处理
     */
    @Deprecated(since = "26.2")
    private static BakedMesh bake(PrimitiveTopology mode,
                                  VertexFormat format, int light,
                                  boolean keepLightCopy,
                                  Consumer<VertexConsumer> emitter) {
        return null;
    }

    @ApiStatus.AvailableSince("26.2")
    private static BakedMesh bake26_2(PrimitiveTopology mode,
                                      VertexFormat format, int light,
                                      Consumer<VertexConsumer> emitter) {
        BufferBuilder builder = acquireBuilder(mode, format);
        long profileEmit = ClientRenderProfiler.begin();
        try {
            emitter.accept(builder);
            MeshData rendered = builder.build();
            ClientRenderProfiler.end(ClientRenderProfiler.BAKE_EMIT, profileEmit);
            if (rendered == null) {
                return new BakedMesh(null, format, 0, 0, -1, 0, light);
            }

            MeshData.DrawState drawState = rendered.drawState();
            VertexFormat writtenFormat = drawState.format();
            int vertexCount = drawState.vertexCount();
            int stride = writtenFormat.getVertexSize();
            int bytes = vertexCount * stride;
            int uv2Offset = light == FULL_BRIGHT ? -1 : elementOffset(writtenFormat, "UV2");
            // 副本必须在 close 之前拷出来：MeshData 关闭会归还 ByteBufferBuilder 的区段
            @Nullable ByteBuffer vertexCopy = copyVertices(rendered.vertexBuffer());
            rendered.close();
            ClientRenderProfiler.countBakeBytes(bytes);

            return new BakedMesh(vertexCopy, writtenFormat, vertexCount, stride, uv2Offset, bytes, light);
        } finally {
            builderDepth--;
        }
    }

    private static ByteBuffer copyVertices(ByteBuffer source) {
        // 与 BufferBuilder 保持同一字节序：回放按同样的序读写 short / float
        ByteBuffer copy = ByteBuffer.allocateDirect(source.remaining()).order(source.order());
        copy.put(source);
        copy.position(0);
        return copy;
    }

    /**
     * 旧签名的 uv2 偏移查询；26.2 起用 {@link #elementOffset}
     */
    @Deprecated(since = "26.2")
    private static int uv2Offset(VertexFormat format) {
        return -1;
    }

    /**
     * 把网格的 uv2 改写成新的光照（只改 CPU 副本，回放时自然生效）
     * <ul>
     *     <li>不重新发射顶点，也不新建缓冲</li>
     *     <li>发光节点烘的是满亮 15/15，不跟着世界光照变化；只有等于旧光照的顶点才会被改写</li>
     * </ul>
     * @return {@code false} 表示这份网格无法原地改写，调用方应关闭它并重新烘焙
     */
    public boolean updateLight(int newLight) {
        if (this.closed) return false;
        if (this.light == newLight) return true;

        @Nullable ByteBuffer copy = this.vertexCopy;
        if (copy == null) {
            this.light = newLight;
            return true;
        }
        if (this.uv2PatchOffset < 0) return false;

        long profileLight = ClientRenderProfiler.begin();
        short oldU = (short) (this.light & 0xFFFF);
        short oldV = (short) (this.light >>> 16);
        short newU = (short) (newLight & 0xFFFF);
        short newV = (short) (newLight >>> 16);

        for (int i = 0; i < this.vertexCount; i++) {
            int offset = i * this.stride + this.uv2PatchOffset;
            if (copy.getShort(offset) == oldU && copy.getShort(offset + 2) == oldV) {
                copy.putShort(offset, newU);
                copy.putShort(offset + 2, newV);
            }
        }
        ClientRenderProfiler.end(ClientRenderProfiler.LIGHT_UPDATE, profileLight);
        this.light = newLight;

        ClientRenderProfiler.countLightPatch(copy.capacity());
        return true;
    }

    /**
     * 旧签名（[1.20.1, 26.2) 重传原 VBO）；26.2 起光照只改 CPU 副本，回放时自然生效
     */
    @Deprecated(since = "26.2")
    private boolean uploadCopy() {
        return false;
    }

    private static BufferBuilder acquireBuilder(PrimitiveTopology mode,
                                                VertexFormat format) {
        int depth = builderDepth++;
        if (depth == BUILDERS.size()) BUILDERS.add(
                new ByteBufferBuilder(BUILD_CAPACITY)
        );
        return new BufferBuilder(BUILDERS.get(depth), mode, format);
    }

    /**
     * 旧签名（[1.20.1, 26.2) 用自有 GpuBuffer 绘制）；26.2 起改用 {@code StaticBake#drawMesh26_2}
     */
    @Deprecated(since = "26.2")
    public boolean draw(RenderType renderType, Matrix4f pose) {
        return false;
    }

    @Deprecated(since = "26.2")
    @ApiStatus.AvailableSince("1.21.6")
    private static RenderTarget outputTarget(RenderType renderType) {
        return null;
    }

    public int bytes() {
        return this.bytes;
    }

    /**
     * 旧签名（[1.20.1, 26.2) 比对顶点格式是否被光影包扩展）；26.2 起回放按属性名写入，不再需要
     */
    @Deprecated(since = "1.21.6")
    public boolean extendedFormat() {
        return false;
    }

    /**
     * 烘焙时的顶点步长
     */
    @ApiStatus.AvailableSince("1.21.6")
    public int stride() {
        return this.stride;
    }

    @Override
    public void close() {
        this.closed = true;
    }

    /**
     * 已被缓存淘汰：调用方应重新烘焙，而不是继续复用
     */
    public boolean isClosed() {
        return this.closed;
    }

    /**
     * 把烘焙好的顶点回放进调用方的 {@link VertexConsumer}
     * <ul>
     *     <li>位置与法线经 {@code pose} 变换，与即时渲染同一口径（法线因此不需要着色器现算）</li>
     *     <li>属性按名字回放：光影包可能扩展顶点格式，回放只写它认识的语义</li>
     *     <li><b>不检查 {@link #closed}</b>：几何在 submit 阶段提交、draw 阶段才回放，缓存淘汰可能在这之间关闭条目，
     *         此时仍应画完这一帧，否则整片几何静默消失</li>
     * </ul>
     * @return {@code false} 表示网格为空
     */
    @ApiStatus.AvailableSince("26.2")
    public boolean replay(PoseStack.Pose pose,
                          VertexConsumer consumer) {
        @Nullable ByteBuffer copy = this.vertexCopy;
        if (copy == null) return true;

        for (int i = 0; i < this.vertexCount; i++) {
            int base = i * this.stride;
            consumer.addVertex(pose,
                    copy.getFloat(base + this.positionOffset),
                    copy.getFloat(base + this.positionOffset + 4),
                    copy.getFloat(base + this.positionOffset + 8));
            if (this.colorOffset >= 0) {
                // 交错缓冲里的字节序就是 RGBA
                consumer.setColor(copy.get(base + this.colorOffset) & 0xFF,
                        copy.get(base + this.colorOffset + 1) & 0xFF,
                        copy.get(base + this.colorOffset + 2) & 0xFF,
                        copy.get(base + this.colorOffset + 3) & 0xFF);
            }
            if (this.uv0Offset >= 0) {
                consumer.setUv(copy.getFloat(base + this.uv0Offset),
                        copy.getFloat(base + this.uv0Offset + 4));
            }
            if (this.uv1Offset >= 0) {
                consumer.setUv1(copy.getShort(base + this.uv1Offset) & 0xFFFF,
                        copy.getShort(base + this.uv1Offset + 2) & 0xFFFF);
            }
            if (this.uv2Offset >= 0) {
                consumer.setUv2(copy.getShort(base + this.uv2Offset) & 0xFFFF,
                        copy.getShort(base + this.uv2Offset + 2) & 0xFFFF);
            }
            if (this.normalOffset >= 0) {
                consumer.setNormal(pose,
                        copy.get(base + this.normalOffset) / 127.0F,
                        copy.get(base + this.normalOffset + 1) / 127.0F,
                        copy.get(base + this.normalOffset + 2) / 127.0F);
            }
            if (this.lineWidthOffset >= 0) {
                consumer.setLineWidth(copy.getFloat(base + this.lineWidthOffset));
            }
        }
        return true;
    }
}
