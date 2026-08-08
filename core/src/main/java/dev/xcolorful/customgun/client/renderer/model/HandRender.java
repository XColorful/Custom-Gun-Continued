/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.model.AnimatedModelObject;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class HandRender implements IModelComponentRenderer {

    private final HumanoidArm humanoidArm;
    private final AnimatedModelObject animatedModelObject;

    private HandRender(HumanoidArm humanoidArm, AnimatedModelObject animatedModelObject) {
        this.humanoidArm = humanoidArm;
        this.animatedModelObject = animatedModelObject;
    }

    // --------IModelComponentRenderer--------

    @Override
    public void render(PoseStack poseStack,
                       VertexConsumer vertexBuffer,
                       ItemDisplayContext transformType,
                       int light, int overlay) {
        if (!this.animatedModelObject.getRenderHand()) return;

        if (!transformType.firstPerson()) return;

        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        Matrix3f normal = new Matrix3f(poseStack.last().normal());
        Matrix4f pose = new Matrix4f(poseStack.last().pose());

        //和枪械模型共用顶点缓冲的都需要代理到渲染结束后渲染
        this.animatedModelObject.delegateRender((_poseStack, _vertexBuffer, _transformType, _light, _overlay) -> {
            PoseStack poseStack1 = new PoseStack();
            poseStack1.last().normal().mul(normal);
            poseStack1.last().pose().mul(pose);

            Minecraft mc = Minecraft.getInstance();

            ClientRenderHelper.renderFirstPersonArm(mc.player, this.humanoidArm, poseStack1, _light);
            mc.renderBuffers().bufferSource().endBatch();
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
