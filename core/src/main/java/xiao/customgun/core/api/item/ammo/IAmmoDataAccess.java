/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.ammo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceTag;

public interface IAmmoDataAccess extends IAmmoPojoGetter,
        IAmmoNBTAccess,
        IAmmoExpAccess,
        _IAmmoPropertyAccess {

    /**
     * 获取子弹ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull ResourceLocation getAmmoLocation(ItemStack ammoItem);
    void setAmmoLocation(ItemStack ammoItem, ResourceLocation ammoLocation);

    /**
     * 获取子弹数量
     */
    int getAmmoCount(ItemStack ammoItem);
    void setAmmoCount(ItemStack ammoItem, int ammoCount);
    /**
     * 消耗子弹
     * @return 成功消耗的子弹数
     */
    int consumeAmmo(ItemStack ammoItem, int amount);

    /**
     * 消耗子弹是否不减少数量
     */
    boolean hasInfiniteFeed(ItemStack ammoItem);
    void setInfiniteFeed(ItemStack ammoItem, boolean infiniteFeed);

    /**
     * 是否为全类型子弹
     */
    boolean isAlmightyAmmo(ItemStack ammoItem);
    void setAlmightyAmmo(ItemStack ammoItem, boolean almighty);

    // --------Deprecated--------

    @Deprecated default @NotNull ResourceLocation getAmmoId(ItemStack ammoItem) {
        return getAmmoLocation(ammoItem);
    }
    @Deprecated default void setAmmoId(ItemStack ammoItem, ResourceLocation ammoLocation) {
        setAmmoLocation(ammoItem, ammoLocation);
    }

    @Deprecated default boolean isCreative(ItemStack ammoItem) {
        return hasInfiniteFeed(ammoItem);
    }
    @Deprecated default void setCreative(ItemStack ammoItem, boolean almighty) {
        setInfiniteFeed(ammoItem, true);
        setAlmightyAmmo(ammoItem, almighty);
    }

    @Deprecated default boolean isAllTypeCreative(ItemStack ammoItem) {
        return isAlmightyAmmo(ammoItem) && hasInfiniteFeed(ammoItem);
    }

    @Deprecated default boolean isAmmoOfGun(ItemStack gunItem, ItemStack ammoItem) {
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return false;
        return iGun.isMatchedAmmo(gunItem, ammoItem);
    }
}
