package dev.xcolorful.customgun.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.api.model.IModelObjectRender;
import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockRenderer;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.compat.iris.IrisCompat;
import dev.xcolorful.customgun.client.model.bake.BakeDirtyTracker;
import dev.xcolorful.customgun.client.model.bake.BakedMesh;
import dev.xcolorful.customgun.client.model.bake.DiscardVertexConsumer;
import dev.xcolorful.customgun.client.model.bake.StaticBake;
import dev.xcolorful.customgun.client.model.bake.StaticBakeCache;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.model.bedrock.FunctionalBedrockPart;
import dev.xcolorful.customgun.client.model.bedrock.NodeTransform;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry._Bone;
import dev.xcolorful.customgun.client.util.ClientModelUtils;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.client.util.ClientRenderProfiler;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry.NodeName;
import dev.xcolorful.customgun.core.resource.instance.PojoInstance;
import dev.xcolorful.customgun.core.util.MathUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.*;
import java.util.function.Predicate;

public class ModelObject extends PojoInstance<BedrockModel> implements IModelObjectRender {

    public static final long FINGERPRINT_SEED = MathUtil.GOLDEN_RATIO_64;

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
     * 渲染探针用：上次提交时节点状态的指纹，仅采集窗口内更新
     * <li>不参与渲染，仅用于统计「本次提交的几何是否真的变了」</li>
     */
    private long cgc$lastStateHash = Long.MIN_VALUE;
    /** 上一次采集到状态指纹的帧序号，用于一帧内只算一次 */
    private long cgc$profileStamp = Long.MIN_VALUE;

    // ----第一人称分块烘焙（渲染重构B1-b）----
    /*
    节点每帧被动画改写，整模烘焙每帧都会失效
    这里把模型切成若干块：
    顶层节点是块根（自身变换提升为绘制矩阵），动画命中的节点按需再切开，每块独立缓存，只有脏子树需要重烘焙
     */
    /**
     * 每个块的最大网格键数（`RenderType` / 叠加 / 颜色组合）
     * <li>光影的阴影 pass 会换一套光照（有时还换顶点格式）；光照已不建键，改由每条几何的光照变体保留</li>
     */
    private static final int CGC$MAX_MESHES_PER_CHUNK = 4;

    /**
     * 每条几何保留的光照变体数（渲染重构B1-c-B）
     * <ul>
     *     <li>不能只留一份：主 pass 与光影阴影 pass 的光照不同，每帧来回切换会退化成每帧整段重传</li>
     *     <li>不放大余量：满员才开始改写，上限越大，光照变化前要白白多烘焙几次；
     *         `b1-b-3` 实测光影下重建次数与无光影持平（2.8 vs 2.6 次/秒），同一帧确实只有这两种光照</li>
     * </ul>
     */
    private static final int CGC$MAX_LIGHT_VARIANTS = 2;

    /**
     * 按 DFS 序遍历的块列表，决定绘制顺序
     */
    private final List<Cgc$Chunk> cgc$chunks = new ArrayList<>();
    private final Map<BedrockPart, Cgc$Chunk> cgc$chunkByNode = new IdentityHashMap<>();
    /**
     * 当前以即时块绘制的块根
     * <li>哪种绘制方式是按帧决定的（真弹匣只在附加弹匣可见期间让位），下一帧要还原成一帧没有登记的那些</li>
     */
    private final Set<BedrockPart> cgc$immediateRoots = Collections.newSetFromMap(new IdentityHashMap<>());
    private int cgc$chunkOrderNext;
    private final BakeDirtyTracker cgc$dirtyTracker = new BakeDirtyTracker();
    private final Predicate<BedrockPart> cgc$isCut = this.cgc$chunkByNode::containsKey;
    private boolean cgc$chunksInitialized;

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
        this.cgc$clearChunks();

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

        // [1.20.1, 26.2)
//        /*
        MultiBufferSource.BufferSource bufferSource = ClientRenderHelper.GL.getModelBufferSource();
        VertexConsumer builder = bufferSource.getBuffer(renderType);
        ClientRenderProfiler.countBatch();
        ClientRenderProfiler.countRenderType(renderType);
//         */

