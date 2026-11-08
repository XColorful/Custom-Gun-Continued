package dev.xcolorful.customgun.client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.model.AnimatedModelObject;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

public class HandRender implements IModelComponentRenderer {

    private final HumanoidArm humanoidArm;
    private final AnimatedModelObject animatedModelObject;

    private HandRender(HumanoidArm humanoidArm, AnimatedModelObject animatedModelObject) {
        this.humanoidArm = humanoidArm;
        this.animatedModelObject = animatedModelObject;
    }

    /**
     * 在 submit 阶段沿手部定位组路径提交第一人称手臂
     * <ul>
     *     <li>26.2 起模型几何由 {@code SubmitNodeCollector} 延迟渲染</li>
     *     <li>{@link #render} 经委托在 flush 阶段执行，此时 {@code FirstPersonArmHelper} 的收集器已被清空，提交不出去</li>
     *     <li>所以手臂必须像 {@link #render} 的调用方那样在 submit 阶段直接提交</li>
     * </ul>
     */
    @ApiStatus.AvailableSince("26.2")
    public static void renderFirstPersonArm(PoseStack poseStack,
                                            @Nullable List<BedrockPart> handPosPath,
                                            HumanoidArm hand,
                                            int light) {
        // [26.2, )
        if (handPosPath == null) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        poseStack.pushPose(); {
            for (int i = 0; i < handPosPath.size(); i++) {
                handPosPath.get(i).translate_rotate_scale(poseStack);
            }
            ClientRenderHelper.rotate(poseStack, Axis.ZP.rotationDegrees(180f));
            ClientRenderHelper.renderFirstPersonArm(player, hand, poseStack, light);
        }
        poseStack.popPose();
    }

    // --------IModelComponentRenderer--------

    @Override
    public void render(PoseStack poseStack,
                       VertexConsumer vertexBuffer,
                       ItemDisplayContext transformType,
                       int light, int overlay) {
        if (!this.animatedModelObject.getRenderHand()) return;

        if (!transformType.firstPerson()) return;

        ClientRenderHelper.rotate(poseStack, Axis.ZP.rotationDegrees(180f));
        Matrix3f normal = new Matrix3f(poseStack.last().normal());
        Matrix4f pose = new Matrix4f(poseStack.last().pose());

        //和枪械模型共用顶点缓冲的都需要代理到渲染结束后渲染
        this.animatedModelObject.delegateRender((_poseStack, _vertexBuffer, _transformType, _light, _overlay) -> {
            PoseStack poseStack1 = new PoseStack();
            poseStack1.last().normal().mul(normal);
            poseStack1.last().pose().mul(pose);

            Minecraft mc = Minecraft.getInstance();

            ClientRenderHelper.renderFirstPersonArm(mc.player, this.humanoidArm, poseStack1, _light);
        });
    }

    public static class Left extends HandRender {

        public Left(AnimatedModelObject animatedModelObject) {
            super(HumanoidArm.LEFT, animatedModelObject);
        }
    }

    public static class Right extends HandRender {

        public Right(AnimatedModelObject animatedModelObject) {
            super(HumanoidArm.RIGHT, animatedModelObject);
        }
    }
}
