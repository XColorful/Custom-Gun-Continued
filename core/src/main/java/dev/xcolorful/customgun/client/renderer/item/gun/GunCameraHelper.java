/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.item.gun;

import dev.xcolorful.customgun.client.api.entity.shooter.ILocalShooterGetter;
import dev.xcolorful.customgun.client.api.event.IComputeCameraAnglesEvent;
import dev.xcolorful.customgun.client.api.event.IComputeFovEvent;
import dev.xcolorful.customgun.client.api.event.IComputeFovModifierEvent;
import dev.xcolorful.customgun.client.api.event.render.BeforeRenderHandEvent;
import dev.xcolorful.customgun.client.api.item.IAnimateGeoItem;
import dev.xcolorful.customgun.client.api.renderer.KeepingItemRenderer;
import dev.xcolorful.customgun.client.api.renderer.item.IAnimateGeoItemRenderer;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.compat.shouldersurfing.ShoulderSurfingCompat;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.mixin.renderer.GameRendererMixin;
import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShootState;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import dev.xcolorful.customgun.core.api.event.*;
import dev.xcolorful.customgun.core.api.event.gun.GunFireEvent;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentNBTAccessor;
import dev.xcolorful.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.item.gun.modifier.IRecoilDataModifier;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterAim;
import dev.xcolorful.customgun.core.item.attachment.modifier.RecoilDataModifier;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._RecoilDataModifierData;
import dev.xcolorful.customgun.core.resource.data.data.gun._RecoilData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import dev.xcolorful.customgun.core.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GunCameraHelper implements IEventHandler {
    private static class GunCameraHelperHolder {
        private static final GunCameraHelper INSTANCE = new GunCameraHelper();
    }
    public static GunCameraHelper get() {
        return GunCameraHelperHolder.INSTANCE;
    }
    protected GunCameraHelper() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case COMPUTE_CAMERA_ANGLES_EVENT -> {
                onComputeCameraAngles((IComputeCameraAnglesEvent) event);
            }
            case COMPUTE_FOV_EVENT -> {
                onComputeFovEvent((IComputeFovEvent) event);
            }
            case COMPUTE_FOV_MODIFIER_EVENT -> {
                onComputeFovModifierEvent((IComputeFovModifierEvent) event);
            }
            default -> {
                onReceiveWrongEvent(eventType);
            }
        }
    }

    /**
     * 默认MC同时只有一个camera，用于平滑 FOV 变化
     */
    public static class State {
        public static final MathUtil.SecondOrderDynamics WORLD_FOV_DYNAMICS = new MathUtil.SecondOrderDynamics(0.5f, 1.2f, 0.5f, 0);
        public static final MathUtil.SecondOrderDynamics ITEM_MODEL_FOV_DYNAMICS = new MathUtil.SecondOrderDynamics(0.5f, 1.2f, 0.5f, 0);
        /**
         * 由{@link GameRendererMixin}设置
         */
        @ApiStatus.Internal
        public static boolean renderItemInHand = false;
    }
    private static PolynomialSplineFunction pitchSplineFunction;
    private static PolynomialSplineFunction yawSplineFunction;
    private static long lastGunFireTimestamp = -1L;
    private static double xRotO = 0;
    private static double yRotO = 0;

    private void onComputeCameraAngles(IComputeCameraAnglesEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (
                // 设置没开视角摇晃 (玩家移动时的镜头晃动)
                !mc.options.bobView().get()
                // 没有玩家？
                || mc.player == null
        ) return;

        this._applyLevelCameraAnimation(event, mc.player);
        this._applyCameraRecoil(event);
    }
    private void _applyLevelCameraAnimation(IComputeCameraAnglesEvent event, LocalPlayer player) {
        ItemStack currentItem = KeepingItemRenderer.cgc$getRenderer().cgc$getCurrentItem();

        // 尝试调用物品的自定义相机动画
        @Nullable IAnimateGeoItemRenderer<?, ?> renderer = IAnimateGeoItem.cgc$getCustomRenderer(currentItem);
        if (renderer == null) return;

        renderer.applyLevelCameraAnimation(event, currentItem, player);
    }
    private void _applyCameraRecoil(IComputeCameraAnglesEvent event) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        long timeTotal = System.currentTimeMillis() - lastGunFireTimestamp;
        boolean isShoulderSurfing = ShoulderSurfingCompat.isShoulderSurfing();

        // 垂直后坐
        if (pitchSplineFunction != null && pitchSplineFunction.isValidPoint(timeTotal)) {
            double value = pitchSplineFunction.value(timeTotal);
            if (isShoulderSurfing) {
                ShoulderSurfingCompat.setXRot(ShoulderSurfingCompat.getXRot() - (float) (value - xRotO));
            } else {
                localPlayer.setXRot(localPlayer.getXRot() - (float) (value - xRotO));
            }
            xRotO = value;
        }

        // 水平后坐
        if (yawSplineFunction != null && yawSplineFunction.isValidPoint(timeTotal)) {
            double value = yawSplineFunction.value(timeTotal);
            if (isShoulderSurfing) {
                ShoulderSurfingCompat.setXRot(ShoulderSurfingCompat.getYRot() - (float) (value - yRotO));
            } else {
                localPlayer.setXRot(localPlayer.getYRot() - (float) (value - yRotO));
            }
            yRotO = value;
        }
    }

    private void onComputeFovEvent(IComputeFovEvent event) {
        if (_isLevelRenderFov(event)) {
            // 改世界渲染FOV
            this._applyScopeMagnification(event);
        } else {
            // 改手部渲染FOV
            this._applyGunModelFovModifying(event);
        }
    }
    /**
     * 判断是否是世界渲染的 FOV，反之则是手部渲染 FOV 事件
     */
    private boolean _isLevelRenderFov(IComputeFovEvent event) {
        boolean result; {
            // [1.20.1, 26.1)
            result = !State.renderItemInHand;

            // [26.1, 26.2)
//            result = Boolean.FALSE.equals(event.useConfiguredFov());
        }
        return result;
    }
    private void _applyScopeMagnification(IComputeFovEvent event) {
        Entity entity = ClientRenderUtils.getEntity(event.getCamera());
        if (!(entity instanceof LivingEntity livingEntity)) return;

        ItemStack gunItem = KeepingItemRenderer.cgc$getRenderer().cgc$getCurrentItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            float fov = State.WORLD_FOV_DYNAMICS.update_tick_get(event.getFOV());
            event.setFOV(fov);
            return;
        }

        float scopeZoomScale = iGun.getScopeZoomScale(gunItem);
        float aimingProgress = livingEntity instanceof LocalPlayer localPlayer
                ? ILocalShooterGetter.fromLocalPlayer(localPlayer).cgc$getRenderAimingProgress((float) event.getPartialTick())
                : ILivingShooterGetter.cgc$fromLivingEntity(livingEntity).cgc$getSynAimingProgress();

        float fov = State.WORLD_FOV_DYNAMICS.update_tick_get((float) MathUtil.magnificationToFov(1 + (scopeZoomScale - 1) * aimingProgress, event.getFOV()));
        event.setFOV(fov);
    }
    private void _applyGunModelFovModifying(IComputeFovEvent event) {
        Entity entity = ClientRenderUtils.getEntity(event.getCamera());
        if (!(entity instanceof LivingEntity livingEntity)) return;

        ItemStack gunItem = KeepingItemRenderer.cgc$getRenderer().cgc$getCurrentItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            float fov = State.ITEM_MODEL_FOV_DYNAMICS.update_tick_get(event.getFOV());
            event.setFOV(fov);
            return;
        }

        var scopeLocation = iGun.getAttachmentLocation(gunItem, AttachmentCategory.SCOPE);
        if (ResourceTag.NULL_LOCATION.equals(scopeLocation)) {
            scopeLocation = iGun.getBuiltinAttachmentLocation(gunItem, AttachmentCategory.SCOPE);
        }

        @Nullable CompoundTag scopeCustomDataTag = iGun.getAttachmentCustomDataTag(gunItem, AttachmentCategory.SCOPE);
        int scopeViewIndex = AttachmentNBTAccessor.INSTANCE.getScopeViewIndex(scopeCustomDataTag);
        if (scopeViewIndex < 0) {
            if (PlannedRefactor.MOVE_SCOPE_VIEW_INDEX_TO_CORE) {}
            scopeViewIndex = 0;
        }

        float checkedFov = event.getFOV();
        { // 尝试使用配件fov修改，若无则尝试使用枪械本身fov修改，否则维持不变
            @Nullable ClientAttachmentIndexInstance clientAttachmentIndexInstance = ClientResourceApi.getClientAttachmentIndexInstance(scopeLocation);
            if (clientAttachmentIndexInstance != null) {
                // 尝试使用配件fov修改
                /**
                 * scopeViewIndex会超过 pojo 列表长度的原因见{@link LivingShooterAim#_doZoom}
                 */
                { // scopeViewFov获取
                    AttachmentDisplay attachmentDisplay = clientAttachmentIndexInstance.getAttachmentDisplay();
                    float @Nullable [] scopeViewFovList = attachmentDisplay.getScopeViewFov();
                    if (scopeViewFovList != null) {
                        /**
                         * {@link ClientAttachmentIndexInstance#checkScopeViewFov}不保证列表长度不为0
                         * {@link ClientAttachmentIndexInstance#checkScopeLengthMatch}保证scopeViewIndex和scopeViewFov长度相同
                         */
                        checkedFov = scopeViewFovList.length != 0 ? scopeViewFovList[scopeViewIndex % scopeViewFovList.length] : 0;
                    }
                }
            } else {
                // 若无则尝试使用枪械本身机瞄fov修改
                @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
                if (gunDisplayInstance != null) {
                    GunDisplay gunDisplay = gunDisplayInstance.getPojo();
                    checkedFov = gunDisplay.getIronViewFov();
                }
            }
        }

        float aimingProgress = livingEntity instanceof LocalPlayer localPlayer
                ? ILocalShooterGetter.fromLocalPlayer(localPlayer).cgc$getRenderAimingProgress((float) event.getPartialTick())
                : ILivingShooterGetter.cgc$fromLivingEntity(livingEntity).cgc$getSynAimingProgress();
        float fov = State.ITEM_MODEL_FOV_DYNAMICS.update_tick_get(Mth.lerp(aimingProgress, event.getFOV(), checkedFov));
        event.setFOV(fov);
    }

    private void onComputeFovModifierEvent(IComputeFovModifierEvent event) {
        if (!RenderConfig.DISABLE_MOVEMENT_ATTRIBUTE_FOV.get()) return;

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        ItemStack gunItem = localPlayer.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        float modifier = 1.0f;
        if (localPlayer.getAbilities().flying) {
            modifier *= 1.1f;
        }
        if (localPlayer.isSprinting()) {
            modifier *= 1.15f;
        }

        event.setNewFovModifier(modifier);
    }

    public static class Addon implements ICustomEventHandler {
        private static class AddonHolder {
            private static final Addon INSTANCE = new Addon();
        }
        public static Addon get() {
            return AddonHolder.INSTANCE;
        }
        protected Addon() {}
        @Override public String getEventHandlerName() {
            return this.getClass().getName();
        }
        @Override
        public void handleEvent(CustomEventType eventType, ICustomEvent event) {
            switch (eventType) {
                case GUN_FIRE_EVENT -> {
                    onGunFire((GunFireEvent) event);
                }
                case BEFORE_RENDER_HAND_EVENT -> {
                    onBeforeRenderHand((BeforeRenderHandEvent) event);
                }
                default -> {
                    onReceiveWrongEvent(eventType);
                }
            }
        }

        private void onGunFire(GunFireEvent event) {
            if (event.getLogicalSide().isServer()) return;

            @Nullable LivingEntity livingShooter = event.getLivingShooter();
            if (livingShooter == null) return;

            LocalPlayer localPlayer = Minecraft.getInstance().player;
            if (!livingShooter.equals(localPlayer)) return;

            @Nullable IGun iGun = event.getIGun();
            if (iGun == null) return;

            @Nullable ILivingShooter iLivingShooter = event.getILivingShooter();
            if (iLivingShooter == null) return;

            @Nullable ShooterGunModifierCache shooterGunModifierCache = iLivingShooter.cgc$getGunModifierCache();
            if (shooterGunModifierCache == null) return;

            ItemStack gunItem = event.getGunItem();
            var gunLocation = iGun.getGunLocation(gunItem);
            @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
            if (gunIndexInstance == null) return;

            GunData gunData = gunIndexInstance.getGunData();
            this._initCameraRecoil(shooterGunModifierCache, iLivingShooter, livingShooter, iGun, gunItem, localPlayer, gunData);
        }
        private void _initCameraRecoil(@NotNull ShooterGunModifierCache shooterGunModifierCache,
                                       @NotNull ILivingShooter iLivingShooter, @NotNull LivingEntity livingShooter,
                                       @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                       @NotNull LocalPlayer localPlayer,
                                       @NotNull GunData gunData) {
            float aimingRecoilModifier = 1; {
                /*
                以下是高倍镜减后坐，这个一般来说不需要
                并且原模组默认的后坐力非常小，几乎没用
                 */
//                ILocalShooter iLocalShooter = ILocalShooterGetter.fromLocalPlayer(localPlayer);
//                float partialTicks = ClientRenderUtils.getRenderFrameTime();
//                float aimingProgress = iLocalShooter.cgc$getRenderAimingProgress(partialTicks);
//                float scopeZoomScale = iGun.getScopeZoomScale(gunItem);
//                // ↓根据开镜进度，对高倍镜减后坐
//                aimingRecoilModifier = 1 - aimingProgress + aimingProgress / (float) Math.min(Math.sqrt(scopeZoomScale), 1.5);
            }

            ShootState shootState = ShootState.fromLivingShooter(iLivingShooter, livingShooter);
            switch (shootState) {
                case SNEAK -> {}
                case PRONE -> aimingRecoilModifier *= gunData.getProneRecoilMultiplier();
            }

            _RecoilData modifiedRecoilData; {
                // 获取所有配件对摄像机后坐力的修改
                List<_RecoilDataModifierData> modifiers = new ArrayList<>();
                @Nullable _RecoilDataModifierData recoilDataModifierData = IRecoilDataModifier.getValue(shooterGunModifierCache, AttachmentModifierType.RECOIL_DATA);
                if (recoilDataModifierData != null) modifiers.add(recoilDataModifierData);
                _RecoilData recoilData = gunData.getRecoilData();
                modifiedRecoilData = RecoilDataModifier.INSTANCE.eval(modifiers, recoilData);
            }

            pitchSplineFunction = GunRecoilCalculator.getSplineFunction(modifiedRecoilData.getPitchRecoils(), aimingRecoilModifier);
            yawSplineFunction = GunRecoilCalculator.getSplineFunction(modifiedRecoilData.getYawRecoils(), aimingRecoilModifier);
            lastGunFireTimestamp = System.currentTimeMillis();
            xRotO = 0;
            yRotO = 0;
        }

        private void onBeforeRenderHand(BeforeRenderHandEvent event) {
            Minecraft mc = Minecraft.getInstance();
            if (
                // 设置没开视角摇晃 (玩家移动时的镜头晃动)
                    !mc.options.bobView().get()
                    // 没有玩家？
                    || mc.player == null
            ) return;

            this._applyItemInHandCameraAnimation(event, mc.player);
        }
        private void _applyItemInHandCameraAnimation(BeforeRenderHandEvent event, LocalPlayer player) {
            ItemStack currentItem = KeepingItemRenderer.cgc$getRenderer().cgc$getCurrentItem();

            // 尝试调用物品的自定义相机动画
            @Nullable IAnimateGeoItemRenderer<?, ?> renderer = IAnimateGeoItem.cgc$getCustomRenderer(currentItem);
            if (renderer == null) return;

            renderer.applyItemInHandCameraAnimation(event, currentItem, player);
        }
    }
}