        if (ClientRenderProfiler.enabled) { // 采集窗口内统计：本次提交的节点状态是否与上次不同
            long stamp = ClientRenderProfiler.frameStamp();
            // 同一模型一帧内会被多个展示框 / 光影阴影 pass 重复提交，节点状态在帧内不会变化：指纹只算一次
            boolean firstThisFrame = this.cgc$profileStamp != stamp;
            this.cgc$profileStamp = stamp;

            long hash = 1125899906842597L;
            for (IBedrockRenderer renderer : this.modelMap_values()) {
                BedrockPart part = renderer.getModelRenderer();
                ClientRenderProfiler.countNodes(1);
                if (!firstThisFrame) continue;
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
            if (firstThisFrame && hash != this.cgc$lastStateHash) {
                this.cgc$lastStateHash = hash;
                ClientRenderProfiler.countStateChange();
            }
        }

        /*
        [1.20.1, 26.2)
        即时模式下顶点在本次调用内就被提交，节点状态不会被清空，因此不需要快照与还原
        这段双重快照在 1.20.1 上纯属开销（multi 场景实测 0.135 ms/帧），渲染重构B1-a 中移除
        [26.2, ) 延迟渲染
        几何要在 prepare 阶段才真正写顶点，那时动画已把节点状态清空
        这里把本帧的动画姿态快照下来，绘制回调里写回，画完再还原成清空后的状态
         */
        // [26.2, )
        /*
        List<BedrockPart> parts = new ArrayList<>(this.modelMap.size()); {
            for (IBedrockRenderer renderer : this.modelMap_values()) {
                parts.add(renderer.getModelRenderer());
            }
        }
        List<NodeTransform> animatedTransforms = new ArrayList<>(parts.size()); {
            for (int i = 0; i < parts.size(); i++) {
                animatedTransforms.add(NodeTransform.capture(parts.get(i)));
            }
        }
        */

        RenderType bakedRenderType = ClientRenderHelper.bakePipelineState(renderType); // [26.2, )

        // 节点静态的场景走烘焙网格（渲染重构B1-a）；第一人称节点每帧被动画改写，走脏子树分块烘焙（B1-b）
        boolean baked = StaticBake.eligible()
                && (transformType.firstPerson()
                    ? this.renderBakedFirstPerson(matrixStack,
//                collector, animatedTransforms, // [26.2, )
                transformType, bakedRenderType, light, overlay, red, green, blue, alpha)
                    : this.renderBaked(matrixStack,
                transformType, bakedRenderType, light, overlay, red, green, blue, alpha));

        // [1.20.1, 26.2)
//        /*
        if (!baked) { // 无法烘焙时退回即时管线
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
//        */

        // [26.2, )
        /*
        if (!baked) { // 无法烘焙时退回即时管线
            this.cgc$submitImmediateModel(matrixStack,
                    collector, animatedTransforms,
                    transformType, bakedRenderType, light, overlay, red, green, blue, alpha);
        }

        // 状态段登记的委托也要在绘制回调里执行：它们读节点姿态，而绘制阶段节点已被清空
        this.cgc$submitDelegates(matrixStack,
                collector, animatedTransforms,
                transformType, bakedRenderType, light, overlay);

        ClientRenderProfiler.end(ClientRenderProfiler.MODEL_TOTAL, profModel);
        */
    }

    /**
     * 静态烘焙路径
     * <ul>
     *     <li>状态段：每帧执行 functional 回调与可见性判定，产出状态指纹与委托</li>
     *     <li>几何段：仅在缓存未命中时执行，用单位 {@link PoseStack} 把节点姿态烘进模型空间网格</li>
     *     <li>绘制：用调用方的姿态矩阵绘制，不产生上传与 flush</li>
     * </ul>
     * functional 回调只在状态段执行一次，因此委托拿到的仍是真实姿态矩阵
     * @return {@code false} 表示本帧无法烘焙（有回调直接发射几何），调用方应退回即时管线
     */
    private boolean renderBaked(PoseStack matrixStack,
                                ItemDisplayContext transformType,
                                RenderType renderType,
                                int light, int overlay,
                                float red, float green, float blue, float alpha) {
        // 提前判断，避免同帧把 functional 回调执行两次后才发现无法绘制
        if (!StaticBake.available()) return false;

        int delegateMark = this.delegateMark();
        DiscardVertexConsumer discard = DiscardVertexConsumer.INSTANCE;
        long vertexMark = discard.writtenVertices();

        long fingerprint = FINGERPRINT_SEED;
        final long profStatus = ClientRenderProfiler.begin();
        for (int i = 0; i < this.shouldRender.size(); i++) {
            fingerprint = fingerprint * 31L + this.shouldRender.get(i)
                    .renderStatus(matrixStack, transformType, light, overlay, discard, null);
        }
        ClientRenderProfiler.end(ClientRenderProfiler.STATIC_STATUS, profStatus);

        if (discard.writtenVertices() != vertexMark) {
            // 回调直接把几何写进 consumer（不是委托）：烘焙会丢掉这部分几何，退回即时管线
            this.trimDelegates(delegateMark);
            return false;
        }

        if (!StaticBake.drawOrBake(this, null,
                matrixStack.last().pose(), // [1.20.1, 26.2)
//                matrixStack, // [26.2, )
                renderType, light, overlay,
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

    /**
     * 第一人称分块烘焙（渲染重构B1-b）
     * <ul>
     *     <li>状态段每帧执行，并逐节点比较状态，收集脏节点</li>
     *     <li>顶层节点是块根：自身变换提升为绘制矩阵，动画写根不产生重烘焙</li>
     *     <li>其余节点首次变脏时在它所在的块上切开，只重烘焙脏节点自己的子树</li>
     *     <li>functional 回调直接发射几何的节点改为「单节点退回」：该子树永远即时绘制，不再让整个模型退回</li>
     * </ul>
     * @return {@code false} 表示本帧无法烘焙（没有可用着色器），调用方应退回即时管线
     */
    private boolean renderBakedFirstPerson(PoseStack matrixStack,
//                                           SubmitNodeCollector collector, // [26.2, )
//                                           List<NodeTransform> animatedTransforms, // [26.2, )
                                           ItemDisplayContext transformType,
                                           RenderType renderType,
                                           int light, int overlay,
                                           float red, float green, float blue, float alpha) {
        // 提前判断，避免同帧把 functional 回调执行两次后才发现无法绘制
        if (!StaticBake.available()) return false;

        int delegateMark = this.delegateMark();
        BakeDirtyTracker tracker = this.cgc$dirtyTracker;
        tracker.begin();
        DiscardVertexConsumer discard = DiscardVertexConsumer.INSTANCE;
        for (int i = 0; i < this.shouldRender.size(); i++) {
            this.shouldRender.get(i)
                    .renderStatus(matrixStack, transformType, light, overlay, discard, tracker);
        }
        this.cgc$collectImmediate(tracker);

        if (!this.cgc$chunksInitialized) {
            this.cgc$initChunks();
        } else {
            this.cgc$applyDirty(tracker);
        }

        if (!this.cgc$drawChunks(matrixStack,
//                collector, animatedTransforms, // [26.2, )
                transformType, renderType, light, overlay, red, green, blue, alpha)) {
            // 退回即时管线前丢弃状态段登记的委托，避免同一次提交里重复渲染
            this.trimDelegates(delegateMark);
            return false;
        }
        return true;
    }

    /**
     * 把整模几何提交为一次自定义几何绘制（无法烘焙时的退回路径）
     */
    @ApiStatus.AvailableSince("26.2")
    private void cgc$submitImmediateModel(PoseStack matrixStack,
                                          Object collector, // SubmitNodeCollector collector,
                                          List<NodeTransform> animatedTransforms,
                                          ItemDisplayContext transformType,
                                          RenderType renderType,
                                          int light, int overlay,
                                          float red, float green, float blue, float alpha) {
        /*
        final long profTraverse = ClientRenderProfiler.begin();
        ClientRenderProfiler.countBatch();
        ClientRenderProfiler.countRenderType(renderType);
        collector.submitCustomGeometry(matrixStack, renderType, (pose, builder) -> {
            List<NodeTransform> cleanedTransforms = new ArrayList<>(animatedTransforms.size());
            PoseStack poseStack = cgc$enterDrawCallback(pose, animatedTransforms, cleanedTransforms);
            try {
                {
                    for (int i = 0; i < this.shouldRender.size(); i++) {
                        this.shouldRender.get(i)
                                .render(poseStack,
                                        transformType,
                                        builder,
                                        light, overlay,
                                        red, green, blue, alpha);
                    }
                }
            } finally {
                cgc$exitDrawCallback(cleanedTransforms);
            }
        });
        ClientRenderProfiler.end(ClientRenderProfiler.TRAVERSE, profTraverse);
        */
    }
    /**
     * 把状态段登记的委托提交为一次自定义几何绘制
     */
    @ApiStatus.AvailableSince("26.2")
    private void cgc$submitDelegates(PoseStack matrixStack,
                                     Object collector, // SubmitNodeCollector collector,
                                     List<NodeTransform> animatedTransforms,
                                     ItemDisplayContext transformType,
                                     RenderType renderType,
                                     int light, int overlay) {
        /*
        if (this.delegateRenderers.isEmpty()) return;

        // 回调延后执行，这里把列表换掉，避免后续登记混进来
        final List<IModelComponentRenderer> delegates = this.delegateRenderers;
        this.delegateRenderers = new ArrayList<>();

        final long profDelegate = ClientRenderProfiler.begin();
        ClientRenderProfiler.countRenderType(renderType);
        collector.submitCustomGeometry(matrixStack, renderType, (pose, builder) -> {
            List<NodeTransform> cleanedTransforms = new ArrayList<>(animatedTransforms.size());
            PoseStack poseStack = cgc$enterDrawCallback(pose, animatedTransforms, cleanedTransforms);
            try {
                for (int i = 0; i < delegates.size(); i++) {
                    delegates.get(i).render(poseStack,
                            builder,
                            transformType,
                            light, overlay);
                }
            } finally {
                cgc$exitDrawCallback(cleanedTransforms);
            }
        });
        ClientRenderProfiler.end(ClientRenderProfiler.DELEGATE, profDelegate);
         */
    }
    /**
     * 延迟渲染的绘制回调里，节点状态已被动画清空：先记下清空后的状态，再写回提交时快照的姿态
     * @param cleanedTransforms 出口处必须 apply 回去，否则会影响同帧其它绘制
     * @return 以快照姿态为基底的 PoseStack，可直接递归渲染节点
     */
    @ApiStatus.AvailableSince("26.2")
    private static PoseStack cgc$enterDrawCallback(PoseStack.Pose pose,
                                                   List<NodeTransform> animatedTransforms,
                                                   List<NodeTransform> cleanedTransforms) {
        return null;
        /*
        for (int i = 0; i < animatedTransforms.size(); i++) {
            cleanedTransforms.add(NodeTransform.capture(animatedTransforms.get(i).part()));
        }
        PoseStack poseStack = new PoseStack();
        poseStack.last().set(pose);
        // 与烘焙回放同口径：法线从位置矩阵现算，不沿用调用方可能失真的 normal
        poseStack.last().normal().set(poseStack.last().pose()).invert().transpose();
        for (int i = 0; i < animatedTransforms.size(); i++) {
            animatedTransforms.get(i).apply();
        }
        return poseStack;
         */
    }
    @ApiStatus.AvailableSince("26.2")
    private static void cgc$exitDrawCallback(List<NodeTransform> cleanedTransforms) {
        /*
        for (int i = 0; i < cleanedTransforms.size(); i++) {
            cleanedTransforms.get(i).apply();
        }
         */
    }

    /**
     * 状态段跑完之后、应用脏节点之前的一次回调
     * <ul>
     *     <li>子类在这里把「本帧必须和即时块走同一条即时绘制路径」的节点也登记为即时块</li>
     *     <li>例如附加弹匣的即时块会再画一遍真弹匣，两份几何重合</li>
     *     <li>真弹匣留在烘焙块里时，它的深度是「(ModelView × 姿态) × 顶点」，即时块是「ModelView × (姿态 × 顶点)」</li>
     *     <li>浮点结合顺序不同就差最后一位，重合面随视角来回压过 → 闪烁</li>
     * </ul>
     */
    protected void cgc$collectImmediate(BakeDirtyTracker tracker) {}

    private void cgc$initChunks() {
        this.cgc$chunksInitialized = true;
        StaticBakeCache.registerModel(this);

        for (int i = 0; i < this.shouldRender.size(); i++) {
            BedrockPart part = this.shouldRender.get(i);
            this.cgc$assignOrder(part);
            Cgc$Chunk chunk = new Cgc$Chunk(part, false);
            this.cgc$chunkByNode.put(part, chunk);
            this.cgc$chunks.add(chunk);
        }

        // 首帧的回调也可能已经直接发射了几何；这一帧的脏列表因为还没建立基线要丢弃，但即时节点必须保留
        List<BedrockPart> immediate = this.cgc$dirtyTracker.immediate();
        for (int i = 0; i < immediate.size(); i++) {
            this.cgc$markImmediateChunk(immediate.get(i));
        }
    }

    /**
     * DFS 前序序号：决定块的绘制顺序，与即时遍历的提交顺序一致
     */
    private void cgc$assignOrder(BedrockPart part) {
        part.cgc$dfsOrder = this.cgc$chunkOrderNext++;
        for (int i = 0; i < part.children.size(); i++) {
            this.cgc$assignOrder(part.children.get(i));
        }
    }

    private void cgc$applyDirty(BakeDirtyTracker tracker) {
        List<BedrockPart> immediate = tracker.immediate();
        for (int i = 0; i < immediate.size(); i++) {
            this.cgc$markImmediateChunk(immediate.get(i));
        }
        this.cgc$restoreStaleImmediate(immediate);

        List<BedrockPart> dirty = tracker.dirty();
        for (int i = 0; i < dirty.size(); i++) {
            BedrockPart node = dirty.get(i);
            // 块根自己的变换 / 可见性都不进网格（变换由矩阵承担、可见性在绘制时剪枝），无需重烘焙
            if (this.cgc$chunkByNode.containsKey(node)) continue;

            Cgc$Chunk owner = this.cgc$nearestChunk(node.getParent());
            if (owner == null || owner.immediate) continue;
            // 所在块本帧已经要重建，脏节点会一起烘进去
            if (owner.dirty || owner.stale) continue;

            this.cgc$splitAt(node, owner);
        }
    }

    private void cgc$splitAt(BedrockPart node, Cgc$Chunk owner) {
        Cgc$Chunk chunk = new Cgc$Chunk(node, false);
        this.cgc$chunkByNode.put(node, chunk);
        this.cgc$insertChunk(chunk);
        this.cgc$invalidate(owner);
    }

    /**
     * functional 回调直接发射几何：该节点整棵子树改用即时绘制
     */
    private void cgc$markImmediateChunk(BedrockPart node) {
        this.cgc$immediateRoots.add(node);

        Cgc$Chunk existing = this.cgc$chunkByNode.get(node);
        if (existing != null && existing.immediate) return;

        Cgc$Chunk owner = this.cgc$nearestChunk(node.getParent());
        // 祖先已经是即时块：它的回调整棵子树都会画一遍，再给本节点切一块就是画两遍
        if (owner != null && owner.immediate) return;

        this.cgc$removeChunkAndDescendants(node);

        Cgc$Chunk chunk = new Cgc$Chunk(node, true);
        this.cgc$chunkByNode.put(node, chunk);
        this.cgc$insertChunk(chunk);

        if (owner != null) this.cgc$invalidate(owner);
    }

    /**
     * 本帧不再需要即时绘制的节点还原成烘焙块
     * <li>还原后仍是自己的块根：父块的网格早就把它切出去了，合回去要连带重烘父块，得不偿失</li>
     * <li>新块带着「脏」状态，下一次绘制会重新烘焙</li>
     */
    private void cgc$restoreStaleImmediate(List<BedrockPart> immediate) {
        if (this.cgc$immediateRoots.isEmpty()) return;
        this.cgc$immediateRoots.removeIf(node -> {
            if (immediate.contains(node)) return false;
            Cgc$Chunk chunk = this.cgc$chunkByNode.get(node);
            if (chunk != null && chunk.immediate) this.cgc$restoreChunk(node, chunk);
            return true;
        });
    }

    private void cgc$restoreChunk(BedrockPart node, Cgc$Chunk immediateChunk) {
        this.cgc$chunks.remove(immediateChunk);
        this.cgc$chunkByNode.remove(node);
        immediateChunk.close();

        Cgc$Chunk chunk = new Cgc$Chunk(node, false);
        this.cgc$chunkByNode.put(node, chunk);
        this.cgc$insertChunk(chunk);
    }

    private void cgc$removeChunkAndDescendants(BedrockPart node) {
        for (int i = this.cgc$chunks.size() - 1; i >= 0; i--) {
            Cgc$Chunk chunk = this.cgc$chunks.get(i);
            if (chunk.node == node || cgc$isDescendant(chunk.node, node)) {
                this.cgc$chunks.remove(i);
                this.cgc$chunkByNode.remove(chunk.node);
                chunk.close();
            }
        }
    }

    private static boolean cgc$isDescendant(BedrockPart node, BedrockPart ancestor) {
        for (BedrockPart part = node.getParent(); part != null; part = part.getParent()) {
            if (part == ancestor) return true;
        }
        return false;
    }

    private Cgc$Chunk cgc$nearestChunk(@Nullable BedrockPart node) {
        for (BedrockPart part = node; part != null; part = part.getParent()) {
            Cgc$Chunk chunk = this.cgc$chunkByNode.get(part);
            if (chunk != null) return chunk;
        }
        return null;
    }

    private void cgc$insertChunk(Cgc$Chunk chunk) {
        int order = this.cgc$orderOf(chunk.node);
        int index = 0;
        while (index < this.cgc$chunks.size() && this.cgc$orderOf(this.cgc$chunks.get(index).node) < order) {
            index++;
        }
        this.cgc$chunks.add(index, chunk);
    }

    private int cgc$orderOf(BedrockPart node) {
        return node.cgc$dfsOrder >= 0 ? node.cgc$dfsOrder : Integer.MAX_VALUE;
    }

    private void cgc$invalidate(Cgc$Chunk chunk) {
        chunk.close();
        chunk.dirty = true;
    }

    @ApiStatus.Internal
    public void cgc$clearChunks() {
        for (int i = 0; i < this.cgc$chunks.size(); i++) {
            this.cgc$chunks.get(i).close();
        }
        this.cgc$chunks.clear();
        this.cgc$chunkByNode.clear();
        this.cgc$immediateRoots.clear();
        this.cgc$chunkOrderNext = 0;
        this.cgc$chunksInitialized = false;
    }

    private boolean cgc$drawChunks(PoseStack matrixStack,
//                                   SubmitNodeCollector collector, // [26.2, )
//                                   List<NodeTransform> animatedTransforms, // [26.2, )
                                   ItemDisplayContext transformType,
                                   RenderType renderType,
                                   int light, int overlay,
                                   float red, float green, float blue, float alpha) {
        boolean drawnAny = false;
        boolean drawFailed = false;

        for (int i = 0; i < this.cgc$chunks.size(); i++) {
            Cgc$Chunk chunk = this.cgc$chunks.get(i);
            // 即时块自己的可见性由回调判定，其余块（含块根）不可见就整棵跳过
            if (this.cgc$isPruned(chunk.node, !chunk.immediate)) {
                if (!chunk.immediate) chunk.stale = true;
                continue;
            }

            if (chunk.immediate) {
                this.cgc$drawImmediate(chunk, matrixStack,
//                        collector, animatedTransforms, // [26.2, )
                        transformType, renderType, light, overlay, red, green, blue, alpha);
                drawnAny = true;
                continue;
            }

            BakedMesh mesh = this.cgc$getOrBake(chunk, renderType, light, overlay,
                    red, green, blue, alpha);

            if (StaticBake.drawMesh(mesh, renderType,
                    this.cgc$chunkPose(chunk, matrixStack)) // [1.20.1, 26.2)
//                    this.cgc$chunkPoseStack(chunk, matrixStack)) // [26.2, )
            ) {
                drawnAny = true;
            } else {
                // 网格已关闭（资源重载竞态）或着色器不可用：下一帧重建，本帧保持现状避免重复绘制
                chunk.dirty = true;
                drawFailed = true;
            }
        }

        // 一个网格都没画出来（着色器刚失效）时退回即时管线；已经画了部分时保持现状，避免重复绘制
        return drawnAny || !drawFailed;
    }

    private BakedMesh cgc$getOrBake(Cgc$Chunk chunk,
                                    RenderType renderType,
                                              int light, int overlay,
                                              float red, float green, float blue, float alpha) {
        Cgc$MeshKey key = new Cgc$MeshKey(renderType, overlay,
                IrisCompat.isUsingRenderPack(), red, green, blue, alpha);

        boolean invalidated = chunk.dirty || chunk.stale;
        // 失效前这一条几何已经有网格，才是「失效导致的重建」；块的第一次烘焙不算
        boolean reloaded = invalidated && chunk.meshes.containsKey(key);
        if (invalidated) {
            // 失效是整个块的：其它光照（光影阴影 pass）的网格同样过期，一并丢弃
            chunk.close();
            chunk.dirty = false;
            chunk.stale = false;
        }

        Map<Integer, BakedMesh> variants = chunk.meshes.get(key);
        if (variants == null) {
            // 容量上限：插入前淘汰最久未用的那条
            if (chunk.meshes.size() >= CGC$MAX_MESHES_PER_CHUNK) {
                Iterator<Map.Entry<Cgc$MeshKey, Map<Integer, BakedMesh>>> iterator = chunk.meshes.entrySet().iterator();
                if (iterator.hasNext()) {
                    Map.Entry<Cgc$MeshKey, Map<Integer, BakedMesh>> eldest = iterator.next();
                    iterator.remove();
                    cgc$closeVariants(eldest.getValue());
                }
            }
            // accessOrder = true：迭代顺序即最久未用顺序
            variants = new LinkedHashMap<>(2, 0.75F, true);
            chunk.meshes.put(key, variants);
        }

        BakedMesh mesh = variants.get(light);
        if (mesh != null) return mesh;

        // 光照变了：优先改写一份现成网格（原地 patch uv2 + 重传原 VBO），而不是重新发射几何
        BakedMesh reusable = cgc$takeReusable(variants);
        boolean patchFailed = false;
        if (reusable != null) {
            if (reusable.updateLight(light)) {
                variants.put(light, reusable);
                return reusable;
            }
            reusable.close();
            patchFailed = true;
        }

        ClientRenderProfiler.countBakeCause(reloaded, patchFailed, variants.isEmpty());
        final long profBake = ClientRenderProfiler.begin();
        mesh = BakedMesh.bakePatchable(renderType.mode(), // renderType.primitiveTopology(),
                renderType.format(), light, consumer ->
                        chunk.node.bakeChunkGeometry(new PoseStack(), consumer,
                                light, overlay, red, green, blue, alpha, this.cgc$isCut));
        ClientRenderProfiler.end(ClientRenderProfiler.BAKE, profBake);
        ClientRenderProfiler.countBake();

        variants.put(light, mesh);
        chunk.dirty = false;
        chunk.stale = false;
        return mesh;
    }

    /**
     * 满员时取出最久未使用的光照变体，供改写后放回；未满员返回 {@code null}，调用方应正常烘焙
     */
    private static @Nullable BakedMesh cgc$takeReusable(Map<Integer, BakedMesh> variants) {
        if (variants.size() < CGC$MAX_LIGHT_VARIANTS) return null;
        Iterator<Map.Entry<Integer, BakedMesh>> iterator = variants.entrySet().iterator();
        if (!iterator.hasNext()) return null;
        Map.Entry<Integer, BakedMesh> eldest = iterator.next();
        iterator.remove();
        return eldest.getValue();
    }

    private static void cgc$closeVariants(Map<Integer, BakedMesh> variants) {
        for (BakedMesh mesh : variants.values()) {
            mesh.close();
        }
        variants.clear();
    }

    /**
     * 块的模型空间 → 视图空间变换：调用方姿态 × 祖先链 × 块根自身变换（即时块不含自身，交给回调）
     */
    private PoseStack cgc$chunkPoseStack(Cgc$Chunk chunk, PoseStack matrixStack) {
        PoseStack pose = new PoseStack();
        pose.last().pose().set(matrixStack.last().pose());
        /*
        即时块写进原版 RenderType 的缓冲，那套着色器直接点乘视图空间法线；只抄 pose 会让法线丢掉调用方的旋转
        烘焙块存的是模型空间法线，由烘焙着色器乘 IT(姿态)，不受这里影响
        两者要对齐就得同源：调用方的 normal 在视角大幅旋转后会与 IT(姿态) 差到 1.0（1.20.1 实测，同一帧里
        烘焙块正常、即时块偏亮），因此这里从位置矩阵现算，而不是照抄

        调用方的 normal 为什么会失真还没查清；已排除项、影响面与复现方式见
        docs/architecture/client/render/refactor/b1b-first-person-bake.md 的「未查清」一节，
        那边修好后请一并更新本注释和文档
         */
        pose.last().normal().set(pose.last().pose()).invert().transpose();
        for (int i = 0; i < chunk.path.length; i++) {
            chunk.path[i].translate_rotate_scale(pose);
        }
        if (!chunk.immediate) {
            chunk.node.translate_rotate_scale(pose);
        }
        return pose;
    }

    private Matrix4f cgc$chunkPose(Cgc$Chunk chunk, PoseStack matrixStack) {
        return this.cgc$chunkPoseStack(chunk, matrixStack).last().pose();
    }

//    @Deprecated(since = "26.2")
    private void cgc$drawImmediate(Cgc$Chunk chunk, PoseStack matrixStack,
                                   ItemDisplayContext transformType,
                                   RenderType renderType,
                                   int light, int overlay,
                                   float red, float green, float blue, float alpha) {
        MultiBufferSource.BufferSource bufferSource = ClientRenderHelper.GL.getModelBufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        ClientRenderProfiler.countBatch();
        ClientRenderProfiler.countRenderType(renderType);

        // 回调在状态段已经执行过一次并登记了委托；这里重放几何时再登记的委托是重复的
        int delegateMark = this.delegateMark();
        chunk.node.render(this.cgc$chunkPoseStack(chunk, matrixStack),
                transformType,
                consumer,
                light, overlay,
                red, green, blue, alpha);
        this.trimDelegates(delegateMark);

        final long profFlush = ClientRenderProfiler.begin();
        ClientRenderProfiler.countFlush();
        if (!IrisCompat.endBatch(bufferSource)) {
            bufferSource.endBatch();
        }
        ClientRenderProfiler.end(ClientRenderProfiler.FLUSH, profFlush);
    }
    @ApiStatus.AvailableSince("26.2")
    private void cgc$submitImmediateChunk(Cgc$Chunk chunk, PoseStack matrixStack,
                                          Object collector, // SubmitNodeCollector collector,
                                          List<NodeTransform> animatedTransforms,
                                          ItemDisplayContext transformType,
                                          RenderType renderType,
                                          int light, int overlay,
                                          float red, float green, float blue, float alpha) {
        /*
        ClientRenderProfiler.countBatch();
        ClientRenderProfiler.countRenderType(renderType);

        collector.submitCustomGeometry(matrixStack, renderType, (pose, consumer) -> {
            List<NodeTransform> cleanedTransforms = new ArrayList<>(animatedTransforms.size());
            PoseStack poseStack = cgc$enterDrawCallback(pose, animatedTransforms, cleanedTransforms);
            try {
                // 回调在状态段已经执行过一次并登记了委托；这里重放几何时再登记的委托是重复的
                int delegateMark = this.delegateMark();
                chunk.node.render(this.cgc$chunkPoseStack(chunk, poseStack),
                        transformType,
                        consumer,
                        light, overlay,
                        red, green, blue, alpha);
                this.trimDelegates(delegateMark);
            } finally {
                cgc$exitDrawCallback(cleanedTransforms);
            }
        });
         */
    }

    /**
     * 祖先链（以及可选的自身）不可见或被替换渲染时，这棵子树不产生几何
     * @param includeSelf 即时块传 {@code false}：回调自己决定是否绘制，即使节点不可见
     */
    private static boolean cgc$isPruned(BedrockPart node, boolean includeSelf) {
        for (BedrockPart part = includeSelf ? node : node.getParent(); part != null; part = part.getParent()) {
            if (!part.visible) return true;
            if (part instanceof FunctionalBedrockPart functional
                    && functional.functionalRenderer != null
                    && functional.cgc$statusReplaced) return true;
        }
        return false;
    }

    /**
     * 一个烘焙块：以 {@code node} 为根，网格只覆盖「块根之下、没有被别的块切走」的部分
     */
    private static final class Cgc$Chunk {
        final BedrockPart node;
        /**
         * 顶层 → 块根父节点，自上而下；不含块根自身
         */
        final BedrockPart[] path;
        final boolean immediate;
        final Map<Cgc$MeshKey, Map<Integer, BakedMesh>> meshes = new LinkedHashMap<>(2);
        boolean dirty = true;
        /**
         * 被祖先剪枝跳过过：恢复可见时必须重烘焙（隐藏期间的状态变化不会被状态段看到）
         */
        boolean stale = true;

        Cgc$Chunk(BedrockPart node, boolean immediate) {
            this.node = node;
            this.immediate = immediate;

            List<BedrockPart> ancestors = new ArrayList<>();
            for (BedrockPart part = node.getParent(); part != null; part = part.getParent()) {
                ancestors.add(part);
            }
            Collections.reverse(ancestors);
            this.path = ancestors.toArray(new BedrockPart[0]);
        }

        void close() {
            for (Map<Integer, BakedMesh> variants : this.meshes.values()) {
                cgc$closeVariants(variants);
            }
            this.meshes.clear();
        }
    }

    private record Cgc$MeshKey(RenderType renderType, int overlay, boolean shaderPack,
                               float red, float green, float blue, float alpha) {}

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
