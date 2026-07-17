/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.instance.assets;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.item.gun.DamageDisplayType;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.model.GunModelObject;
import xiao.customgun.client.model.ModelObject;
import xiao.customgun.client.resource.assets.display.GunDisplay;
import xiao.customgun.client.resource.assets.display._LaserDisplay;
import xiao.customgun.client.resource.assets.display._LodDisplay;
import xiao.customgun.client.resource.assets.display._ModelTransform;
import xiao.customgun.client.resource.assets.display.ammo._AmmoParticle;
import xiao.customgun.client.resource.assets.display.gun.*;
import xiao.customgun.client.resource.assets.model.BedrockModel;
import xiao.customgun.client.resource.assets.script.AssetsScript;
import xiao.customgun.core.api.item.gun.AmmoCountType;
import xiao.customgun.core.resource.instance.PojoInstance;

import java.awt.*;
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
    private @Nullable _AmmoParticle ammoParticleCache;
    private @Nullable ParticleOptions ammoParticleOptionsCache;
    private Map<GunSoundType, ResourceLocation> gunSoundsCache;

    private @Nullable LuaTable script = null;
    private @Nullable LuaTable scriptParamCache = null;

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
        {
            BedrockModel bedrockModel = ClientResourceApi.getBedrockModel(this.getPojo().getModelLocation());
            if (bedrockModel != null) {
                this.gunModel = GunModelObject.fromPojo(bedrockModel);
                if (this.gunModel == null) CustomGun.LOGGER.debug("GunDisplayInstance: Failed to create GunModelObject {}", this.getPojo().getModelLocation());
            } else {
                CustomGun.LOGGER.debug("GunDisplayInstance: BedrockModel {} not found", this.getPojo().getModelLocation());
            }
        }
        _LodDisplay lodDisplay = this.getPojo().getLodDisplay();
        if (lodDisplay != null) {
            BedrockModel bedrockModel = ClientResourceApi.getBedrockModel(lodDisplay.getModelLocation());
            if (bedrockModel != null) {
                this.gunModelLod = GunModelObject.fromPojo(bedrockModel);
                if (this.gunModelLod == null) CustomGun.LOGGER.debug("GunDisplayInstance: Failed to create GunModelObject (for lod) {}", lodDisplay.getModelLocation());
            } else {
                CustomGun.LOGGER.debug("GunDisplayInstance: BedrockModel (for lod) {} not found", lodDisplay.getModelLocation());
            }
        }

        Map<String, _SurroundDisplay> surroundDisplayByHotbar = this.getPojo().getSurroundDisplayByHotbar();
        if (surroundDisplayByHotbar != null) {
            this.surroundDisplayByHotbarCache = new Int2ObjectArrayMap<>();
            for (Map.Entry<String, _SurroundDisplay> entry : surroundDisplayByHotbar.entrySet()) {
                try {
                    this.surroundDisplayByHotbarCache.put(Integer.parseInt(entry.getKey()), entry.getValue());
                } catch (Exception ignored) {}
            }
        }

        _AmmoDisplayOverride ammoDisplayOverride = this.getPojo().getAmmoDisplayOverride();
        if (ammoDisplayOverride != null) {
            this.tracerColorCache = ammoDisplayOverride.getTracerColor();
            this.ammoParticleCache = ammoDisplayOverride.getAmmoParticle();
            if (this.ammoParticleOptionsCache != null) {
                var particleRl = this.ammoParticleCache.getParticleLocation();
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
        this.gunSoundsCache = this.getPojo().getGunSounds();

        var scriptLocation = this.getPojo().getScriptLocation();
        if (scriptLocation != null) {
            AssetsScript assetsScript = ClientResourceApi.getAssetsScript(scriptLocation);
            if (assetsScript == null) CustomGun.LOGGER.debug("GunDisplayInstance: AssetsScript {} not found", scriptLocation);
            else if (!assetsScript.isValid()) CustomGun.LOGGER.debug("GunDisplayInstance: AssetsScript {} not valid", scriptLocation);
            else this.script = assetsScript.getResultTable();
        }
        this.reloadScriptParams();

        return true;
    }
    private static final int ERR_TRANSFORM_SCALE = 1;
    private static final int ERR_AMMO_PARTICLE_COUNT = 1 << 1;
    private static final int ERR_AMMO_PARTICLE_LIFETIME = 1 << 2;
    private static final int ERR_IRON_ZOOM_SCALE = 1 << 3;
    private static final int ERR_IRON_VIEW_FOV = 1 << 4;
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;
        if (!resetCache()) return false;

        int errorMask = 0;
        // GunDisplay
        errorMask |= this.getPojo().getModelTransform().getScale() == null ? ERR_TRANSFORM_SCALE : 0;
        errorMask |= (this.ammoParticleCache != null && this.ammoParticleCache.getCount() < 1) ? ERR_AMMO_PARTICLE_COUNT : 0;
        errorMask |= (this.ammoParticleCache != null && this.ammoParticleCache.getLifetimeTicks() < 1) ? ERR_AMMO_PARTICLE_LIFETIME : 0;
        errorMask |= this.getPojo().getIronZoomScale() < 1 ? ERR_IRON_ZOOM_SCALE : 0;
        errorMask |= this.getPojo().getIronViewFov() > 70 ? ERR_IRON_VIEW_FOV : 0;
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
    private void reloadScriptParams() {
        // 加载状态机参数
        Map<String, Object> params = this.getPojo().getScriptParam();
        if (params != null) {
            this.scriptParamCache = new LuaTable();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                this.scriptParamCache.set(entry.getKey(), CoerceJavaToLua.coerce(entry.getValue()));
            }
        }
    }

    // --------Getter--------

    public @Nullable ModelObject getGunModel() {
        return this.gunModel;
    }
    public @Nullable ModelObject getGunModelLod() {
        return this.gunModelLod;
    }

    public @Nullable Int2ObjectArrayMap<_SurroundDisplay> getSurroundDisplayByHotbar() {
        return this.surroundDisplayByHotbarCache;
    }
    public @Nullable Color getTracerColor() {
        return this.tracerColorCache;
    }
    public @Nullable _AmmoParticle getAmmoParticle() {
        return this.ammoParticleCache;
    }
    public @Nullable ParticleOptions getParticleOptions() {
        return this.ammoParticleOptionsCache;
    }
    public @Nullable ResourceLocation getGunSound(GunSoundType gunSoundType) {
        return this.gunSoundsCache.get(gunSoundType);
    }
    public @Nullable LuaTable getScript() {
        return this.script;
    }
    public @Nullable LuaTable getScriptParams() {
        return this.scriptParamCache;
    }

    // --------Deprecated--------

    @Deprecated public @Nullable _ModelTransform getTransform() {
        return this.getPojo().getModelTransform();
    }
    @Deprecated public ResourceLocation getModelTexture() {
        return this.getPojo().getTextureLocation();
    }
    @Deprecated public ResourceLocation getSlotTexture() {
        return this.getPojo().getSlotTextureLocation();
    }
    @Deprecated public ResourceLocation getHUDTexture() {
        return this.getPojo().getHudTextureLocation();
    }
    @Deprecated public ResourceLocation getHudEmptyTexture() {
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
        return this.getPojo().getThirdPersonAnimationType().getCategoryName();
    }
    @Deprecated public @Nullable ResourceLocation getPlayerAnimator3rd() {
        return this.getPojo().getPlayerAnimatorLocation();
    }
    @Deprecated public boolean is3rdFixedHand() {
        return this.getPojo().getPlayerAnimatorFixedHand();
    }
    @Deprecated public @Nullable ResourceLocation getSounds(GunSoundType gunSoundType) {
        return this.getGunSound(gunSoundType);
    }
    @Deprecated public @Nullable List<ResourceLocation> getPreloadSounds() {
        return this.getPojo().getPreloadSoundLocation();
    }
    @Deprecated public @Nullable _ControllableData getControllableData() {
        return this.getPojo().getControllableData();
    }
}
