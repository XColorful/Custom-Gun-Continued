package dev.xcolorful.customgun.client.model.bedrock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockCube;
import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockCubeCompile;
import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockRenderer;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.client.util.ClientRenderProfiler;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.util.MathUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.Random;

public class BedrockPart implements IBedrockCubeCompile, IBedrockRenderer {

    public final @Nullable String name;
    public final ObjectList<IBedrockCube> cubes = new ObjectArrayList<>();
    public final ObjectList<BedrockPart> children = new ObjectArrayList<>();

    public BedrockPart(@Nullable String name) {
        this.name = name;
    }

    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public boolean visible = true;
    public boolean illuminated = false;
    public boolean mirror;
    /**
     * 通常用于动画旋转
     */
    public Quaternionf additionalQuaternion = new Quaternionf(0, 0, 0, 1);
    public float xScale = 1;
    public float yScale = 1;
    public float zScale = 1;

    protected BedrockPart parent;

    /**
     * {@link #containsFunctional()} 的缓存；节点树构建完成后不再变化
     */
    private @Nullable Boolean cgc$subtreeFunctional;

    /**
     * 渲染探针用：上一次采样到的节点状态指纹与所在帧序号
     * <li>同一节点一帧内可能被多次提交（多个展示框 / 光影阴影 pass），按帧去重</li>
     * <li>只有即时管线会经过，静态烘焙路径不计入「脏节点」——那里本来就应当是 0</li>
     */
    private long cgc$profileHash = Long.MIN_VALUE;
    private long cgc$profileStamp = Long.MIN_VALUE;
    /** 本帧已把「以本节点为根的子树」计入脏子树，用于避免祖先/后代重复累加 */
    private long cgc$coveredStamp = Long.MIN_VALUE;
    /** 子树节点数（含自身），节点树构建完成后缓存 */
    private int cgc$subtreeSize = -1;

    private float initRotX;
    private float initRotY;
    private float initRotZ;

    public void translate_rotate_scale(PoseStack poseStack) {
        poseStack.translate(this.offsetX, this.offsetY, this.offsetZ);
        poseStack.translate((this.x / 16.0F), (this.y / 16.0F), (this.z / 16.0F));
        if (this.zRot != 0.0F) {
            ClientRenderHelper.rotate(poseStack, Axis.ZP.rotation(this.zRot));
        }
        if (this.yRot != 0.0F) {
            ClientRenderHelper.rotate(poseStack, Axis.YP.rotation(this.yRot));
        }
        if (this.xRot != 0.0F) {
            ClientRenderHelper.rotate(poseStack, Axis.XP.rotation(this.xRot));
        }
        ClientRenderHelper.rotate(poseStack, additionalQuaternion);
        poseStack.scale(xScale, yScale, zScale);
    }

    // --------Getter--------

    public boolean isEmpty() {
        return this.cubes.isEmpty();
    }

    public BedrockPart getParent() {
        return this.parent;
    }

    // --------Setter--------

