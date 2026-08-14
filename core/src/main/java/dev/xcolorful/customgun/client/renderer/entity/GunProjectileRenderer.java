/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xcolorful.customgun.client.api.entity.IClientGunProjectile;
import dev.xcolorful.customgun.client.api.entity.projectile.IClientGunProjectileGetter;
import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.api.resource.assets.model.GunProjectileModel;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.model.AmmoModelObject;
import dev.xcolorful.customgun.client.model.ModelObject;
import dev.xcolorful.customgun.client.renderer.item.GunItemRenderer;
import dev.xcolorful.customgun.client.resource.assets.display.AmmoDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.ammo._AmmoEntityDisplay;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAmmoIndexInstance;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.entity.projectile.GunProjectile;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.awt.*;

public class GunProjectileRenderer extends EntityRenderer<GunProjectile, GunProjectileRenderer.State> {

    public GunProjectileRenderer(EntityRendererProvider.Context providerContext) {
        super(providerContext);
    }

    private static final @NotNull ModelObject modelObject = GunProjectileModel.createModelObject();

    @ApiStatus.Internal
    public static @NotNull ModelObject getModel() {
        return modelObject;
    }

    // --------EntityRenderer--------

    @ApiStatus.AvailableSince("1.21.4")
    @Override
    public @NotNull GunProjectileRenderer.State createRenderState() {
        return new GunProjectileRenderer.State();
    }

    @ApiStatus.AvailableSince("1.21.4")
    @Override
    public void extractRenderState(@NotNull GunProjectile gunProjectile,
                                   @NotNull GunProjectileRenderer.State state,
                                   float partialTicks) {
        super.extractRenderState(gunProjectile, state, partialTicks);
        state.gunProjectile = gunProjectile;
    }

    @ApiStatus.AvailableSince("1.21.10")
    @Override
    public void submit(@NotNull GunProjectileRenderer.State state,
                       @NotNull PoseStack poseStack,
                       @NotNull SubmitNodeCollector submitNodeCollector,
                       @NotNull CameraRenderState cameraRenderState) {
        super.submit(state, poseStack, submitNodeCollector, cameraRenderState);
        if (state.gunProjectile == null) return;

        this.render(state,
                poseStack,
                Minecraft.getInstance().renderBuffers().bufferSource(),
                state.lightCoords);
    }

//    @Override
    public void render(@NotNull GunProjectileRenderer.State renderState,
//                       @NotNull GunProjectile gunProjectile,
//                       float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer,
                       int packedLight) {
        GunProjectile gunProjectile = renderState.gunProjectile;
        float partialTicks = renderState.partialTick;
        float entityYaw = Mth.lerp(partialTicks, gunProjectile.yRotO, gunProjectile.getYRot());
        float entityPitch = Mth.lerp(partialTicks, gunProjectile.xRotO, gunProjectile.getXRot());

        IClientGunProjectile iClientGunProjectile = IClientGunProjectileGetter.fromGunProjectile(gunProjectile);
        @Nullable GunDisplayInstance gunDisplayInstance = iClientGunProjectile.cgc$getClientGunDisplayInstanceCache();
        if (gunDisplayInstance == null) return;

        // 渲染子弹模型
        this._renderAmmoObject(poseStack, buffer, entityYaw, entityPitch, partialTicks, packedLight, gunProjectile);

        // 渲染曳光弹
        if (gunProjectile.getIsTracer(gunProjectile)) {
            float[] tracerColor; { // 曳光弹颜色{R,G,B,A}
                @Nullable Color color = gunDisplayInstance.getTracerColor();
                if (color != null) {
                    tracerColor = new float[]{color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()};
                } else {
                    @Nullable ClientAmmoIndexInstance clientAmmoIndexInstance = iClientGunProjectile.cgc$getClientAmmoIndexInstanceCache();
                    if (clientAmmoIndexInstance != null) {
                        AmmoDisplay ammoDisplay = clientAmmoIndexInstance.getAmmoDisplay();
                        color = ammoDisplay.getTracerColor();
                        tracerColor = new float[]{color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()};
                    } else {
                        color = Color.WHITE;
                        tracerColor = new float[]{color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()};
                    }
                }
            }

            this._renderTracer(poseStack, buffer, entityYaw, entityPitch, partialTicks, packedLight, iClientGunProjectile, gunProjectile, tracerColor);
        }
    }
    private void _renderAmmoObject(@NotNull PoseStack poseStack,
                                   @NotNull MultiBufferSource buffer,
                                   float entityYaw, float entityPitch, float partialTicks,
                                   int packedLight,
                                   @NotNull GunProjectile gunProjectile) {
        var ammoLocation = gunProjectile.getAmmoLocation(gunProjectile);
        @Nullable ClientAmmoIndexInstance clientAmmoIndexInstance = ClientResourceApi.getClientAmmoIndexInstance(ammoLocation);
        if (clientAmmoIndexInstance == null) return;

        @Nullable AmmoModelObject ammoEntityModelObject = clientAmmoIndexInstance.getAmmoEntityModel();
        if (ammoEntityModelObject == null) return;

        AmmoDisplay ammoDisplay = clientAmmoIndexInstance.getAmmoDisplay();
        @Nullable _AmmoEntityDisplay ammoEntityDisplay = ammoDisplay.getAmmoEntityDisplay();
        if (ammoEntityDisplay == null) return;

        @Nullable var textureLocation = ammoEntityDisplay.getTextureLocation();
        if (textureLocation == null) textureLocation = ClientRenderUtils.getMissingTextureLocation();

        poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw - 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(entityPitch));

