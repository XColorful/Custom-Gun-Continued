/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.resource;

import dev.xcolorful.customgun.client.resource._AllAssetsManager;
import dev.xcolorful.customgun.client.resource._AssetsInstanceManager;
import dev.xcolorful.customgun.client.resource.assets.animation.BedrockAnimation;
import dev.xcolorful.customgun.client.resource.assets.animation.GltfAnimation;
import dev.xcolorful.customgun.client.resource.assets.display.AmmoDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.BlockDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.client.resource.assets.info.GunpackInfo;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.client.resource.assets.script.AssetsScript;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAmmoIndexInstance;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import dev.xcolorful.customgun.client.resource.instance.data.ClientBlockIndexInstance;
import dev.xcolorful.customgun.client.resource.instance.data.ClientGunIndexInstance;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientResourceApi {

    // --------gunpack info--------

    public static @Nullable GunpackInfo getGunpackInfo(Identifier gunpackInfoLocation) {
        var assetsManager = _AllAssetsManager.INSTANCE.gunpackInfoManager;
        return assetsManager != null ? assetsManager.getPojo(gunpackInfoLocation) : null;
    }
    public static Set<Map.Entry<Identifier, GunpackInfo>> getAllGunpackInfo() {
        var assetsManager = _AllAssetsManager.INSTANCE.gunpackInfoManager;
        return assetsManager != null ? assetsManager.getAllPojo().entrySet() : new HashSet<>();
    }

    // --------animation--------

    public static @Nullable BedrockAnimation getBedrockAnimation(Identifier animationLocation) {
        var assetsManager = _AllAssetsManager.INSTANCE.bedrockAnimationManager;
        return assetsManager != null ? assetsManager.getPojo(animationLocation) : null;
    }
    public static Set<Map.Entry<Identifier, BedrockAnimation>> getAllBedrockAnimation() {
        var assetsManager = _AllAssetsManager.INSTANCE.bedrockAnimationManager;
        return assetsManager != null ? assetsManager.getAllPojo().entrySet() : new HashSet<>();
    }
    public static @Nullable GltfAnimation getGltfAnimation(Identifier animationLocation) {
        var assetsManager = _AllAssetsManager.INSTANCE.gltfAnimationManager;
        return assetsManager != null ? assetsManager.getPojo(animationLocation) : null;
    }
    public static Set<Map.Entry<Identifier, GltfAnimation>> getAllGltfAnimation() {
        var assetsManager = _AllAssetsManager.INSTANCE.gltfAnimationManager;
        return assetsManager != null ? assetsManager.getAllPojo().entrySet() : new HashSet<>();
    }

    // --------display--------

    public static @Nullable GunDisplay getGunDisplay(Identifier displayLocation) {
        var assetsManager = _AllAssetsManager.INSTANCE.gunDisplayManager;
        return assetsManager != null ? assetsManager.getPojo(displayLocation) : null;
    }
    public static Set<Map.Entry<Identifier, GunDisplay>> getAllGunDisplay() {
        var assetsManager = _AllAssetsManager.INSTANCE.gunDisplayManager;
        return assetsManager != null ? assetsManager.getAllPojo().entrySet() : new HashSet<>();
    }
    public static @Nullable AttachmentDisplay getAttachmentDisplay(Identifier attachmentDisplayLocation) {
        var assetsManager = _AllAssetsManager.INSTANCE.attachmentDisplayManager;
        return assetsManager != null ? assetsManager.getPojo(attachmentDisplayLocation) : null;
    }
    public static Set<Map.Entry<Identifier, AttachmentDisplay>> getAllAttachmentDisplay() {
        var assetsManager = _AllAssetsManager.INSTANCE.attachmentDisplayManager;
        return assetsManager != null ? assetsManager.getAllPojo().entrySet() : new HashSet<>();
    }
    public static @Nullable AmmoDisplay getAmmoDisplay(Identifier ammoDisplayLocation) {
        var assetsManager = _AllAssetsManager.INSTANCE.ammoDisplayManager;
        return assetsManager != null ? assetsManager.getPojo(ammoDisplayLocation) : null;
    }
    public static Set<Map.Entry<Identifier, AmmoDisplay>> getAllAmmoDisplay() {
        var assetsManager = _AllAssetsManager.INSTANCE.ammoDisplayManager;
        return assetsManager != null ? assetsManager.getAllPojo().entrySet() : new HashSet<>();
    }
    public static @Nullable BlockDisplay getBlockDisplay(Identifier blockDisplayLocation) {
        var assetsManager = _AllAssetsManager.INSTANCE.blockDisplayManager;
        return assetsManager != null ? assetsManager.getPojo(blockDisplayLocation) : null;
    }
    public static Set<Map.Entry<Identifier, BlockDisplay>> getAllBlockDisplay() {
        var assetsManager = _AllAssetsManager.INSTANCE.blockDisplayManager;
        return assetsManager != null ? assetsManager.getAllPojo().entrySet() : new HashSet<>();
    }

    // --------model--------

    public static @Nullable BedrockModel getBedrockModel(Identifier bedrockModelLocation) {
        var assetsManager = _AllAssetsManager.INSTANCE.bedrockModelManager;
        return assetsManager != null ? assetsManager.getPojo(bedrockModelLocation) : null;
    }
    public static Set<Map.Entry<Identifier, BedrockModel>> getAllBedrockModel() {
        var assetsManager = _AllAssetsManager.INSTANCE.bedrockModelManager;
        return assetsManager != null ? assetsManager.getAllPojo().entrySet() : new HashSet<>();
    }

    // --------script--------

    public static @Nullable AssetsScript getAssetsScript(Identifier scriptLocation) {
        var assetsManager = _AllAssetsManager.INSTANCE.clientScriptManager;
        return assetsManager != null ? assetsManager.getFile(scriptLocation) : null;
    }
    public static Set<Map.Entry<Identifier, AssetsScript>> getAllAssetsScript() {
        var assetsManager = _AllAssetsManager.INSTANCE.clientScriptManager;
        return assetsManager != null ? assetsManager.getAllFiles().entrySet() : new HashSet<>();
    }

    // --------data instance--------

    public static @Nullable ClientGunIndexInstance getClientGunIndexInstance(Identifier gunLocation) {
        return _AssetsInstanceManager.GUN_INDEX.get(gunLocation);
    }
    public static Set<Map.Entry<Identifier, @NotNull ClientGunIndexInstance>> getAllClientGunIndexInstance() {
        return _AssetsInstanceManager.GUN_INDEX.entrySet();
    }
    public static @Nullable ClientAttachmentIndexInstance getClientAttachmentIndexInstance(Identifier attachmentLocation) {
        return _AssetsInstanceManager.ATTACHMENT_INDEX.get(attachmentLocation);
    }
    public static Set<Map.Entry<Identifier, @NotNull ClientAttachmentIndexInstance>> getAllClientAttachmentIndexInstance() {
        return _AssetsInstanceManager.ATTACHMENT_INDEX.entrySet();
    }
    public static @Nullable ClientAmmoIndexInstance getClientAmmoIndexInstance(Identifier ammoLocation) {
        return _AssetsInstanceManager.AMMO_INDEX.get(ammoLocation);
    }
    public static Set<Map.Entry<Identifier, @NotNull ClientAmmoIndexInstance>> getAllClientAmmoIndexInstance() {
        return _AssetsInstanceManager.AMMO_INDEX.entrySet();
    }
    public static @Nullable ClientBlockIndexInstance getClientBlockIndexInstance(Identifier blockLocation) {
        return _AssetsInstanceManager.BLOCK_INDEX.get(blockLocation);
    }
    public static Set<Map.Entry<Identifier, @NotNull ClientBlockIndexInstance>> getAllClientBlockIndexInstance() {
        return _AssetsInstanceManager.BLOCK_INDEX.entrySet();
    }

    // --------assets instance--------

    public static @Nullable GunDisplayInstance getGunDisplayInstance(ItemStack gunItem) {
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return null;

        var displayLocation = iGun.getGunDisplayLocation(gunItem);
        return getGunDisplayInstance(displayLocation);
    }
    public static @Nullable GunDisplayInstance getGunDisplayInstance(Identifier displayLocation) {
        return _AssetsInstanceManager.GUN_DISPLAY.get(displayLocation);
    }
    public static @Nullable GunDisplayInstance getGunDisplayInstance(Identifier displayLocation, Identifier fallbackLocation) {
        GunDisplayInstance instance = getGunDisplayInstance(displayLocation);
        return instance != null ? instance : getGunDisplayInstance(fallbackLocation);
    }
    public static Set<Map.Entry<Identifier, @NotNull GunDisplayInstance>> getAllGunDisplayInstance() {
        return _AssetsInstanceManager.GUN_DISPLAY.entrySet();
    }

    // --------recipe--------

    public static RecipeManager getRecipeManager() {
        Level level = Minecraft.getInstance().level;
        return level != null ? getRecipeManager() : null;
    }
}
