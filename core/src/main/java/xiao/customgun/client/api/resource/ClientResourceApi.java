/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.resource.AssetsInstanceManager;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.resource.instance.data.AmmoIndexInstance;
import xiao.customgun.client.resource.instance.data.AttachmentIndexInstance;
import xiao.customgun.client.resource.instance.data.BlockIndexInstance;
import xiao.customgun.client.resource.instance.data.GunIndexInstance;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.data.index.GunIndex;

public class ClientResourceApi {

    // --------data instance--------

    public static @Nullable GunIndexInstance getGunIndexInstance(ResourceLocation gunLocation) {
        return AssetsInstanceManager.GUN_INDEX.get(gunLocation);
    }

    public static @Nullable AttachmentIndexInstance getAttachmentIndexInstance(ResourceLocation attachmentLocation) {
        return AssetsInstanceManager.ATTACHMENT_INDEX.get(attachmentLocation);
    }

    public static @Nullable AmmoIndexInstance getAmmoIndexInstance(ResourceLocation ammoLocation) {
        return AssetsInstanceManager.AMMO_INDEX.get(ammoLocation);
    }

    public static @Nullable BlockIndexInstance getBlockIndexInstance(ResourceLocation blockLocation) {
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
    public static @Nullable GunDisplayInstance getGunDisplayInstance(ResourceLocation displayLocation) {
        return AssetsInstanceManager.GUN_DISPLAY.get(displayLocation);
    }
    public static @Nullable GunDisplayInstance getGunDisplayInstance(ResourceLocation displayLocation, ResourceLocation fallbackLocation) {
        GunDisplayInstance instance = getGunDisplayInstance(displayLocation);
        return instance != null ? instance : getGunDisplayInstance(fallbackLocation);
    }
}