        poseStack.pushPose(); {
            poseStack.translate(0, 1.5, 0);
            poseStack.scale(-1, -1, 1);
            ammoEntityModelObject.render(poseStack,
                    ItemDisplayContext.GROUND,
                    ClientRenderUtils.RenderType_.entityTranslucentCull(textureLocation),
                    packedLight,
                    OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }
    private void _renderTracer(@NotNull PoseStack poseStack,
                               @NotNull MultiBufferSource buffer,
                               float entityYaw, float entityPitch, float partialTicks,
                               int packedLight,
                               IClientGunProjectile iClientGunProjectile, @NotNull GunProjectile gunProjectile,
                               float @NotNull [] tracerColor) {
        Entity livingShooter = gunProjectile.getOwner();
        if (livingShooter == null) return;

        Vec3 bulletPosition = gunProjectile.getPosition(partialTicks);
        double bulletDistance = bulletPosition.distanceTo(livingShooter.getEyePosition());
        if (bulletDistance < 2) return; // 距离两格外才渲染

        boolean isFirstPerson = livingShooter instanceof LocalPlayer
                && this.entityRenderDispatcher.options.getCameraType().isFirstPerson();
        if (isFirstPerson && !RenderConfig.ENABLE_FIRST_PERSON_BULLET_TRACER.get()) return;

        poseStack.pushPose(); {
            float scale = 0.005f;
            double trailLength = 0.85 * gunProjectile.getDeltaMovement().length();
            double disToEye = bulletPosition.distanceTo(livingShooter.getEyePosition(partialTicks));
            trailLength = Math.min(trailLength, disToEye * 0.8);

            if (isFirstPerson) {
                // 第一人称渲染自己的曳光弹的时候需要应用偏移
                Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
                float @Nullable [] offset = iClientGunProjectile.cgc$getFirstPersonRenderOffset();
                if (offset == null) {
                    Vector3f _offset = GunItemRenderer.State.muzzleRenderOffset;
                    offset = new float[]{_offset.x, _offset.y, _offset.z};
                    iClientGunProjectile.cgc$setCameraXRot(ClientRenderUtils.getCameraXRot(camera));
                    iClientGunProjectile.cgc$setCameraYRot(ClientRenderUtils.getCameraYRot(camera));
                    iClientGunProjectile.cgc$setFirstPersonRenderOffset(offset);
                }

                // 按照生存时间减少曳光弹的偏移，避免渲染位置距离落点太远
                double offsetReducer = Math.max(0, (50 - disToEye)) / 50;

                // 摄像机旋转
                poseStack.mulPose(Axis.YN.rotationDegrees(iClientGunProjectile.cgc$getCameraYRot() + 180f));
                poseStack.mulPose(Axis.XN.rotationDegrees(iClientGunProjectile.cgc$getCameraXRot()));

                // 应用偏移
                poseStack.translate(offset[0] * offsetReducer, offset[1] * offsetReducer, offset[2] * offsetReducer);

                // 逆转摄像机旋转
                poseStack.mulPose(Axis.XP.rotationDegrees(iClientGunProjectile.cgc$getCameraXRot()));
                poseStack.mulPose(Axis.YP.rotationDegrees(iClientGunProjectile.cgc$getCameraYRot() + 180f));
            }

            // 说是 override 其实默认值是 1
            // 所以这里直接乘也没关系
            scale *= iClientGunProjectile.cgc$getTracerScaleModifier(gunProjectile);
            scale *= (float) Math.max(1.0, disToEye / 3.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw - 180.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(entityPitch));
            poseStack.translate(0, isFirstPerson ? 0 : -0.2, trailLength / 2.0);
            poseStack.scale(scale, scale, (float) trailLength);

            RenderType type = ClientRenderUtils.RenderType_.energySwirl(CustomTexture.GUN_PROJECTILE.getLocation(), 15, 15);
            modelObject.render(poseStack, ItemDisplayContext.NONE, type, packedLight, OverlayTexture.NO_OVERLAY,
                    tracerColor[0], tracerColor[1], tracerColor[2], tracerColor[3]);
        }
        poseStack.popPose();
    }

//    @Override
    public @NotNull Identifier getTextureLocation(@NotNull GunProjectile gunProjectile) {
        return CustomTexture.GUN_PROJECTILE.getLocation();
    }

    @ApiStatus.AvailableSince("1.21.4")
    public static class State extends EntityRenderState {
        private GunProjectile gunProjectile;

        public State() {
        }
    }
}