    public void setPos(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public void setInitRotationAngle(float x, float y, float z) {
        this.initRotX = x;
        this.initRotY = y;
        this.initRotZ = z;
    }

    @ApiStatus.Internal
    public void setParent(BedrockPart parent) {
        this.parent = parent;
    }

    public void addChild(BedrockPart child) {
        this.children.add(child);
        // 节点树在加载期构建，子树节点数要一并失效；运行时增删子节点时同样走这里
        for (BedrockPart part = this; part != null; part = part.parent) {
            part.cgc$subtreeSize = -1;
        }
    }

    // --------IBedrockCubeCompile--------

    @Override
    public void compile(PoseStack.Pose pose,
                        VertexConsumer consumer,
                        int light, int overlay,
                        float red, float green, float blue, float alpha) {
        for (int i = 0; i < this.cubes.size(); i++) {
            ClientRenderProfiler.countCube();
            this.cubes.get(i)
                    .compile(pose,
                            consumer,
                            light, overlay,
                            red, green, blue, alpha);
        }
    }

    // --------静态烘焙--------

    /**
     * 状态段：执行 functional 回调、确定可见性，不写顶点
     * <ul>
     *     <li>返回值是本子树的节点状态指纹，作为烘焙缓存的失效依据</li>
     *     <li>隐藏的子树不参与渲染，因此也不计入指纹</li>
     *     <li>它们重新可见时父节点的 {@code visible} 会变化，指纹随之改变</li>
     * </ul>
     * 子树内没有 functional 节点时不需要回调，也就不需要维护 {@link PoseStack}，只做指纹累加
     */
    public long renderStatus(PoseStack poseStack,
                             ItemDisplayContext transformType,
                             int light, int overlay,
                             VertexConsumer discard) {
        if (!this.visible) return 0L;
        if (!this.containsFunctional()) return this.subtreeStateHash();

        long hash = this.stateHash();
        int cubePackedLight = this.illuminated ? ClientRenderUtils.LightTexture_.pack(15, 15) : light;

        poseStack.pushPose(); {
            this.translate_rotate_scale(poseStack);
            for (int i = 0; i < this.children.size(); i++) {
                hash = hash * 31L + this.children.get(i)
                        .renderStatus(poseStack, transformType, cubePackedLight, overlay, discard);
            }
        }
        poseStack.popPose();
        return hash;
    }

    /**
     * 子树内是否存在 functional 节点
     * <ul>
     *     <li>节点树在 {@code resetCache} 期间构建完成后不再改变，结果缓存到首次查询</li>
     *     <li>{@link FunctionalBedrockPart} 恒为 {@code true}</li>
     * </ul>
     */
    public boolean containsFunctional() {
        if (this.cgc$subtreeFunctional == null) {
            boolean found = false;
            for (int i = 0; i < this.children.size(); i++) {
                if (this.children.get(i).containsFunctional()) {
                    found = true;
                    break;
                }
            }
            this.cgc$subtreeFunctional = found;
        }
        return this.cgc$subtreeFunctional;
    }

    /**
     * 运行时改动 functional 回调后，清掉自身与祖先的 {@link #containsFunctional()} 缓存
     * <ul>
     *     <li>该缓存按「加载完成后不再变化」优化；addon 在首次渲染之后再挂回调时必须调用本方法</li>
     *     <li>否则状态段会走快速指纹路径、回调永远不执行</li>
     * </ul>
     */
    public void invalidateFunctionalCache() {
        BedrockPart part = this;
        while (part != null) {
            part.cgc$subtreeFunctional = null;
            part = part.parent;
        }
    }

    /**
     * 不维护姿态、只累加可见节点指纹
     */
    public long subtreeStateHash() {
        if (!this.visible) return 0L;
        long hash = this.stateHash();
        for (int i = 0; i < this.children.size(); i++) {
            hash = hash * 31L + this.children.get(i).subtreeStateHash();
        }
        return hash;
    }

    /**
     * 几何段：只按当前可见性发射顶点，不执行 functional 回调
     * <ul>
     *     <li>functional 回调由状态段执行过，回调的副作用（可见性、委托）已经生效</li>
     *     <li>因此这里可以安全地跳过回调，并使用一个独立的单位 {@link PoseStack} 烘焙出模型空间网格</li>
     * </ul>
     */
    public void renderGeometry(PoseStack poseStack,
                               ItemDisplayContext transformType,
                               VertexConsumer consumer,
                               int light, int overlay,
                               float red, float green, float blue, float alpha) {
        if (!this.visible) return;
        if (this.cubes.isEmpty() && this.children.isEmpty()) return;

        int cubePackedLight = this.illuminated ? ClientRenderUtils.LightTexture_.pack(15, 15) : light;

        poseStack.pushPose(); {
            this.translate_rotate_scale(poseStack);
            this.compile(poseStack.last(), consumer, cubePackedLight, overlay, red, green, blue, alpha);
            for (int i = 0; i < this.children.size(); i++) {
                this.children.get(i)
                        .renderGeometry(poseStack, transformType, consumer,
                                cubePackedLight, overlay, red, green, blue, alpha);
            }
        }
        poseStack.popPose();
    }

    /**
     * 64 位黄金比例常量，来源于 Fibonacci Hashing / Knuth's multiplicative hashing
     * <li>{@code floor(2^64 * (φ - 1))}，其中 {@code φ = (1 + √5) / 2}</li>
     */
    private static final long STATE_HASH_SEED = MathUtil.GOLDEN_RATIO_64;
    /**
     * 把本节点的全部变换字段累加进指纹
     * <ul>
     *     <li>动画只写 offset / additionalQuaternion / scale / visible，其余字段在加载后不再变化</li>
     *     <li>一并计入可以让 addon 直接改写节点姿态时也能正确失效</li>
     * </ul>
     */
    @SuppressWarnings("NumericOverflow")
    public long stateHash() {
        long hash = STATE_HASH_SEED * 31L + Float.floatToRawIntBits(this.x);
        hash = hash * 31L + Float.floatToRawIntBits(this.y);
        hash = hash * 31L + Float.floatToRawIntBits(this.z);
        hash = hash * 31L + Float.floatToRawIntBits(this.xRot);
        hash = hash * 31L + Float.floatToRawIntBits(this.yRot);
        hash = hash * 31L + Float.floatToRawIntBits(this.zRot);
        hash = hash * 31L + Float.floatToRawIntBits(this.offsetX);
        hash = hash * 31L + Float.floatToRawIntBits(this.offsetY);
        hash = hash * 31L + Float.floatToRawIntBits(this.offsetZ);
        hash = hash * 31L + Float.floatToRawIntBits(this.additionalQuaternion.x);
        hash = hash * 31L + Float.floatToRawIntBits(this.additionalQuaternion.y);
        hash = hash * 31L + Float.floatToRawIntBits(this.additionalQuaternion.z);
        hash = hash * 31L + Float.floatToRawIntBits(this.additionalQuaternion.w);
        hash = hash * 31L + Float.floatToRawIntBits(this.xScale);
        hash = hash * 31L + Float.floatToRawIntBits(this.yScale);
        hash = hash * 31L + Float.floatToRawIntBits(this.zScale);
        hash = hash * 31L + (this.visible ? 1L : 2L);
        return hash;
    }

    /**
     * 渲染探针：统计本节点是否「本帧第一次访问」，以及状态是否与上一帧不同
     * <ul>
     *     <li>只在采集窗口内由 {@link #render} 调用；未采集时调用方只付一次 volatile 读</li>
     *     <li>状态取 {@link #stateHash()}，与烘焙缓存的失效口径一致</li>
     *     <li>另外累加「脏子树」：以脏节点为根的子树节点数。根节点自身不算——B1-b 把根变换提升为
     *         绘制矩阵，改根不需要重烘焙；其余节点一旦变换，整棵子树在模型空间的顶点都要重写</li>
     * </ul>
     */
    public void profileNodeState() {
        long stamp = ClientRenderProfiler.frameStamp();
        if (this.cgc$profileStamp == stamp) return;
        this.cgc$profileStamp = stamp;
        ClientRenderProfiler.countDistinctNode();
        long hash = this.stateHash();
        if (hash == this.cgc$profileHash) return;

        this.cgc$profileHash = hash;
        ClientRenderProfiler.countDirtyNode();

        // 根节点变换由绘制矩阵承担，不产生重烘焙；但它也不能挡住后代的计数
        if (this.parent == null) return;
        for (BedrockPart part = this.parent; part != null; part = part.parent) {
            if (part.cgc$coveredStamp == stamp) return; // 祖先的子树已计入，避免重复
        }
        this.cgc$coveredStamp = stamp;
        ClientRenderProfiler.countDirtySubtree(this.subtreeSize());
    }

    /** 子树节点数（含自身）；节点树构建完成后缓存 */
    public int subtreeSize() {
        if (this.cgc$subtreeSize < 0) {
            int size = 1;
            for (int i = 0; i < this.children.size(); i++) {
                size += this.children.get(i).subtreeSize();
            }
            this.cgc$subtreeSize = size;
        }
        return this.cgc$subtreeSize;
    }

    // --------IBedrockRenderer--------

    @Override
    public BedrockPart getModelRenderer() {
        return this;
    }

    // ----Getter & Setter----

    @Override public float getRotateAngleX() {
        return this.xRot;
    }
    @Override public float getRotateAngleY() {
        return this.yRot;
    }
    @Override public float getRotateAngleZ() {
        return this.zRot;
    }
    @Override public float getOffsetX() {
        return this.offsetX;
    }
    @Override public float getOffsetY() {
        return this.offsetY;
    }
    @Override public float getOffsetZ() {
        return this.offsetZ;
    }
    @Override public float getRotationPointX() {
        return this.x;
    }
    @Override public float getRotationPointY() {
        return this.y;
    }
    @Override public float getRotationPointZ() {
        return this.z;
    }
    @Override public boolean isVisible() {
        return this.visible;
    }
    @Override public float getInitRotateAngleX() {
        return this.initRotX;
    }
    @Override public float getInitRotateAngleY() {
        return this.initRotY;
    }
    @Override public float getInitRotateAngleZ() {
        return this.initRotZ;
    }
    @Override public Quaternionf getAdditionalQuaternion() {
        return this.additionalQuaternion;
    }
    @Override public float getScaleX() {
        return this.xScale;
    }
    @Override public float getScaleY() {
        return this.yScale;
    }
    @Override public float getScaleZ() {
        return this.zScale;
    }

    @Override public void setRotateAngleX(float xRot) {
        this.xRot = xRot;
    }
    @Override public void setRotateAngleY(float yRot) {
        this.yRot = yRot;
    }
    @Override public void setRotateAngleZ(float zRot) {
        this.zRot = zRot;
    }
    @Override public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }
    @Override public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }
    @Override public void setOffsetZ(float offsetZ) {
        this.offsetZ = offsetZ;
    }
    @Override public void setVisible(boolean visible) {
        this.visible = visible;
    }
    @Override public void setAdditionalQuaternion(Quaternionf quaternion) {
        this.additionalQuaternion = quaternion;
    }
    @Override public void setScaleX(float scaleX) {
        this.xScale = scaleX;
    }
    @Override public void setScaleY(float scaleY) {
        this.yScale = scaleY;
    }
    @Override public void setScaleZ(float scaleZ) {
        this.zScale = scaleZ;
    }

    // --------IBedrockRender--------

    @Override
    public void render(PoseStack poseStack,
                       ItemDisplayContext transformType,
                       VertexConsumer consumer,
                       int light, int overlay,
                       float red, float green, float blue, float alpha) {
        if (ClientRenderProfiler.enabled) this.profileNodeState();

        int cubePackedLight = this.illuminated ? ClientRenderUtils.LightTexture_.pack(15, 15) // 最大亮度
                : light;

        if (this.visible) {
            if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
                poseStack.pushPose(); {
                    this.translate_rotate_scale(poseStack);
                    this.compile(poseStack.last(), consumer, cubePackedLight, overlay, red, green, blue, alpha);

                    for (int i = 0; i < this.children.size(); i++) {
                        this.children.get(i)
                                .render(poseStack,
                                        transformType,
                                        consumer,
                                        cubePackedLight, overlay,
                                        red, green, blue, alpha);
                    }
                }
                poseStack.popPose();
            }
        }
    }

    // --------Deprecated--------

    /**
     * 疑似废弃功能
     */
    @Deprecated(forRemoval = false) public IBedrockCube getRandomCube(Random random) {
        return this.cubes.get(random.nextInt(this.cubes.size()));
    }

    @Deprecated public float getInitRotX() {
        return this.getInitRotateAngleX();
    }
    @Deprecated public float getInitRotY() {
        return this.getInitRotateAngleY();
    }
    @Deprecated public float getInitRotZ() {
        return this.getInitRotateAngleZ();
    }
    @Deprecated(forRemoval = true) public void translateAndRotateAndScale(PoseStack poseStack) {
        this.translate_rotate_scale(poseStack);
    }
}
