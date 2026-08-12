/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model.bedrock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry.bone.cube._Uv;
import net.minecraft.client.model.EntityModel;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

public class SlotModel extends EntityModel<Entity> {

    private final BedrockPart part;

    public SlotModel(boolean illuminated) {
        this.part = new BedrockPart("slot");
        this.part.setPos(8.0f, 24.0f, -10.0f);
        this.part.cubes.add(new BedrockCubePerFace(-16.0f, -16.0f, 9.5f,
                16.0f, 16.0f,
                0, 0,
                16, 16,
                _Uv.singleSouthFace()));
        this.part.illuminated = illuminated;
    }
    public SlotModel() {
        this(false);
    }

    @Override
    public void setupAnim(@NotNull Entity entity,
                          float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    /**
     * 这个方法在1.21.1被移除，改用{@link #renderToBuffer(PoseStack, VertexConsumer, int, int, int)}来兼容
     */
    @Deprecated(forRemoval = true)
    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack,
                               @NotNull VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.part.render(poseStack,
                ItemDisplayContext.GUI,
                buffer,
                packedLight, packedOverlay);
    }
    public void renderToBuffer(@NotNull PoseStack poseStack,
                               @NotNull VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               int argb32) {
        this.part.render(poseStack,
                ItemDisplayContext.GUI,
                buffer,
                packedLight, packedOverlay,
                FastColor.ABGR32.red(argb32), FastColor.ABGR32.green(argb32), FastColor.ABGR32.blue(argb32), FastColor.ABGR32.alpha(argb32));
    }
}
