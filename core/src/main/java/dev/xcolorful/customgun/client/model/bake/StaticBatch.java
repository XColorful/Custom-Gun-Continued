package dev.xcolorful.customgun.client.model.bake;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.compat.iris.IrisCompat;
import dev.xcolorful.customgun.client.util.ClientRenderProfiler;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 静态展示实例的跨实例合批（渲染重构B2）
 * <ul>
 *     <li>物品展示框里的枪体与配件世界矩阵固定，可以按 RenderType 合并成一次 draw</li>
 *     <li>世界矩阵从实例姿态里剥掉相机：{@code W = T(camPos) * R_cam^-1 * pose}；顶点按批次锚点存成相对坐标，
 *         镜头移动不影响网格，因此同一批只在实例集合或内容变化时重建</li>
 *     <li>每个实例的顶点区间单独记录、光照按实例存放：一次光照变化只改写该实例的 uv2 并重传这一段</li>
 *     <li>只用于 {@link ItemDisplayContext#FIXED} 且没有光影包；掉落物 / 第三人称的世界矩阵每帧都变，仍走
 *         {@link StaticBake#drawOrBake}</li>
 * </ul>
 * 帧边界由 {@code LevelRendererMixin} 调用 {@link #beginFrame()} / {@link #flush()}；实例身份取自
 * {@link #pushEntity} 登记的当前实体
 */
@ApiStatus.Internal
public final class StaticBatch {

    private StaticBatch() {}

    /**
     * 合批总开关；与 {@link StaticBake#ENABLED} 同时生效
     */
    public static boolean ENABLED = true;

    private static final int MAX_BLOCKS = 128;
    private static final long MAX_BYTES = 32L << 20;
    /**
     * 同一实例连续重建超过这个次数就不再合批：第三方把 FIXED 用在动画上时世界矩阵每帧都变，退回逐实例绘制
     */
    private static final int MAX_REBAKES = 4;

    private static final Map<InstanceKey, Instance> INSTANCES = new HashMap<>();
    /**
     * 姿态 / 内容每帧都变的实例：暂缓合批，避免「重建 5 次 → 退回 → 再重建」地来回摇摆
     * <li>记录拉黑时的帧号，过一段时间再给一次机会：世界刚加载时姿态抖动也会连续重建几次</li>
     */
    private static final Map<InstanceKey, Integer> UNBATCHED = new HashMap<>();
    private static final int UNBATCHED_RETRY = 1200;
    private static final Map<RenderType, Group> GROUPS = new IdentityHashMap<>();
    private static long totalBytes;
    private static int frame;

    /**
     * 换维度后实体 id 会指向另一个实体
     */
    private static @Nullable Object levelIdentity;

    /**
     * 顶点格式随光影状态变化（光影包会扩展成 {@code IrisVertexFormats.ENTITY}），切光影时整批作废
     */
    private static boolean shaderPack;

    private static @Nullable BufferBuilder builder;

    private static final Deque<Entity> ENTITY_STACK = new ArrayDeque<>();

    // --------帧边界--------

    /**
     * 由 {@code LevelRendererMixin} 在 {@code LevelRenderer#renderLevel} 开头调用
     */
    public static void beginFrame() {
        frame++;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != levelIdentity) {
            levelIdentity = mc.level;
            clearAll();
        }
        boolean pack = IrisCompat.isUsingRenderPack();
        if (pack != shaderPack) {
            shaderPack = pack;
            clearAll();
        }
    }

    /**
     * 登记正在渲染的实体：{@code renderByItem} 自己拿不到实例身份
     */
    public static void pushEntity(Entity entity) {
        ENTITY_STACK.push(entity);
    }

    public static void popEntity() {
        if (!ENTITY_STACK.isEmpty()) ENTITY_STACK.pop();
    }

    /**
     * 是否走合批
     * <ul>
     *     <li>只有展示框这种「世界矩阵固定 + 没有光影包」的提交能合批</li>
     *     <li>光影包下影子 pass 会再渲染一遍实体，帧末 flush 拿不到影子 pass 自己的相机矩阵</li>
     * </ul>
     */
    public static boolean batching(ItemDisplayContext context) {
        boolean pack = IrisCompat.isUsingRenderPack();
        boolean result = ENABLED
                && StaticBake.eligible()
                && !StaticBake.FORCE_REBUILD
                && context == ItemDisplayContext.FIXED
                && !ENTITY_STACK.isEmpty()
                && Minecraft.getInstance().level != null
                && !pack;

        // 首次遇到展示实例时把判定依据打进日志：合批不生效时先看这一行
        if (context == ItemDisplayContext.FIXED) {
            if (!diagnosed) {
                diagnosed = true;
                CustomGun.LOGGER.info("[B2] FIXED submit: batching={} enabled={} bakeEnabled={} forceRebuild={}"
                                + " entityStackEmpty={} levelNull={} shaderPack={}",
                        result, ENABLED, StaticBake.eligible(), StaticBake.FORCE_REBUILD,
                        ENTITY_STACK.isEmpty(), Minecraft.getInstance().level == null, pack);
            }
            if (result) ClientRenderProfiler.countBatchCandidate();
        }
        return result;
    }

    private static boolean diagnosed;
    private static boolean diagnosedSubmit;
    private static boolean diagnosedEmit;
    private static boolean diagnosedUnbatched;
    private static boolean diagnosedSuccess;

    /**
     * 登记一个静态实例；几何只在实例首次出现或内容变化时发射
     * @return {@code false} 表示这个实例不能合批，调用方应退回 {@link StaticBake#drawOrBake}
     */
    public static boolean submit(@Nullable Object owner,
                                 @Nullable Object subKey,
                                 Matrix4f pose,
                                 RenderType renderType,
                                 int light, int overlay,
                                 float red, float green, float blue, float alpha,
                                 long fingerprint,
                                 Consumer<VertexConsumer> emitter) {
        boolean available = StaticBake.available();
        boolean quads = renderType.mode() == VertexFormat.Mode.QUADS;
        boolean newEntity = DefaultVertexFormat.NEW_ENTITY.equals(renderType.format());
        Entity entity = ENTITY_STACK.peek();

        if (!diagnosedSubmit) {
            diagnosedSubmit = true;
            CustomGun.LOGGER.info("[B2] submit probe: available={} mode={} quads={} format={} newEntity={} formatSize={} expectedSize={} entity={}",
                    available, renderType.mode(), quads, renderType.format(), newEntity,
                    renderType.format().getVertexSize(), DefaultVertexFormat.NEW_ENTITY.getVertexSize(), entity);
        }
        if (!available || !quads || !newEntity || entity == null) {
            ClientRenderProfiler.countBatchGuardReject();
            if (!available) ClientRenderProfiler.countBatchNoShader();
            return false;
        }

        Matrix4f world = worldMatrix(pose);
        BlockKey key = new BlockKey(fingerprint, overlay, red, green, blue, alpha);
        InstanceKey instanceKey = new InstanceKey(entity.getId(), renderType, owner, subKey);
        @Nullable Integer blockedAt = UNBATCHED.get(instanceKey);
        if (blockedAt != null) {
            if (frame - blockedAt < UNBATCHED_RETRY) return false;
            UNBATCHED.remove(instanceKey);
        }
        Instance instance = INSTANCES.get(instanceKey);

        if (instance != null && instance.block != null && instance.block.key.equals(key)
                && sameMatrix(instance.block.world, world)) {
            Block block = instance.block;
            block.lastSeen = frame;
            instance.rebakes = 0;
            if (block.light != light) patchLight(block, light);
            ClientRenderProfiler.countBatchSubmit();
            return true;
        }

        if (instance != null && ++instance.rebakes > MAX_REBAKES) {
            // 内容 / 姿态每帧都在变：放弃合批，交还调用方逐实例绘制
            ClientRenderProfiler.countBatchUnbatched();
            if (!diagnosedUnbatched) {
                diagnosedUnbatched = true;
                CustomGun.LOGGER.info("[B2] instance unbatched after {} rebuilds: owner={} subKey={}",
                        instance.rebakes, owner, subKey);
            }
            drop(instanceKey, instance);
            UNBATCHED.put(instanceKey, frame);
            return false;
        }

        Group group = GROUPS.get(renderType);
        if (group == null) {
            group = new Group(renderType, new Vector3d(
                    Math.floor(world.m30()), Math.floor(world.m31()), Math.floor(world.m32())));
            GROUPS.put(renderType, group);
        }

        Matrix4f bakeMatrix = new Matrix4f(world);
        bakeMatrix.m30((float) (world.m30() - group.anchor.x));
        bakeMatrix.m31((float) (world.m31() - group.anchor.y));
        bakeMatrix.m32((float) (world.m32() - group.anchor.z));

        @Nullable BakedMesh.EmittedGeometry emitted = BakedMesh.emitGeometry(
                renderType.mode(), renderType.format(), light, emitter);
        if (emitted == null || emitted.stride() != DefaultVertexFormat.NEW_ENTITY.getVertexSize()) {
            ClientRenderProfiler.countBatchEmitReject();
            if (!diagnosedEmit) {
                diagnosedEmit = true;
                CustomGun.LOGGER.info("[B2] emit rejected: null={} stride={} vertices={}",
                        emitted == null, emitted == null ? -1 : emitted.stride(),
                        emitted == null ? -1 : emitted.vertexCount());
            }
            // 没有顶点 / 没有光影包时不应该出现别的格式：放弃合批，交还调用方逐实例绘制
            if (instance != null) drop(instanceKey, instance);
            return false;
        }

        if (instance == null) {
            instance = new Instance();
            INSTANCES.put(instanceKey, instance);
        } else if (instance.block != null) {
            ClientRenderProfiler.countBatchRebake();
            removeBlock(instance.block);
        }

        ByteBuffer bytes = transform(emitted, bakeMatrix);
        Block block = new Block(key, entity.getId(), group, bytes,
                emitted.vertexCount(), emitted.stride(), emitted.uv2Offset(), light,
                entity instanceof ItemFrame itemFrame ? itemFrame.getItem().getItem() : null, world);
        block.lastSeen = frame;
        instance.block = block;
        group.blocks.add(block);
        group.dirty = true;
        totalBytes += bytes.capacity();
        evict();
        if (!diagnosedSuccess) {
            diagnosedSuccess = true;
            CustomGun.LOGGER.info("[B2] batch block baked: stride={} vertices={} uv2Offset={} anchor={}",
                    emitted.stride(), emitted.vertexCount(), emitted.uv2Offset(), group.anchor);
        }
        ClientRenderProfiler.countBatchSubmit();
        return true;
    }

    private static void drop(InstanceKey key, Instance instance) {
        if (instance.block != null) removeBlock(instance.block);
        INSTANCES.remove(key);
    }

    private static void removeBlock(Block block) {
        block.group.blocks.remove(block);
        block.group.dirty = true;
        totalBytes -= block.byteSize();
    }

    // --------帧末：重建 / 绘制--------

    /**
     * 由 {@code LevelRendererMixin} 在实体循环之后调用
     */
    public static void flush() {
        if (INSTANCES.isEmpty() && GROUPS.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || IrisCompat.isUsingRenderPack()) {
            clearAll();
            return;
        }

        Vec3 camPos = ClientRenderUtils.getMainCamera(mc).getPosition();
        prune(mc, camPos);

        for (Group group : GROUPS.values()) {
            if (group.dirty) compose(group);
        }
        for (Group group : GROUPS.values()) {
            draw(group, camPos);
        }

        Iterator<Map.Entry<RenderType, Group>> iterator = GROUPS.entrySet().iterator();
        while (iterator.hasNext()) {
            Group group = iterator.next().getValue();
            if (!group.blocks.isEmpty()) continue;
            group.close();
            iterator.remove();
        }
    }

    /**
     * 不在本帧出现的实例分两种：退出可见范围（实体还在，继续画，避免镜头平移时反复重建）与实体已消失
     */
    private static void prune(Minecraft mc, Vec3 camPos) {
        Iterator<Map.Entry<InstanceKey, Instance>> iterator = INSTANCES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<InstanceKey, Instance> entry = iterator.next();
            Block block = entry.getValue().block;
            if (block == null || block.lastSeen == frame) continue;

            Entity entity = mc.level.getEntity(block.entityId);
            if (entity != null && inRange(entity, camPos)
                    && !(entity instanceof ItemFrame itemFrame && block.item != null
                            && itemFrame.getItem().getItem() != block.item)) {
                continue;
            }
            iterator.remove();
            removeBlock(block);
        }
    }

    private static boolean inRange(Entity entity, Vec3 camPos) {
        double dx = entity.getX() - camPos.x;
        double dy = entity.getY() - camPos.y;
        double dz = entity.getZ() - camPos.z;
        return entity.shouldRenderAtSqrDistance(dx * dx + dy * dy + dz * dz);
    }

    /**
     * 把当前所有实例的顶点拼进该 RenderType 的缓冲；实例顺序取首次出现的顺序
     */
    private static void compose(Group group) {
        int total = 0;
        for (int i = 0; i < group.blocks.size(); i++) {
            total += group.blocks.get(i).byteSize();
        }
        group.dirty = false;
        if (total == 0) {
            group.combined = null;
            return;
        }

        long profile = ClientRenderProfiler.begin();
        ByteBuffer combined = ByteBuffer.allocateDirect(total).order(ByteOrder.nativeOrder());
        int offset = 0;
        for (int i = 0; i < group.blocks.size(); i++) {
            Block block = group.blocks.get(i);
            block.byteOffset = offset;
            ByteBuffer copy = block.bytes.duplicate();
            copy.position(0);
            combined.put(copy);
            offset += block.byteSize();
        }
        combined.flip();
        group.combined = combined;

        if (group.vertexBuffer == null) {
            group.vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        }
        group.vertexBuffer.bind();
        try {
            BufferBuilder builder = builder();
            builder.begin(group.renderType.mode(), group.renderType.format());
            builder.putBulkData(combined.duplicate()); // TODO 这是Forge注入的，1.20.2neo有，26.3neo没有
            group.vertexBuffer.upload(builder.end());
        } finally {
            VertexBuffer.unbind();
            ClientRenderProfiler.end(ClientRenderProfiler.BATCH_BUILD, profile);
        }
        ClientRenderProfiler.countBatchRebuild();
    }

    private static void draw(Group group, Vec3 camPos) {
        if (group.vertexBuffer == null || group.combined == null) return;

        boolean setup = false;
        try {
            group.renderType.setupRenderState();
            setup = true;

            @Nullable ShaderInstance shader = StaticBake.resolveEntityShader();
            if (shader == null) return;

            long profile = ClientRenderProfiler.begin();
            group.vertexBuffer.bind();
            group.vertexBuffer.drawWithShader(viewMatrix(group.anchor, camPos),
                    RenderSystem.getProjectionMatrix(), shader);
            ClientRenderProfiler.end(ClientRenderProfiler.DRAW, profile);

            ClientRenderProfiler.countRenderType(group.renderType);
            ClientRenderProfiler.countDraw();
            ClientRenderProfiler.countBatchDraw();
        } finally {
            VertexBuffer.unbind();
            if (setup) group.renderType.clearRenderState();
        }
    }

    // --------光照--------

    /**
     * 只改写该实例自己的 uv2 区间并重传这一段，不重建整批
     */
    private static void patchLight(Block block, int newLight) {
        int oldLight = block.light;
        block.light = newLight;
        if (block.uv2Offset < 0) return;

        long profile = ClientRenderProfiler.begin();
        short oldU = (short) (oldLight & 0xFFFF);
        short oldV = (short) (oldLight >>> 16);
        short newU = (short) (newLight & 0xFFFF);
        short newV = (short) (newLight >>> 16);
        int size = block.byteSize();
        patch(block.bytes, block.vertexCount, block.stride, block.uv2Offset, oldU, oldV, newU, newV);

        Group group = block.group;
        if (!group.dirty && group.combined != null && group.vertexBuffer != null) {
            int offset = block.byteOffset;
            ByteBuffer slice = group.combined.duplicate();
            slice.position(offset);
            slice.limit(offset + size);
            ByteBuffer view = slice.slice();
            // slice() 的字节序固定是 BIG_ENDIAN，顶点数据是本地序，必须显式改回来
            view.order(block.bytes.order());
            patch(view, block.vertexCount, block.stride, block.uv2Offset, oldU, oldV, newU, newV);
            uploadRange(group, offset, view);
        }
        ClientRenderProfiler.end(ClientRenderProfiler.LIGHT_UPDATE, profile);
        ClientRenderProfiler.countLightPatch(size);
    }

    private static void patch(ByteBuffer buffer, int vertexCount, int stride, int uv2Offset,
                              short oldU, short oldV, short newU, short newV) {
        for (int i = 0; i < vertexCount; i++) {
            int offset = i * stride + uv2Offset;
            if (buffer.getShort(offset) == oldU && buffer.getShort(offset + 2) == oldV) {
                buffer.putShort(offset, newU);
                buffer.putShort(offset + 2, newV);
            }
        }
    }

    private static void uploadRange(Group group, int offset, ByteBuffer data) {
        group.vertexBuffer.bind();
        try {
            int vbo = GL20.glGetVertexAttribi(0, GL20.GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING);
            if (vbo <= 0) return;
            GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, data);
        } finally {
            VertexBuffer.unbind();
        }
    }

    // --------坐标与顶点--------

    /**
     * 从「世界 → 视图」的实例姿态里剥掉相机，得到世界矩阵
     * <ul>
     *     <li>视图矩阵 {@code V = R_cam * T(-camPos)}，{@code W = V^-1 * pose}</li>
     *     <li>平移用 double 组装：{@code pose} 的平移已经是相机相对量（精度好），相机坐标本身不进 float</li>
     * </ul>
     */
    private static Matrix4f worldMatrix(Matrix4f pose) {
        Matrix3f inverseRotation = new Matrix3f(RenderSystem.getInverseViewRotationMatrix());
        Matrix3f linear = new Matrix3f(inverseRotation).mul(new Matrix3f(pose));
        Vector3f translation = inverseRotation.transform(new Vector3f(pose.m30(), pose.m31(), pose.m32()));

        Vec3 camPos = cameraPosition();
        Matrix4f world = new Matrix4f().set(linear);
        world.setTranslation(
                (float) (camPos.x + translation.x),
                (float) (camPos.y + translation.y),
                (float) (camPos.z + translation.z));
        return world;
    }

    /**
     * 批次锚点坐标 → 视图空间的矩阵
     */
    private static Matrix4f viewMatrix(Vector3d anchor, Vec3 camPos) {
        Matrix3f inverseRotation = new Matrix3f(RenderSystem.getInverseViewRotationMatrix());
        Matrix3f rotation = new Matrix3f(inverseRotation).invert();
        Vector3f offset = new Vector3f(
                (float) (anchor.x - camPos.x),
                (float) (anchor.y - camPos.y),
                (float) (anchor.z - camPos.z));
        rotation.transform(offset);

        Matrix4f view = new Matrix4f(rotation);
        view.setTranslation(offset);
        return new Matrix4f(RenderSystem.getModelViewMatrix()).mul(view);
    }

    private static Vec3 cameraPosition() {
        Camera camera = ClientRenderUtils.getMainCamera(Minecraft.getInstance());
        return camera.getPosition();
    }

    /**
     * 两次计算出的世界矩阵是否足够接近
     * <ul>
     *     <li>相机矩阵乘回去会引入约 1e-7（旋转）与 1e-4（平移）的浮点噪声，直接按哈希比较会在低位抖动</li>
     *     <li>展示框的姿态变化是整格 / 90° 级，1e-4 / 1e-3 的容差足够区分</li>
     * </ul>
     */
    private static boolean sameMatrix(Matrix4f a, Matrix4f b) {
        float eps = 1.0E-4F;
        if (Math.abs(a.m00() - b.m00()) > eps || Math.abs(a.m01() - b.m01()) > eps || Math.abs(a.m02() - b.m02()) > eps
                || Math.abs(a.m10() - b.m10()) > eps || Math.abs(a.m11() - b.m11()) > eps || Math.abs(a.m12() - b.m12()) > eps
                || Math.abs(a.m20() - b.m20()) > eps || Math.abs(a.m21() - b.m21()) > eps || Math.abs(a.m22() - b.m22()) > eps) {
            return false;
        }
        float translationEps = 1.0E-3F;
        return Math.abs(a.m30() - b.m30()) <= translationEps
                && Math.abs(a.m31() - b.m31()) <= translationEps
                && Math.abs(a.m32() - b.m32()) <= translationEps;
    }

    /**
     * 把模型空间顶点搬到批次锚点坐标系：位置用完整矩阵，法线用逆转置
     */
    private static ByteBuffer transform(BakedMesh.EmittedGeometry emitted, Matrix4f matrix) {
        ByteBuffer source = emitted.bytes();
        ByteBuffer target = ByteBuffer.allocateDirect(source.capacity()).order(source.order());
        target.put(source.duplicate());
        target.position(0);

        int positionOffset = elementOffset(DefaultVertexFormat.NEW_ENTITY, VertexFormatElement.Usage.POSITION);
        int normalOffset = elementOffset(DefaultVertexFormat.NEW_ENTITY, VertexFormatElement.Usage.NORMAL);
        Matrix3f normalMatrix = new Matrix3f(matrix).invert().transpose();

        Vector3f position = new Vector3f();
        Vector3f normal = new Vector3f();
        for (int i = 0; i < emitted.vertexCount(); i++) {
            int offset = i * emitted.stride();
            position.set(target.getFloat(offset + positionOffset),
                    target.getFloat(offset + positionOffset + 4),
                    target.getFloat(offset + positionOffset + 8));
            matrix.transformPosition(position);
            target.putFloat(offset + positionOffset, position.x);
            target.putFloat(offset + positionOffset + 4, position.y);
            target.putFloat(offset + positionOffset + 8, position.z);

            if (normalOffset < 0) continue;
            normal.set(target.get(offset + normalOffset) / 127.0F,
                    target.get(offset + normalOffset + 1) / 127.0F,
                    target.get(offset + normalOffset + 2) / 127.0F);
            normalMatrix.transform(normal);
            if (normal.lengthSquared() > 0.0F) normal.normalize();
            target.put(offset + normalOffset, (byte) Math.round(clamp(normal.x) * 127.0F));
            target.put(offset + normalOffset + 1, (byte) Math.round(clamp(normal.y) * 127.0F));
            target.put(offset + normalOffset + 2, (byte) Math.round(clamp(normal.z) * 127.0F));
        }
        return target;
    }

    private static float clamp(float value) {
        return Math.max(-1.0F, Math.min(1.0F, value));
    }

    /**
     * @return 元素在顶点里的字节偏移；没有该元素时返回 {@code -1}
     */
    private static int elementOffset(VertexFormat format, VertexFormatElement.Usage usage) {
        int offset = 0;
        for (VertexFormatElement element : format.getElements()) {
            if (element.getUsage() == usage) return offset;
            offset += element.getByteSize();
        }
        return -1;
    }

    private static BufferBuilder builder() {
        if (builder == null) builder = new BufferBuilder(1 << 20);
        return builder;
    }

    // --------生命周期--------

    private static void evict() {
        while ((INSTANCES.size() > MAX_BLOCKS || totalBytes > MAX_BYTES) && INSTANCES.size() > 1) {
            InstanceKey eldestKey = null;
            Block eldest = null;
            for (Map.Entry<InstanceKey, Instance> entry : INSTANCES.entrySet()) {
                Block block = entry.getValue().block;
                if (block == null) continue;
                if (eldest == null || block.lastSeen < eldest.lastSeen) {
                    eldest = block;
                    eldestKey = entry.getKey();
                }
            }
            if (eldest == null || eldestKey == null) return;
            INSTANCES.remove(eldestKey);
            removeBlock(eldest);
        }
    }
    /**
     * 资源重载 / 换维度：旧模型对象会被整体替换，世界空间缓冲必须一起释放
     */
    public static void clearAll() {
        for (Group group : GROUPS.values()) {
            group.close();
        }
        GROUPS.clear();
        INSTANCES.clear();
        UNBATCHED.clear();
        totalBytes = 0;
    }

    // --------数据结构--------

    /**
     * 实例身份：同一实体身上的同一子网格；内容或姿态变化时只替换 block，不换实例
     */
    private record InstanceKey(int entityId,
                               RenderType renderType,
                               @Nullable Object owner,
                               @Nullable Object subKey) {}

    /**
     * 内容键：指纹 / 覆盖层 / 顶点颜色任一变化都要重发顶点；姿态单独用 {@link #sameMatrix} 比较
     */
    private record BlockKey(long fingerprint,
                            int overlay,
                            float red, float green, float blue, float alpha) {}

    private static final class Instance {
        @Nullable Block block;
        int rebakes;
    }

    private static final class Block {

        final BlockKey key;
        final int entityId;
        final Group group;
        /** 锚点坐标系下的交错顶点 */
        final ByteBuffer bytes;
        final int vertexCount;
        final int stride;
        /** uv2 字节偏移；{@code -1} 表示烘焙时光照为满亮，不能原地改写 */
        final int uv2Offset;
        /**
         * 展示框里的物品：内容被换掉时不会再提交，只能靠它识别并丢弃残留批次
         */
        final @Nullable Item item;
        /**
         * 烘焙时的世界矩阵；下一帧用容差比较，避免相机矩阵乘回去产生的低位抖动被判成姿态变化
         */
        final Matrix4f world;

        int light;
        int lastSeen;
        int byteOffset;

        Block(BlockKey key, int entityId, Group group, ByteBuffer bytes,
              int vertexCount, int stride, int uv2Offset, int light, @Nullable Item item, Matrix4f world) {
            this.key = key;
            this.entityId = entityId;
            this.group = group;
            this.bytes = bytes;
            this.vertexCount = vertexCount;
            this.stride = stride;
            this.uv2Offset = uv2Offset;
            this.light = light;
            this.item = item;
            this.world = new Matrix4f(world);
        }

        int byteSize() {
            return this.bytes.capacity();
        }
    }

    private static final class Group {

        final RenderType renderType;
        final Vector3d anchor;
        final List<Block> blocks = new ArrayList<>();

        @Nullable VertexBuffer vertexBuffer;
        @Nullable ByteBuffer combined;
        boolean dirty = true;

        Group(RenderType renderType, Vector3d anchor) {
            this.renderType = renderType;
            this.anchor = anchor;
        }

        void close() {
            for (int i = 0; i < this.blocks.size(); i++) {
                totalBytes -= this.blocks.get(i).byteSize();
            }
            this.blocks.clear();
            this.combined = null;
            if (this.vertexBuffer == null) return;
            VertexBuffer buffer = this.vertexBuffer;
            this.vertexBuffer = null;
            if (RenderSystem.isOnRenderThread()) {
                buffer.close();
            } else {
                RenderSystem.recordRenderCall(buffer::close);
            }
        }
    }
}
