/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.compat.ar.BeamRenderAR;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.init.registry.ClientRenderRegistry;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.client.resource.assets.display._LaserDisplay;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class BeamRender {

    private static final _LaserDisplay DEFAULT_LASER_DISPLAY;
    static {
        _LaserDisplay laserDisplay = new _LaserDisplay()
                .applyBackCompatibility();
        laserDisplay.validate();
        if (!laserDisplay.isValid()) throw new IllegalStateException("BeamRender: laser display is invalid");
        DEFAULT_LASER_DISPLAY = laserDisplay;
    }

    public static void render(PoseStack poseStack,
                              ItemDisplayContext transformType,
                              @NotNull List<BedrockPart> path,
                              ItemStack pojoItem) {
        if (pojoItem == null
                || (!transformType.firstPerson() && transformType != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) return;

        if (BeamRenderAR.render(poseStack, transformType, path, pojoItem)) return;

        @Nullable Object collector = ClientRenderHelper.FirstPersonArmHelper.getFirstPersonArmCollector();
        if (collector == null) return;

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer builder = bufferSource.getBuffer(ClientRenderRegistry.LaserBeamRenderState.getLaserBeam());
        poseStack.pushPose(); {
            for (int i = 0; i < path.size(); ++i) {
                path.get(i).translate_rotate_scale(poseStack);
            }

            final _LaserDisplay finalLaserDisplay;
            final int finalColor;
            { // 获取激光display
                final _LaserDisplay[] laserDisplay = new _LaserDisplay[1];
                final Integer[] color = new Integer[1];
                _getLaser(pojoItem, (_laserDisplay) -> laserDisplay[0] = _laserDisplay, (_laserColor) -> color[0] = _laserColor);
                finalLaserDisplay = laserDisplay[0] != null ? laserDisplay[0] : DEFAULT_LASER_DISPLAY;
                finalColor = color[0] != null ? color[0] : finalLaserDisplay.getDefaultColor().getRGB();
            }

            int r = (finalColor >> 16) & 0xFF;
            int g = (finalColor >> 8) & 0xFF;
            int b = finalColor & 0xFF;
            float z = transformType.firstPerson() ? -finalLaserDisplay.getLaserLength() : -finalLaserDisplay.getThirdPersonLaserLength();
            float width = transformType.firstPerson() ? finalLaserDisplay.getLaserWidth() : finalLaserDisplay.getThirdPersonLaserWidth();

            {
                _stringVertex(builder, poseStack.last(), z, width, r, g, b, RenderConfig.ENABLE_LASER_FADE_OUT.get());
            }
        }
        poseStack.popPose();
    }

    private static void _stringVertex(VertexConsumer pConsumer,
                                      PoseStack.Pose pPose,
                                      float z, float width,
                                      int r, int g, int b,
                                      boolean fadeOut) {
        float halfWidth = width / 2;
        int endAlpha = fadeOut ? 0 : 255;
        int light = ClientRenderUtils.LightTexture_.pack(15, 15);
        pConsumer.addVertex(pPose.pose(), -halfWidth, -halfWidth, 0).setColor(r, g, b, 255).setUv(0, 0).setLight(light);
        pConsumer.addVertex(pPose.pose(), -halfWidth, halfWidth, 0).setColor(r, g, b, 255).setUv(0, 1).setLight(light);
        pConsumer.addVertex(pPose.pose(), -halfWidth, halfWidth, z).setColor(r, g, b, endAlpha).setUv(1, 1).setLight(light);
        pConsumer.addVertex(pPose.pose(), -halfWidth, -halfWidth, z).setColor(r, g, b, endAlpha).setUv(1, 0).setLight(light);

        pConsumer.addVertex(pPose.pose(), -halfWidth, halfWidth, 0).setColor(r, g, b, 255).setUv(0, 0).setLight(light);
        pConsumer.addVertex(pPose.pose(), halfWidth, halfWidth, 0).setColor(r, g, b, 255).setUv(0, 1).setLight(light);
        pConsumer.addVertex(pPose.pose(), halfWidth, halfWidth, z).setColor(r, g, b, endAlpha).setUv(1, 1).setLight(light);
        pConsumer.addVertex(pPose.pose(), -halfWidth, halfWidth, z).setColor(r, g, b, endAlpha).setUv(1, 0).setLight(light);

        pConsumer.addVertex(pPose.pose(), halfWidth, halfWidth, 0).setColor(r, g, b, 255).setUv(0, 0).setLight(light);
        pConsumer.addVertex(pPose.pose(), halfWidth, -halfWidth, 0).setColor(r, g, b, 255).setUv(0, 1).setLight(light);
        pConsumer.addVertex(pPose.pose(), halfWidth, -halfWidth, z).setColor(r, g, b, endAlpha).setUv(1, 1).setLight(light);
        pConsumer.addVertex(pPose.pose(), halfWidth, halfWidth, z).setColor(r, g, b, endAlpha).setUv(1, 0).setLight(light);

        pConsumer.addVertex(pPose.pose(), halfWidth, -halfWidth, 0).setColor(r, g, b, 255).setUv(0, 1).setLight(light);
        pConsumer.addVertex(pPose.pose(), -halfWidth, -halfWidth, 0).setColor(r, g, b, 255).setUv(0, 1).setLight(light);
        pConsumer.addVertex(pPose.pose(), -halfWidth, -halfWidth, z).setColor(r, g, b, endAlpha).setUv(1, 1).setLight(light);
        pConsumer.addVertex(pPose.pose(), halfWidth, -halfWidth, z).setColor(r, g, b, endAlpha).setUv(1, 0).setLight(light);
    }

    // public仅用于文档链接
    @ApiStatus.Internal
    public static void _getLaser(ItemStack pojoItem, Consumer<_LaserDisplay> setLaserDisplay, Consumer<Integer> setLaserColor) {
        if (pojoItem == null) return;

        { // 配件激光
            @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(pojoItem);
            if (iAttachment != null) {
                // 激光颜色
                if (iAttachment.hasLaserColor(pojoItem)) {
                    setLaserColor.accept(iAttachment.getLaserColorInt(pojoItem));
                }

                var attachmentLocation = iAttachment.getAttachmentLocation(pojoItem);
                @Nullable ClientAttachmentIndexInstance clientAttachmentIndexInstance = ClientResourceApi.getClientAttachmentIndexInstance(attachmentLocation);
                if (clientAttachmentIndexInstance == null) return;

                AttachmentDisplay attachmentDisplay = clientAttachmentIndexInstance.getAttachmentDisplay();
                @Nullable _LaserDisplay laserDisplay = attachmentDisplay.getLaserDisplay();
                setLaserDisplay.accept(laserDisplay);
                return;
            }
        }

        { // 枪械激光
            @Nullable IGun iGun = IGunGetter.fromItemStack(pojoItem);
            if (iGun != null) {
                // 激光颜色
                if (iGun.hasLaserColor(pojoItem)) {
                    setLaserColor.accept(iGun.getLaserColorInt(pojoItem));
                }

                @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(pojoItem);
                if (gunDisplayInstance == null) return;

                GunDisplay gunDisplay = gunDisplayInstance.getPojo();
                @Nullable _LaserDisplay laserDisplay = gunDisplay.getLaserDisplay();
                setLaserDisplay.accept(laserDisplay);
                return;
            }
        }
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true) private static _LaserDisplay getLaserConfig(ItemStack stack) {
        return DEFAULT_LASER_DISPLAY;
    }
}
