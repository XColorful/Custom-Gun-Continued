/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;

public interface IGunDataAccess extends IGunPojoGetter,
        IGunStateAccess,
        IGunAmmoDataAccess, IGunAttachmentDataAccess,
        IGunExpAccess,
        _IGunPropertyAccess {

    /**
     * 获取枪械ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull Identifier getGunLocation(ItemStack gunItem);
    void setGunLocation(ItemStack gunItem, Identifier gunLocation);
    /**
     * 获取指定的GunDisplay，如无则返回 // TODO
     */
    @Nullable Identifier getGunDisplayLocation(ItemStack gunItem);
    void setGunDisplayLocation(ItemStack gunItem, Identifier gunDisplayLocation);

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
