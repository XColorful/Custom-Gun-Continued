/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.resource.AllAssetsManager;
import xiao.customgun.client.resource.AssetsInstanceManager;
import xiao.customgun.client.resource.assets.animation.BedrockAnimation;
import xiao.customgun.client.resource.assets.animation.GltfAnimation;
import xiao.customgun.client.resource.assets.display.AmmoDisplay;
import xiao.customgun.client.resource.assets.display.AttachmentDisplay;
import xiao.customgun.client.resource.assets.display.BlockDisplay;
import xiao.customgun.client.resource.assets.display.GunDisplay;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.resource.instance.data.ClientAmmoIndexInstance;
import xiao.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import xiao.customgun.client.resource.instance.data.ClientBlockIndexInstance;
import xiao.customgun.client.resource.instance.data.ClientGunIndexInstance;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.data.index.GunIndex;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientResourceApi {

    // --------animation--------

    public static @Nullable BedrockAnimation getBedrockAnimation(ResourceLocation animationLocation) {
        var assetsManager = AllAssetsManager.INSTANCE.bedrockAnimationManager;
        return assetsManager != null ? assetsManager.getPojo(animationLocation) : null;
    }
    public static @Nullable GltfAnimation getGltfAnimation(ResourceLocation animationLocation) {
        var assetsManager = AllAssetsManager.INSTANCE.gltfAnimationManager;
        return assetsManager != null ? assetsManager.getPojo(animationLocation) : null;
    }

    // --------display--------

    public static @Nullable GunDisplay getGunDisplay(ResourceLocation displayLocation) {
        var assetsManager = AllAssetsManager.INSTANCE.gunDisplayManager;
        return assetsManager != null ? assetsManager.getPojo(displayLocation) : null;
    }
    public static Set<Map.Entry<ResourceLocation, GunDisplay>> getAllGunDisplay() {
        var assetsManager = AllAssetsManager.INSTANCE.gunDisplayManager;
        return assetsManager != null ? assetsManager.getAllPojo().entrySet() : new HashSet<>();
    }
    public static @Nullable AttachmentDisplay getAttachmentDisplay(ResourceLocation attachmentDisplayLocation) {
        var assetsManager = AllAssetsManager.INSTANCE.attachmentDisplayManager;
        return assetsManager != null ? assetsManager.getPojo(attachmentDisplayLocation) : null;
    }
    public static Set<Map.Entry<ResourceLocation, AttachmentDisplay>> getAllAttachmentDisplay() {
        var assetsManager = AllAssetsManager.INSTANCE.attachmentDisplayManager;
        return assetsManager != null ? assetsManager.getAllPojo().entrySet() : new HashSet<>();
    }
    public static @Nullable AmmoDisplay getAmmoDisplay(ResourceLocation ammoDisplayLocation) {
        var assetsManager = AllAssetsManager.INSTANCE.ammoDisplayManager;
        return assetsManager != null ? assetsManager.getPojo(ammoDisplayLocation) : null;
    }
    public static Set<Map.Entry<ResourceLocation, AmmoDisplay>> getAllAmmoDisplay() {
        var assetsManager = AllAssetsManager.INSTANCE.ammoDisplayManager;
        return assetsManager != null ? assetsManager.getAllPojo().entrySet() : new HashSet<>();
    }
    public static @Nullable BlockDisplay getBlockDisplay(ResourceLocation blockDisplayLocation) {
        var assetsManager = AllAssetsManager.INSTANCE.blockDisplayManager;
        return assetsManager != null ? assetsManager.getPojo(blockDisplayLocation) : null;
    }
    public static Set<Map.Entry<ResourceLocation, BlockDisplay>> getAllBlockDisplay() {
        var assetsManager = AllAssetsManager.INSTANCE.blockDisplayManager;
        return assetsManager != null ? assetsManager.getAllPojo().entrySet() : new HashSet<>();
    }

    // --------data instance--------

    public static @Nullable ClientGunIndexInstance getClientGunIndexInstance(Identifier gunLocation) {
        return AssetsInstanceManager.GUN_INDEX.get(gunLocation);
    }
    public static @Nullable ClientAttachmentIndexInstance getClientAttachmentIndexInstance(Identifier attachmentLocation) {
        return AssetsInstanceManager.ATTACHMENT_INDEX.get(attachmentLocation);
    }
    public static @Nullable ClientAmmoIndexInstance getClientAmmoIndexInstance(Identifier ammoLocation) {
        return AssetsInstanceManager.AMMO_INDEX.get(ammoLocation);
    }
    public static @Nullable ClientBlockIndexInstance getClientBlockIndexInstance(Identifier blockLocation) {
        return AssetsInstanceManager.BLOCK_INDEX.get(blockLocation);
    }

    // --------assets instance--------

    public static @Nullable GunDisplayInstance getGunDisplayInstance(ItemStack gunItem) {
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return null;

        var gunLocation = iGun.getGunLocation(gunItem);
        GunIndex gunIndex = ResourceApi.getGunIndex(gunLocation);
        if (gunIndex == null) { // 没有索引默认是无效数据
            return null;
        }

        // 优先用NBT指定的Display
        @Nullable var displayLocation = iGun.getGunDisplayLocation(gunItem);
        if (displayLocation != null) {
            return getGunDisplayInstance(displayLocation, gunIndex.getDisplayIndexLocation());
        }

        // 一般的Display
        return getGunDisplayInstance(gunIndex.getDisplayIndexLocation());
    }
    public static @Nullable GunDisplayInstance getGunDisplayInstance(Identifier displayLocation) {
        return AssetsInstanceManager.GUN_DISPLAY.get(displayLocation);
    }
    public static @Nullable GunDisplayInstance getGunDisplayInstance(Identifier displayLocation, Identifier fallbackLocation) {
        GunDisplayInstance instance = getGunDisplayInstance(displayLocation);
        return instance != null ? instance : getGunDisplayInstance(fallbackLocation);
    }

    // --------recipe--------

    public static RecipeManager getRecipeManager() {
        Level level = Minecraft.getInstance().level;
        return level != null ? getRecipeManager() : null;
    }
}
