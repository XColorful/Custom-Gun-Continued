package dev.xcolorful.customgun.client.model.bedrock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.model.bake.BakeDirtyTracker;
import dev.xcolorful.customgun.client.model.bake.DiscardVertexConsumer;
import dev.xcolorful.customgun.client.util.ClientRenderProfiler;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * visible的优先级低于{@link FunctionalBedrockPart}，当visible为false的时候，仍然会执行functionalRenderers
 */
public class FunctionalBedrockPart extends BedrockPart {

    public @Nullable Function<BedrockPart, IModelComponentRenderer> functionalRenderer;

    /**
     * 最近一次状态段的结果：functional 回调返回了替换渲染器
     * <li>状态段与几何段成对调用，几何段据此跳过已被替换的子树，不再重复执行回调</li>
     */
    @ApiStatus.Internal
    public boolean cgc$statusReplaced;

    public FunctionalBedrockPart(@NotNull String name, @Nullable Function<BedrockPart, IModelComponentRenderer> functionalRenderer) {
        super(name);
        this.functionalRenderer = functionalRenderer;
    }
    public FunctionalBedrockPart(@NotNull BedrockPart part, @Nullable Function<BedrockPart, IModelComponentRenderer> functionalRenderer) {
        super(part.name);
        this.cubes.addAll(part.cubes);
        this.children.addAll(part.children);
        this.x = part.x;
        this.y = part.y;
        this.z = part.z;
        this.xRot = part.xRot;
        this.yRot = part.yRot;
        this.zRot = part.zRot;
        this.offsetX = part.offsetX;
        this.offsetY = part.offsetY;
        this.offsetZ = part.offsetZ;
        this.visible = part.visible;
        this.mirror = part.mirror;
        this.setInitRotationAngle(part.getInitRotateAngleX(), part.getInitRotateAngleY(), part.getInitRotateAngleZ());
        this.xScale = part.xScale;
        this.yScale = part.yScale;
        this.zScale = part.zScale;
        this.functionalRenderer = functionalRenderer;
    }

    // --------IBedrockRender--------

    @Override
    public void render(PoseStack poseStack,
                       ItemDisplayContext transformType,
                       VertexConsumer consumer,
                       int light, int overlay,
                       float red, float green, float blue, float alpha) {
        if (ClientRenderProfiler.enabled) this.profileNodeState();

        int cubePackedLight = this.illuminated ? ClientRenderUtils.LightTexture_.pack(15, 15)
                : light;

        poseStack.pushPose(); {
            this.translate_rotate_scale(poseStack);

            boolean rendered = false;
            if (this.functionalRenderer != null) {
                final long profFunctional = ClientRenderProfiler.begin();
                @Nullable IModelComponentRenderer renderer = this.functionalRenderer.apply(this);
                if (renderer != null) {
                    renderer.render(poseStack,
                            consumer,
                            transformType,
                            cubePackedLight, overlay);
                    rendered = true;
                }
                ClientRenderProfiler.end(ClientRenderProfiler.FUNCTIONAL_NODES, profFunctional);
            }

            if (!rendered) {
                if (this.visible) {
                    super.compile(poseStack.last(),
                            consumer,
                            cubePackedLight, overlay,
                            red, green, blue, alpha);
                    for (int i = 0; i < this.children.size(); i++) {
                        BedrockPart part = this.children.get(i);
                        part.render(poseStack,
                                transformType,
                                consumer,
                                cubePackedLight, overlay,
                                red, green, blue, alpha);
                    }
                }
            }
        }
        poseStack.popPose();
    }

    // --------静态烘焙--------

    @Override
    public boolean containsFunctional() {
        // 只有真正挂了回调的节点才需要每帧维护姿态；模型加载时所有骨骼都是本类型、多数回调为 null
        return this.functionalRenderer != null || super.containsFunctional();
    }

    @Override
    public long statusStateHash() {
        // 替换渲染与否决定这棵子树是否产生几何，必须一起进指纹
        return this.stateHash() * 31L + (this.cgc$statusReplaced ? 1L : 2L);
    }

    @Override
    public long subtreeStateHash(@Nullable BakeDirtyTracker tracker) {
        // 走快速指纹路径说明本节点没有执行回调，不存在替换
        this.cgc$statusReplaced = false;
        return super.subtreeStateHash(tracker);
    }

    @Override
    protected boolean skipsGeometry() {
        return this.cgc$statusReplaced;
    }

    @Override
    public long renderStatus(PoseStack poseStack,
                             ItemDisplayContext transformType,
                             int light, int overlay,
                             DiscardVertexConsumer discard,
                             @Nullable BakeDirtyTracker tracker) {
        if (this.functionalRenderer == null) { // 没有回调：与普通节点同一套逻辑（含快速指纹）
            this.cgc$statusReplaced = false;
            return super.renderStatus(poseStack, transformType, light, overlay, discard, tracker);
        }

        int cubePackedLight = this.illuminated ? ClientRenderUtils.LightTexture_.pack(15, 15) : light;

        poseStack.pushPose(); {
            this.translate_rotate_scale(poseStack);

            boolean rendered = false;
            {
                final long profFunctional = ClientRenderProfiler.begin();
                long vertexBefore = discard.writtenVertices();
                @Nullable IModelComponentRenderer renderer = this.functionalRenderer.apply(this);
                if (renderer != null) {
                    renderer.render(poseStack,
                            discard,
                            transformType,
                            cubePackedLight, overlay);
                    rendered = true;
                }
                if (discard.writtenVertices() != vertexBefore && tracker != null) {
                    // 回调直接发射了几何（如附加弹匣）：这部分无法烘焙，只能该节点整棵子树即时绘制
                    tracker.markImmediate(this);
                }
                ClientRenderProfiler.end(ClientRenderProfiler.FUNCTIONAL_NODES, profFunctional);
            }
            this.cgc$statusReplaced = rendered;

            long hash = this.statusStateHash();
            if (tracker != null) tracker.check(this, hash);

            if (!rendered && this.visible) {
                for (int i = 0; i < this.children.size(); i++) {
                    hash = hash * 31L + this.children.get(i)
                            .renderStatus(poseStack, transformType, cubePackedLight, overlay, discard, tracker);
                }
            }
            poseStack.popPose();
            return hash;
        }
    }

    @Override
    public void renderGeometry(PoseStack poseStack,
                               ItemDisplayContext transformType,
                               VertexConsumer consumer,
                               int light, int overlay,
                               float red, float green, float blue, float alpha) {
        // 回调返回了替换渲染器：子弹树由该渲染器（委托）负责，与即时遍历语义一致
        if (this.cgc$statusReplaced) return;

        super.renderGeometry(poseStack, transformType, consumer, light, overlay, red, green, blue, alpha);
    }


    // --------Deprecated--------

    @Deprecated(forRemoval = true)
    public FunctionalBedrockPart(@Nullable Function<BedrockPart, IModelComponentRenderer> functionalRenderer, @NotNull String name) {
        this(name, functionalRenderer);
    }
}
