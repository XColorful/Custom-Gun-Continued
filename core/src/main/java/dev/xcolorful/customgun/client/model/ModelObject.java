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

    /*
    --------第一人称分块烘焙（渲染重构B1-b）--------
    节点每帧被动画改写，整模烘焙每帧都会失效。这里把模型切成若干块：
    顶层节点是块根（自身变换提升为绘制矩阵），动画命中的节点按需再切开，
    每块独立缓存，只有脏子树需要重烘焙。
     */
    /** 每个块的最大网格数：光影的阴影 pass 会换一套光照（有时还换顶点格式），保留少量条目避免每帧重烘焙 */
    private static final int CGC$MAX_MESHES_PER_CHUNK = 4;

    /** 按 DFS 序遍历的块列表，决定绘制顺序 */
    private final List<Cgc$Chunk> cgc$chunks = new ArrayList<>();
    private final Map<BedrockPart, Cgc$Chunk> cgc$chunkByNode = new IdentityHashMap<>();
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

        MultiBufferSource.BufferSource bufferSource = ClientRenderHelper.GL.getModelBufferSource();
        VertexConsumer builder = bufferSource.getBuffer(renderType);
        ClientRenderProfiler.countBatch();
        ClientRenderProfiler.countRenderType(renderType);

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

        // 节点静态的场景走烘焙网格（渲染重构B1-a）；第一人称节点每帧被动画改写，走脏子树分块烘焙（B1-b）
        boolean baked = StaticBake.eligible()
                && (transformType.firstPerson()
                    ? this.renderBakedFirstPerson(matrixStack, transformType, bakedRenderType, light, overlay,
                            red, green, blue, alpha)
                    : this.renderBaked(matrixStack, transformType, bakedRenderType, light, overlay,
                            red, green, blue, alpha));
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
        int delegateMark = this.delegateMark();
        DiscardVertexConsumer discard = DiscardVertexConsumer.INSTANCE;
        long vertexMark = discard.writtenVertices();

        long fingerprint = FINGERPRINT_SEED;
        for (int i = 0; i < this.shouldRender.size(); i++) {
            fingerprint = fingerprint * 31L + this.shouldRender.get(i)
                    .renderStatus(matrixStack, transformType, light, overlay, discard, null);
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

        if (!this.cgc$chunksInitialized) {
            this.cgc$initChunks();
        } else {
            this.cgc$applyDirty(tracker);
        }

        if (!this.cgc$drawChunks(matrixStack, transformType, renderType, light, overlay,
                red, green, blue, alpha)) {
            // 退回即时管线前丢弃状态段登记的委托，避免同一次提交里重复渲染
            this.trimDelegates(delegateMark);
            return false;
        }
        return true;
    }

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

    /** DFS 前序序号：决定块的绘制顺序，与即时遍历的提交顺序一致 */
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
        Cgc$Chunk existing = this.cgc$chunkByNode.get(node);
        if (existing != null && existing.immediate) return;

        this.cgc$removeChunkAndDescendants(node);

        Cgc$Chunk chunk = new Cgc$Chunk(node, true);
        this.cgc$chunkByNode.put(node, chunk);
        this.cgc$insertChunk(chunk);

        Cgc$Chunk owner = this.cgc$nearestChunk(node.getParent());
        if (owner != null) this.cgc$invalidate(owner);
    }

    private void cgc$removeChunkAndDescendants(BedrockPart node) {
        for (int i = this.cgc$chunks.size() - 1; i >= 0; i--) {
            Cgc$Chunk chunk = this.cgc$chunks.get(i);
            if (chunk.node == node || this.cgc$isDescendant(chunk.node, node)) {
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
        this.cgc$chunkOrderNext = 0;
        this.cgc$chunksInitialized = false;
    }

    private boolean cgc$drawChunks(PoseStack matrixStack,
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
                this.cgc$drawImmediate(chunk, matrixStack, transformType, renderType, light, overlay,
                        red, green, blue, alpha);
                drawnAny = true;
                continue;
            }

            BakedMesh mesh = this.cgc$getOrBake(chunk, renderType, light, overlay,
                    red, green, blue, alpha);

            if (StaticBake.drawMesh(mesh, renderType, this.cgc$chunkPose(chunk, matrixStack))) {
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
        Cgc$MeshKey key = new Cgc$MeshKey(renderType, light, overlay,
                IrisCompat.isUsingRenderPack(), red, green, blue, alpha);

        if (chunk.dirty || chunk.stale) {
            // 失效是整个块的：其它光照（光影阴影 pass）的网格同样过期，一并丢弃
            chunk.close();
            chunk.dirty = false;
            chunk.stale = false;
        }

        BakedMesh mesh = chunk.meshes.get(key);
        if (mesh != null) return mesh;

        final long profBake = ClientRenderProfiler.begin();
        mesh = BakedMesh.bake(renderType.mode(), renderType.format(), consumer ->
                chunk.node.bakeChunkGeometry(new PoseStack(), consumer,
                        light, overlay, red, green, blue, alpha, this.cgc$isCut));
        ClientRenderProfiler.end(ClientRenderProfiler.BAKE, profBake);
        ClientRenderProfiler.countBake();

        // 容量上限：插入前淘汰最久未用的那条
        if (chunk.meshes.size() >= CGC$MAX_MESHES_PER_CHUNK) {
            Iterator<Map.Entry<Cgc$MeshKey, BakedMesh>> iterator = chunk.meshes.entrySet().iterator();
            if (iterator.hasNext()) {
                Map.Entry<Cgc$MeshKey, BakedMesh> eldest = iterator.next();
                iterator.remove();
                eldest.getValue().close();
            }
        }
        chunk.meshes.put(key, mesh);
        chunk.dirty = false;
        chunk.stale = false;
        return mesh;
    }

    /**
     * 块的模型空间 → 视图空间变换：调用方姿态 × 祖先链 × 块根自身变换（即时块不含自身，交给回调）
     */
    private PoseStack cgc$chunkPoseStack(Cgc$Chunk chunk, PoseStack matrixStack) {
        PoseStack pose = new PoseStack();
        pose.last().pose().set(matrixStack.last().pose());
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

    private void cgc$drawImmediate(Cgc$Chunk chunk,
                                   PoseStack matrixStack,
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
        /** 顶层 → 块根父节点，自上而下；不含块根自身 */
        final BedrockPart[] path;
        final boolean immediate;
        final Map<Cgc$MeshKey, BakedMesh> meshes = new LinkedHashMap<>(2);
        boolean dirty = true;
        /** 被祖先剪枝跳过过：恢复可见时必须重烘焙（隐藏期间的状态变化不会被状态段看到） */
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
            for (BakedMesh mesh : this.meshes.values()) {
                mesh.close();
            }
            this.meshes.clear();
        }
    }

    private record Cgc$MeshKey(RenderType renderType, int light, int overlay, boolean shaderPack,
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
