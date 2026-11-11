package dev.xcolorful.customgun.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.api.model.IModelObjectRender;
import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockRenderer;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.compat.iris.IrisCompat;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.model.bedrock.NodeTransform;
import dev.xcolorful.customgun.client.model.bake.DiscardVertexConsumer;
import dev.xcolorful.customgun.client.model.bake.StaticBake;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry._Bone;
import dev.xcolorful.customgun.client.util.ClientModelUtils;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.client.util.ClientRenderProfiler;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry.NodeName;
import dev.xcolorful.customgun.core.resource.instance.PojoInstance;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 对应原模组{@code com.tacz.guns.client.model.bedrock.BedrockModel}
 */
public class ModelObject extends PojoInstance<BedrockModel> implements IModelObjectRender {

    /**
     * 存储 ModelRender 子模型的 HashMap
     * <br>
     * private只是为了集中看方法引用，如{@link #modelMap_put}
     */
    private final HashMap<String, IBedrockRenderer> modelMap = new HashMap<>();
    /**
     * 存储 Bones 的 HashMap，主要是给后面寻找父骨骼进行坐标转换用的
     */
    protected final HashMap<String, _Bone> indexBones = new HashMap<>();
    /**
     * 哪些模型需要渲染。加载进父骨骼的子骨骼是不需要渲染的
     */
    protected final List<BedrockPart> shouldRender = new LinkedList<>();

    /**
     * 委托到渲染结束时执行的渲染器，用于特殊部分的渲染，如手臂
     */
    protected ArrayList<IModelComponentRenderer> delegateRenderers = new ArrayList<>();

    /**
     * 模型的中心点
     */
    protected @Nullable Vec3 offset = null;
    protected boolean hasSize = false;
    protected float width = 0;
    protected float height = 0;

    /**
     * 渲染探针用：上次提交时节点状态的指纹，仅采集窗口内更新。
     * 不参与渲染，仅用于统计「本次提交的几何是否真的变了」。
     */
    private long cgc$lastStateHash = Long.MIN_VALUE;

    protected
    ModelObject(@NotNull BedrockModel pojo) {
        super(pojo);
    }

