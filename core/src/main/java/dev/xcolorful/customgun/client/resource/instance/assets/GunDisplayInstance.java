/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.instance.assets;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.animation.AnimationHelper;
import dev.xcolorful.customgun.client.animation.controller.AnimController;
import dev.xcolorful.customgun.client.animation.statemachine.GunAnimStateContext;
import dev.xcolorful.customgun.client.animation.statemachine.LuaAnimStateMachine;
import dev.xcolorful.customgun.client.api.item.gun.DamageDisplayType;
import dev.xcolorful.customgun.client.api.model.gun.GunModelType;
import dev.xcolorful.customgun.client.api.model.gun.IGunModelType;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.api.sound.gun.GunSoundType;
import dev.xcolorful.customgun.client.init.ClientModEvent;
import dev.xcolorful.customgun.client.model.GunModelObject;
import dev.xcolorful.customgun.client.resource.assets.animation.BedrockAnimation;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.client.resource.assets.display._LaserDisplay;
import dev.xcolorful.customgun.client.resource.assets.display._LodDisplay;
import dev.xcolorful.customgun.client.resource.assets.display._ModelTransform;
import dev.xcolorful.customgun.client.resource.assets.display.ammo._AmmoParticle;
import dev.xcolorful.customgun.client.resource.assets.display.gun.*;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.client.resource.assets.script.AssetsScript;
import dev.xcolorful.customgun.core.api.item.gun.AmmoCountType;
import dev.xcolorful.customgun.core.resource.instance.PojoInstance;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 经过处理和校验的枪械显示数据
 */
public final class GunDisplayInstance extends PojoInstance<GunDisplay> {

    private @Nullable GunModelObject gunModel;
    private @Nullable GunModelObject gunModelLod;

    private @Nullable Int2ObjectArrayMap<_SurroundDisplay> surroundDisplayByHotbarCache;
    private @Nullable Color tracerColorCache;
    private boolean ammoParticleLoaded = false;
    private @Nullable ParticleOptions ammoParticleOptionsCache;
    private Map<GunSoundType, Identifier> gunSoundsCache;

    /**
     * 状态机脚本
     */
    private LuaAnimStateMachine<GunAnimStateContext> animStateMachine = null;
    /**
     * 状态机脚本参数
     */
    private LuaTable animStateMachineParams = null;

    private GunDisplayInstance(@NotNull GunDisplay pojo) {
        super(pojo);
    }

