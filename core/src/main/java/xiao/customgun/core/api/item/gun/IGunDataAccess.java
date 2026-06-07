/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;

public interface IGunDataAccess extends IGunStateAccess,
        IGunAmmoDataAccess, IGunAttachmentDataAccess,
        IGunExpAccess,
        _IGunPropertyAccess {

    @Nullable String getManagerGroupTag(ItemStack gunItem);
    void setManagerGroupTag(ItemStack gunItem, String managerGroupTag);

    /**
     * 获取枪械ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull ResourceLocation getGunLocation(ItemStack gunItem);
    void setGunLocation(ItemStack gunItem, ResourceLocation gunLocation);
    /**
     * 获取指定的GunDisplay，如无则返回 // TODO
     */
    @Nullable ResourceLocation getGunDisplayLocation(ItemStack gunItem);
    void setGunDisplayLocation(ItemStack gunItem, ResourceLocation gunDisplayLocation);

    // --------Deprecated--------

    @Deprecated default @NotNull ResourceLocation getGunId(ItemStack gunItem) {
        return getGunLocation(gunItem);
    }
    @Deprecated default void setGunId(ItemStack gunItem, ResourceLocation gunLocation) {
        setGunDisplayLocation(gunItem, gunLocation);
    }
    @Deprecated default @NotNull ResourceLocation getGunDisplayId(ItemStack gunItem) {
        return getGunLocation(gunItem);
    }
    @Deprecated default void setGunDisplayId(ItemStack gunItem, ResourceLocation gunDisplayLocation) {
        setGunDisplayLocation(gunItem, gunDisplayLocation);
    }
}
