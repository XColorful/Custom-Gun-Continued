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
import dev.xcolorful.customgun.client.util.ClientEntityUtils;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.entity.projectile.GunProjectile;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.awt.*;

public class GunProjectileRenderer extends EntityRenderer<GunProjectile> {

    public GunProjectileRenderer(EntityRendererProvider.Context providerContext) {
        super(providerContext);
    }

    private static final @NotNull ModelObject modelObject = GunProjectileModel.createModelObject();

    @ApiStatus.Internal
    public static @NotNull ModelObject getModel() {
        return modelObject;
    }

    // --------EntityRenderer--------

    /**
     * <ul>
     *     跳过渲染距离判定，只做视锥体裁剪
     *     <li>原版 {@code Entity#shouldRenderAtSqrDistance} 用 碰撞箱尺寸*64 作为渲染距离上限</li>
     *     <li>枪射物碰撞箱极小，会被限制在很近距离，曳光弹就永远画不出来</li>
     * </ul>
     */
    @Override
    public boolean shouldRender(@NotNull GunProjectile gunProjectile,
                                @NotNull Frustum frustum,
                                double camX, double camY, double camZ) {
        AABB aabb = ClientEntityUtils.getBoundingBoxForCulling(gunProjectile).inflate(0.5);
        if (aabb.hasNaN() || aabb.getSize() == 0.0) {
            aabb = new AABB(gunProjectile.getX() - 2.0, gunProjectile.getY() - 2.0, gunProjectile.getZ() - 2.0,
                    gunProjectile.getX() + 2.0, gunProjectile.getY() + 2.0, gunProjectile.getZ() + 2.0);
        }
        return frustum.isVisible(aabb);
    }

    @ApiStatus.AvailableSince("1.21.4")
//    @Override
    public @NotNull GunProjectileRenderer.State createRenderState() {
        return new GunProjectileRenderer.State();
    }

    @ApiStatus.AvailableSince("1.21.4")
//    @Override
    public void extractRenderState(@NotNull GunProjectile gunProjectile,
                                   @NotNull GunProjectileRenderer.State state,
                                   float partialTicks) {
//        super.extractRenderState(gunProjectile, state, partialTicks);
        state.gunProjectile = gunProjectile;
    }

    @ApiStatus.AvailableSince("1.21.10")
//    @Override
    public void submit(@NotNull GunProjectileRenderer.State state,
                       @NotNull PoseStack poseStack,
                       @NotNull Object submitNodeCollector,
                       @NotNull Object cameraRenderState) {
//        super.submit(state, poseStack, submitNodeCollector, cameraRenderState);
        if (state.gunProjectile == null) return;

        ClientRenderHelper.FirstPersonArmHelper.setFirstPersonArmCollector(submitNodeCollector);
        try {
//            this.render(state,
//                    poseStack,
//                    Minecraft.getInstance().renderBuffers().bufferSource(),
//                    state.lightCoords);
        } finally {
            ClientRenderHelper.FirstPersonArmHelper.setFirstPersonArmCollector(null);
        }
    }

    @Override
    public void render( // @NotNull GunProjectileRenderer.State renderState,
                       @NotNull GunProjectile gunProjectile,
                       float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer,
                       int packedLight) {
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
                if (color == null) {
                    @Nullable ClientAmmoIndexInstance clientAmmoIndexInstance = iClientGunProjectile.cgc$getClientAmmoIndexInstanceCache();
                    if (clientAmmoIndexInstance != null) {
                        AmmoDisplay ammoDisplay = clientAmmoIndexInstance.getAmmoDisplay();
                        color = ammoDisplay.getTracerColor();
                    }
                }
                if (color == null) color = Color.WHITE;
                tracerColor = _toTracerColor(color);
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

        poseStack.pushPose(); {
            poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw - 180.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(entityPitch));
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
        if (
                // 前2tick(100ms)要是飞不出两格，就强制渲染
                gunProjectile.tickCount > 2
                // 距离两格内
                || bulletPosition.distanceTo(livingShooter.getEyePosition()) < 2
        ) return;

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
                Camera camera = ClientRenderUtils.getMainCamera(Minecraft.getInstance());
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

                /**
                 * <ul>
                 *     1.21.1起不再做摄像机旋转的还原/重放
                 *     <li>muzzleRenderOffset 是在枪械渲染的 poseStack 上采集的，而那个 poseStack 里已经含有摄像机的 view 变换</li>
                 *     <li>1.20.1 的摄像机约定是 Qx(+pitch) · Qy(-180-yaw) // GameRenderer 里 XP 后 YP 两次 mulPose</li>
                 *     <li>而 1.21.1 换成了 Qy(180-yaw) · Qx(-pitch) // Camera.rotation 整体 conjugate 后一次 mulPose</li>
                 *     <li>俯仰符号与合成顺序都变了，于是「先 YN 再 XN 还原、再 XP/YP 重放」这套写法在 1.20.1 上成立</li>
                 *     <li>而在 1.21.1 上会变成二次旋转: yaw=0 时两次旋转的偏航部分互相抵消，yaw=±90 时抵消不掉，偏移被转成横向，曳光弹起点就横移出屏幕</li>
                 *     <li>实测：这段旋转去掉后 1.21.1 正常，加上则偏移复现（1.20.1 恰好相反）</li>
                 * </ul>
                 */
                // 摄像机旋转
                poseStack.mulPose(Axis.YN.rotationDegrees(iClientGunProjectile.cgc$getCameraYRot() + 180f));
                poseStack.mulPose(Axis.XN.rotationDegrees(iClientGunProjectile.cgc$getCameraXRot()));
                {
                    // 应用偏移
                    poseStack.translate(offset[0] * offsetReducer, offset[1] * offsetReducer, offset[2] * offsetReducer);
                }
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

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull GunProjectile gunProjectile) {
        return CustomTexture.GUN_PROJECTILE.getLocation();
    }

    /**
     * {@link Color} 的通道是 0~255，而渲染的顶点色需要 0~1；
     * 直接按 0~255 传会被量化成接近 0 的 alpha，曳光弹着色器会因 {@code alpha < 0.1} 而 discard。
     */
    private static float @NotNull [] _toTracerColor(@NotNull Color color) {
        return new float[]{
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f};
    }

    @ApiStatus.AvailableSince("1.21.4")
    public static class State {
        private GunProjectile gunProjectile;

        public State() {
        }
    }
}