    public static @Nullable GunDisplayInstance fromPojo(GunDisplay pojo) {
        if (pojo == null) return null;
        GunDisplayInstance instance = new GunDisplayInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        var pojo = this.getPojo();

        @Nullable IGunModelType gunModelType = pojo.getGunModelType();
        if (gunModelType == null) gunModelType = GunModelType.DEFAULT;
        @Nullable var modelLocation = pojo.getModelLocation();
        if (modelLocation != null) {
            @Nullable BedrockModel bedrockModel = ClientResourceApi.getBedrockModel(modelLocation);
            if (bedrockModel != null) {
                this.gunModel = gunModelType.create(bedrockModel);
                if (this.gunModel == null) CustomGun.LOGGER.debug("GunDisplayInstance: Failed to create GunModelObject {}", modelLocation);
            } else {
                CustomGun.LOGGER.debug("GunDisplayInstance: BedrockModel {} not found", modelLocation);
            }
        }
        @Nullable _LodDisplay lodDisplay = pojo.getLodDisplay();
        if (lodDisplay != null) {
            @Nullable var lodModelLocation = lodDisplay.getModelLocation();
            @Nullable BedrockModel bedrockModel = ClientResourceApi.getBedrockModel(lodModelLocation);
            if (bedrockModel != null) {
                this.gunModelLod = gunModelType.create(bedrockModel);
                if (this.gunModelLod == null) CustomGun.LOGGER.debug("GunDisplayInstance: Failed to create GunModelObject (for lod) {}", lodModelLocation);
            } else {
                CustomGun.LOGGER.debug("GunDisplayInstance: BedrockModel (for lod) {} not found", lodModelLocation);
            }
        }

        Map<String, _SurroundDisplay> surroundDisplayByHotbar = pojo.getSurroundDisplayByHotbar();
        if (surroundDisplayByHotbar != null) {
            this.surroundDisplayByHotbarCache = new Int2ObjectArrayMap<>();
            for (Map.Entry<String, _SurroundDisplay> entry : surroundDisplayByHotbar.entrySet()) {
                try {
                    this.surroundDisplayByHotbarCache.put(Integer.parseInt(entry.getKey()), entry.getValue());
                } catch (Exception ignored) {}
            }
        }

        this.reloadAmmoParticleOption();

        this.gunSoundsCache = pojo.getGunSounds();

        AnimController animController;
        { // 动画控制器
            animController = loadAnimController(this.gunModel);
            if (animController == null) return false;
        }

        { // 状态机脚本
            var scriptLocation = pojo.getScriptLocation();
            if (scriptLocation == null) {
                CustomGun.LOGGER.debug("GunDisplayInstance: GunDisplay missing scriptLocation");
                scriptLocation = GunDisplay.DEFAULT_SCRIPT_LOCATION;
            }
            AssetsScript assetsScript = ClientResourceApi.getAssetsScript(scriptLocation);
            if (assetsScript == null) {
                CustomGun.LOGGER.debug("GunDisplayInstance: AssetsScript {} not found", scriptLocation);
                return false;
            } else if (!assetsScript.isValid()) {
                CustomGun.LOGGER.debug("GunDisplayInstance: AssetsScript {} not valid", scriptLocation);
                return false;
            }
            this.animStateMachine = new LuaAnimStateMachine.Builder<GunAnimStateContext>()
                    .setController(animController)
                    .setLuaScripts(assetsScript.getResultTable())
                    .build();
        }

        // 加载状态机参数
        this.reloadScriptParams();

        return true;
    }
    private static final int ERR_TRANSFORM_SCALE = 1;
    private static final int ERR_AMMO_PARTICLE_COUNT = 1 << 1;
    private static final int ERR_AMMO_PARTICLE_LIFETIME = 1 << 2;
    private static final int ERR_IRON_ZOOM_SCALE = 1 << 3;
    private static final int ERR_IRON_VIEW_FOV = 1 << 4;
    @Override protected boolean isPojoValid() {
        if (!super.isPojoValid()) return false;

        var pojo = this.getPojo();
        @Nullable _AmmoParticle ammoParticle = this.getAmmoParticle();

        int errorMask = 0;
        // GunDisplay
        errorMask |= pojo.getModelTransform() == null || pojo.getModelTransform().getScale() == null ? ERR_TRANSFORM_SCALE : 0;
        errorMask |= (ammoParticle != null && ammoParticle.getCount() < 1) ? ERR_AMMO_PARTICLE_COUNT : 0;
        errorMask |= (ammoParticle != null && ammoParticle.getLifetimeTicks() < 1) ? ERR_AMMO_PARTICLE_LIFETIME : 0;
        errorMask |= pojo.getIronZoomScale() < 1 ? ERR_IRON_ZOOM_SCALE : 0;
        errorMask |= pojo.getIronViewFov() > 70 ? ERR_IRON_VIEW_FOV : 0;
        if (errorMask != 0) {
            this.logAllErrors(errorMask);
            return false;
        }

        return true;
    }
    @Override protected void logAllErrors(int errorMask) {
        StringBuilder sb = new StringBuilder("GunDisplayInstance: GunDisplay is invalid because:");
        if ((errorMask & ERR_TRANSFORM_SCALE) != 0) sb.append("\n\t- _ModelTransformScale is null");
        if ((errorMask & ERR_AMMO_PARTICLE_COUNT) != 0) sb.append("\n\t- _AmmoParticle count < 1");
        if ((errorMask & ERR_AMMO_PARTICLE_LIFETIME) != 0) sb.append("\n\t- _AmmoParticle lifetimeTicks < 1");
        if ((errorMask & ERR_IRON_ZOOM_SCALE) != 0) sb.append("\n\t- ironZoomScale < 1");
        if ((errorMask & ERR_IRON_VIEW_FOV) != 0) sb.append("\n\t- ironViewFov > 70");
        CustomGun.LOGGER.debug(sb.toString());
    }
    private @Nullable AnimController loadAnimController(GunModelObject gunModel) {
        var animationLocation = this.getPojo().getGunAnimationLocation();
        if (animationLocation != null) {
            @Nullable BedrockAnimation bedrockAnimation = ClientResourceApi.getBedrockAnimation(animationLocation);
            if (bedrockAnimation != null) {
                // 用 bedrock 动画资源创建动画控制器
                return AnimationHelper.createControllerFromBedrock(bedrockAnimation, gunModel);
            }
            // TODO glTF
            CustomGun.LOGGER.debug("GunDisplayInstance: Animation {} not found", animationLocation);
            return null;

            // TODO 将默认动画填入动画控制器?
        } else {
            return new AnimController(new ArrayList<>(), gunModel);
        }
    }
    private void reloadAmmoParticleOption() {
        if (!ClientModEvent.get().isLoggedOn()) return;
        this.ammoParticleLoaded = true;

        var pojo = this.getPojo();
        _AmmoDisplayOverride ammoDisplayOverride = pojo.getAmmoDisplayOverride();
        if (ammoDisplayOverride != null) {
            this.tracerColorCache = ammoDisplayOverride.getTracerColor();
            @Nullable _AmmoParticle ammoParticle = ammoDisplayOverride.getAmmoParticle();
            if (ammoParticle != null) {
                var particleRl = ammoParticle.getParticleLocation();
                try {
                    this.ammoParticleOptionsCache = ParticleArgument.readParticle(new StringReader(particleRl.toString()), CustomGun.getRegistryAccess());
                } catch (CommandSyntaxException e) {
                    CustomGun.LOGGER.debug("GunDisplayInstance: ParticleArgument.readParticle({}) failed", particleRl, e);
                }
                if (this.ammoParticleOptionsCache == null) {
                    CustomGun.LOGGER.debug("GunDisplayInstance: AmmoParticle {} not valid", particleRl);
                }
            }
        }
    }
    private void reloadScriptParams() {
        this.animStateMachineParams = new LuaTable();

        Map<String, Object> params = this.getPojo().getScriptParam();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                this.animStateMachineParams.set(entry.getKey(), CoerceJavaToLua.coerce(entry.getValue()));
            }
        }
    }

    // --------Getter--------

    public @Nullable GunModelObject getGunModel() {
        return this.gunModel;
    }
    public @Nullable GunModelObject getGunModelLod() {
        return this.gunModelLod;
    }

    public @Nullable Int2ObjectArrayMap<_SurroundDisplay> getSurroundDisplayByHotbar() {
        return this.surroundDisplayByHotbarCache;
    }
    public @Nullable Color getTracerColor() {
        return this.tracerColorCache;
    }
    public @Nullable _AmmoParticle getAmmoParticle() {
        @Nullable _AmmoDisplayOverride ammoDisplayOverride = this.getPojo().getAmmoDisplayOverride();
        return ammoDisplayOverride != null ? ammoDisplayOverride.getAmmoParticle() : null;
    }
    public @Nullable ParticleOptions getParticleOptions() {
        if (!this.ammoParticleLoaded) this.reloadAmmoParticleOption();
        return this.ammoParticleOptionsCache;
    }
    public @Nullable Identifier getGunSound(GunSoundType gunSoundType) {
        return this.gunSoundsCache.get(gunSoundType);
    }
    public LuaAnimStateMachine<GunAnimStateContext> getAnimStateMachine() {
        return this.animStateMachine;
    }
    public LuaTable getAnimStateMachineParams() {
        return this.animStateMachineParams;
    }

    // --------Deprecated--------

    @Deprecated public @Nullable _ModelTransform getTransform() {
        return this.getPojo().getModelTransform();
    }
    @Deprecated public @Nullable Identifier getModelTexture() {
        return this.getPojo().getTextureLocation();
    }
    @Deprecated public Identifier getSlotTexture() {
        return this.getPojo().getSlotTextureLocation();
    }
    @Deprecated public @Nullable Identifier getHUDTexture() {
        return this.getPojo().getHudTextureLocation();
    }
    @Deprecated public @Nullable Identifier getHudEmptyTexture() {
        return this.getPojo().getHudEmptyTextureLocation();
    }
    @Deprecated public boolean enablesTransparency() {
        return this.getPojo().getEnableTransparency();
    }
    @Deprecated public float getIronZoom() {
        return this.getPojo().getIronZoomScale();
    }
    @Deprecated public float getZoomModelFov() {
        return this.getPojo().getIronViewFov();
    }
    @Deprecated public boolean isShowCrosshair() {
        return this.getPojo().getEnableCrosshair();
    }
    @Deprecated public @Nullable _MuzzleFlashDisplay getMuzzleFlash() {
        return this.getPojo().getMuzzleFlashDisplay();
    }
    @Deprecated public @Nullable _LaserDisplay getLaserConfig() {
        return this.getPojo().getLaserDisplay();
    }
    @Deprecated public @Nullable Int2ObjectArrayMap<_SurroundDisplay> getHotbarShow() {
        return this.getSurroundDisplayByHotbar();
    }
    @Deprecated public _SurroundDisplay getOffhandShow() {
        return this.getPojo().getSurroundDisplayByOffhand();
    }
    @Deprecated public DamageDisplayType getDamageStyle() {
        return this.getPojo().getDamageDisplayType();
    }
    @Deprecated public AmmoCountType getAmmoCountStyle() {
        return this.getPojo().getAmmoCountType();
    }
    @Deprecated public @Nullable _AmmoParticle getParticle() {
        return this.getAmmoParticle();
    }
    @Deprecated public @Nullable _ShellEjectionParam getShellEjection() {
        return this.getPojo().getShellEjectionParam();
    }
    @Deprecated public String getThirdPersonAnimation() {
        return this.getPojo().getShooterAnimationCategory().getCategoryName();
    }
    @Deprecated public @Nullable Identifier getPlayerAnimator3rd() {
        return this.getPojo().getPlayerAnimatorLocation();
    }
    @Deprecated public boolean is3rdFixedHand() {
        return this.getPojo().getPlayerAnimatorFixedHand();
    }
    @Deprecated public @Nullable Identifier getSounds(GunSoundType gunSoundType) {
        return this.getGunSound(gunSoundType);
    }
    @Deprecated public @Nullable List<Identifier> getPreloadSounds() {
        return this.getPojo().getPreloadSoundLocation();
    }
    @Deprecated public @Nullable _ControllableData getControllableData() {
        return this.getPojo().getControllableData();
    }
}