    public static @Nullable ModelObject fromPojo(BedrockModel pojo) {
        if (pojo == null) return null;
        ModelObject instance = new ModelObject(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        { // 加载模型
            BedrockModel pojo = this.getPojo();
            String formatVersion = pojo.getFormatVersion();
            if (true) {
                this.loadNewModel(pojo);
            } else {
                this.loadLegacyModel(pojo);
            }
        }

        { // 应用发光
            for (IBedrockRenderer iRenderer : this.modelMap_values()) {
                var renderer = iRenderer.getModelRenderer();
                if (NodeName.Suffix.ILLUMINATE.matches(renderer.name)) {
                    renderer.illuminated = true;
                }
            }
        }

        return true;
    }

    @Override protected boolean isPojoValid() {
        if (!super.isPojoValid()) return false;

        return true;
    }

    protected void loadNewModel(BedrockModel pojo) {
        _ModelLoader.loadNewModel(this, pojo);
    }
    protected void loadLegacyModel(BedrockModel pojo) {
        _ModelLoader.loadLegacyModel(this, pojo);
    }

    protected @Nullable List<BedrockPart> getPath(@Nullable IBedrockRenderer renderer) {
        if (renderer == null) return null;

        BedrockPart part = renderer.getModelRenderer();
        List<BedrockPart> path = new ArrayList<>();
        Deque<BedrockPart> parents = new ArrayDeque<>();
        do {
            parents.push(part);
            part = part.getParent();
        } while (part != null);

        while (!parents.isEmpty()) {
            path.add(parents.pop());
        }
        return path;
    }

    // --------Getter--------

    public boolean hasSize() {
        return this.hasSize;
    }
    public float getWidth() {
        return this.width;
    }
    public float getHeight() {
        return this.height;
    }
    public BedrockPart getNode(String nodeName) {
        @Nullable IBedrockRenderer renderer = this.modelMap.get(nodeName);
        return renderer != null ? renderer.getModelRenderer() : null;
    }
    public _Bone getBone(String name) {
        return this.indexBones.get(name);
    }
    public List<BedrockPart> getShouldRender() {
        return this.shouldRender;
    }
    public HashMap<String, _Bone> getIndexBones() {
        return this.indexBones;
    }

    @ApiStatus.Internal protected @Nullable IBedrockRenderer modelMap_get(String nodeName) {
        return this.modelMap.get(nodeName);
    }
    @ApiStatus.Internal protected Collection<IBedrockRenderer> modelMap_values() {
        return this.modelMap.values();
    }
    @ApiStatus.Internal protected Set<Map.Entry<String, IBedrockRenderer>> modelMap_entrySet() {
        return this.modelMap.entrySet();
    }

    // --------Setter--------

    public void delegateRender(IModelComponentRenderer renderer) {
        delegateRenderers.add(renderer);
    }

    @ApiStatus.Internal
    protected void setRotationAngle(BedrockPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
        modelRenderer.setInitRotationAngle(x, y, z);
    }

    @ApiStatus.Internal protected IBedrockRenderer modelMap_put(String key, IBedrockRenderer renderer) {
        return this.modelMap.put(key, renderer);
    }
    @ApiStatus.Internal protected IBedrockRenderer modelMap_putIfAbsent(String key, IBedrockRenderer renderer) {
        return this.modelMap.putIfAbsent(key, renderer);
    }

    // --------IRenderObject--------

    public void render(PoseStack matrixStack,
                       ItemDisplayContext transformType,
                       RenderType renderType,
                       int light, int overlay,
                       float red, float green, float blue, float alpha) {
        @Nullable Object collector = ClientRenderHelper.FirstPersonArmHelper.getFirstPersonArmCollector();
        if (collector == null) return;

        final long profModel = ClientRenderProfiler.begin();

        MultiBufferSource.BufferSource bufferSource = ClientRenderHelper.GL.getModelBufferSource();
        VertexConsumer builder = bufferSource.getBuffer(renderType);
        ClientRenderProfiler.countBatch();
        ClientRenderProfiler.countRenderType(renderType);

        if (ClientRenderProfiler.enabled) { // 采集窗口内统计：本次提交的节点状态是否与上次不同
            long hash = 1125899906842597L;
            for (IBedrockRenderer renderer : this.modelMap_values()) {
                BedrockPart part = renderer.getModelRenderer();
                ClientRenderProfiler.countNodes(1);
                hash = hash * 31L + Float.floatToRawIntBits(part.offsetX);
                hash = hash * 31L + Float.floatToRawIntBits(part.offsetY);
                hash = hash * 31L + Float.floatToRawIntBits(part.offsetZ);
                hash = hash * 31L + Float.floatToRawIntBits(part.additionalQuaternion.x);
                hash = hash * 31L + Float.floatToRawIntBits(part.additionalQuaternion.y);
                hash = hash * 31L + Float.floatToRawIntBits(part.additionalQuaternion.z);
                hash = hash * 31L + Float.floatToRawIntBits(part.additionalQuaternion.w);
                hash = hash * 31L + Float.floatToRawIntBits(part.xScale);
                hash = hash * 31L + Float.floatToRawIntBits(part.yScale);
                hash = hash * 31L + Float.floatToRawIntBits(part.zScale);
                hash = hash * 31L + (part.visible ? 1L : 2L);
            }
            if (hash != this.cgc$lastStateHash) {
                this.cgc$lastStateHash = hash;
                ClientRenderProfiler.countStateChange();
            }
        }

        /*
        [1.20.1, 26.2)
        即时模式下顶点在本次调用内就被提交，节点状态不会被清空，因此不需要快照与还原
        这段双重快照在 1.20.1 上纯属开销（multi 场景实测 0.135 ms/帧），B1-a 中移除
        26.2 延迟渲染下仍需在 flush 前保存/恢复节点状态，移植时按下面的注释恢复
         */
        // [26.2, )
        /*
        List<BedrockPart> parts = new ArrayList<>(); {
            for (IBedrockRenderer renderer : this.modelMap_values()) {
                parts.add(renderer.getModelRenderer());
            }
        }
        ClientRenderProfiler.countNodes(parts.size());

        List<NodeTransform> animatedTransforms = new ArrayList<>(parts.size()); {
            for (BedrockPart part : parts) {
                animatedTransforms.add(NodeTransform.capture(part));
            }
        }

        List<NodeTransform> cleanedTransforms = new ArrayList<>(parts.size()); {
            for (int i = 0; i < parts.size(); i++) {
                BedrockPart part = parts.get(i);
                cleanedTransforms.add(NodeTransform.capture(part));
            }
            for (int i = 0; i < animatedTransforms.size(); i++) {
                NodeTransform transform = animatedTransforms.get(i);
                transform.apply();
            }
        }
        */

        RenderType bakedRenderType = ClientRenderHelper.bakePipelineState(renderType); // [26.2, )

        // [26.2, ) 延迟渲染下这里需要在 flush 前保存、flush 后还原节点状态

        // B1-a：节点静态的场景走烘焙网格；无法烘焙时退回即时管线
        boolean baked = StaticBake.eligible(transformType)
                && this.renderBaked(matrixStack, transformType, bakedRenderType, light, overlay,
                        red, green, blue, alpha);
        if (!baked) {
            final long profTraverse = ClientRenderProfiler.begin();
            matrixStack.pushPose(); {
                for (int i = 0; i < this.shouldRender.size(); i++) {
                    this.shouldRender.get(i)
                            .render(matrixStack,
                                    transformType,
                                    builder,
                                    light, overlay,
                                    red, green, blue, alpha);
                }
            }
            matrixStack.popPose();
            ClientRenderProfiler.end(ClientRenderProfiler.TRAVERSE, profTraverse);

            final long profFlush = ClientRenderProfiler.begin();
            ClientRenderProfiler.countFlush();
            if (!IrisCompat.endBatch(bufferSource)) {
                bufferSource.endBatch();
            }
            ClientRenderProfiler.end(ClientRenderProfiler.FLUSH, profFlush);
        }

        final long profDelegate = ClientRenderProfiler.begin();
        for (int i = 0; i < this.delegateRenderers.size(); i++) {
            IModelComponentRenderer renderer = delegateRenderers.get(i);
            renderer.render(matrixStack,
                    builder,
                    transformType,
                    light, overlay);
        }
        this.delegateRenderers = new ArrayList<>();
        ClientRenderProfiler.end(ClientRenderProfiler.DELEGATE, profDelegate);

        ClientRenderProfiler.end(ClientRenderProfiler.MODEL_TOTAL, profModel);
    }

    /**
     * 静态烘焙路径
     * <ul>
     *     <li>状态段：每帧执行 functional 回调与可见性判定，产出状态指纹与委托</li>
     *     <li>几何段：仅在缓存未命中时执行，用单位 {@link PoseStack} 把节点姿态烘进模型空间网格</li>
     *     <li>绘制：用调用方的姿态矩阵绘制，不产生上传与 flush</li>
     * </ul>
     * functional 回调只在状态段执行一次，因此委托拿到的仍是真实姿态矩阵。
     *
     * @return {@code false} 表示本帧无法烘焙（有回调直接发射几何），调用方应退回即时管线
     */
    private boolean renderBaked(PoseStack matrixStack,
                                ItemDisplayContext transformType,
                                RenderType renderType,
                                int light, int overlay,
                                float red, float green, float blue, float alpha) {
        int delegateMark = this.delegateMark();
        DiscardVertexConsumer discard = DiscardVertexConsumer.INSTANCE;
        long vertexMark = discard.writtenVertices();

        long fingerprint = 0x9E3779B97F4A7C15L;
        for (int i = 0; i < this.shouldRender.size(); i++) {
            fingerprint = fingerprint * 31L + this.shouldRender.get(i)
                    .renderStatus(matrixStack, transformType, light, overlay, discard);
        }

        if (discard.writtenVertices() != vertexMark) {
            // 回调直接把几何写进 consumer（不是委托）：烘焙会丢掉这部分几何，退回即时管线
            this.trimDelegates(delegateMark);
            return false;
        }

        if (!StaticBake.drawOrBake(this, null, matrixStack.last().pose(), renderType, light, overlay,
                red, green, blue, alpha, fingerprint,
                consumer -> {
                    PoseStack bakePose = new PoseStack();
                    for (int i = 0; i < this.shouldRender.size(); i++) {
                        this.shouldRender.get(i)
                                .renderGeometry(bakePose, transformType, consumer,
                                        light, overlay, red, green, blue, alpha);
                    }
                })) {
            // 没有可用着色器：丢弃状态段登记的委托，由调用方退回即时管线
            this.trimDelegates(delegateMark);
            return false;
        }
        return true;
    }

    @ApiStatus.Internal
    int delegateMark() {
        return this.delegateRenderers.size();
    }

    /**
     * 退回即时管线前丢弃状态段登记的委托，避免同一次提交里重复渲染
     */
    @ApiStatus.Internal
    void trimDelegates(int size) {
        while (this.delegateRenderers.size() > size) {
            this.delegateRenderers.remove(this.delegateRenderers.size() - 1);
        }
    }

    // --------Deprecated--------

    /*
    原模组不仅整了个隐藏width/height说明的Vec2，还可能因为@Nullable亮了灯所以忘了
     */
    @Deprecated(forRemoval = true) public @Nullable Vec2 getSize() {
        return this.hasSize ? new Vec2(this.width, this.height) : null;
    }

    @Deprecated(forRemoval = true) protected float convertPivot(_Bone bones, int index) {
        return ClientModelUtils.pivot_BEtoJE(this.indexBones, bones, index);
    }
}
