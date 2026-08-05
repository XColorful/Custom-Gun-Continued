/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.ammo;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface IAmmoNBTAccess {

    /**
     * 获取子弹ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull ResourceLocation getAmmoLocation(CompoundTag ammoItemCustomDataTag);
    void setAmmoLocation(CompoundTag ammoItemCustomDataTag, ResourceLocation ammoLocation);

    /**
     * 是否供应无限子弹
     */
    boolean hasInfiniteFeed(CompoundTag ammoItemCustomDataTag);
    void setInfiniteFeed(CompoundTag ammoItemCustomDataTag, boolean infiniteFeed);

    /**
     * 是否为全类型子弹
     */
    boolean isAlmightyAmmo(CompoundTag ammoItemCustomDataTag);
    void setAlmightyAmmo(CompoundTag ammoItemCustomDataTag, boolean almighty);
}
