/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IGunDataGetter extends IGunAttachmentDataGetter {

    /**
     * 获取枪械的开火模式
     */
    FireModeType getFireModeType(ItemStack gunItem);

    /**
     * 获取瞄准放大倍率
     */
    float getScopeZoomScale(ItemStack gunItem);

    // --------Deprecated--------

    @Deprecated static FireModeType getMainHandFireMode(LivingEntity livingEntity) {
        var iGunGetter = IGunGetter.fromMainHand(livingEntity);
        return iGunGetter != null ? iGunGetter.getFireModeType(livingEntity.getMainHandItem()) : null;
    }
    @Deprecated default float getAimingZoom(ItemStack gunItem) {
        return getScopeZoomScale(gunItem);
    }
}
