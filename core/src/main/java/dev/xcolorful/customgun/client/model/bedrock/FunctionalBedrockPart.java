/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model.bedrock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * visible的优先级低于{@link FunctionalBedrockPart}，当visible为false的时候，仍然会执行functionalRenderers
 */
public class FunctionalBedrockPart extends BedrockPart {

    public @Nullable Function<BedrockPart, IModelComponentRenderer> functionalRenderer;

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
        int cubePackedLight = this.illuminated ? ClientRenderUtils.LightTexture_.pack(15, 15)
                : light;

        poseStack.pushPose(); {
            this.translate_rotate_scale(poseStack);

            boolean rendered = false;
            if (this.functionalRenderer != null) {
                @Nullable IModelComponentRenderer renderer = this.functionalRenderer.apply(this);
                if (renderer != null) {
                    renderer.render(poseStack,
                            consumer,
                            transformType,
                            cubePackedLight, overlay);
                    rendered = true;
                }
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


    // --------Deprecated--------

    @Deprecated(forRemoval = true)
    public FunctionalBedrockPart(@Nullable Function<BedrockPart, IModelComponentRenderer> functionalRenderer, @NotNull String name) {
        this(name, functionalRenderer);
    }
}
