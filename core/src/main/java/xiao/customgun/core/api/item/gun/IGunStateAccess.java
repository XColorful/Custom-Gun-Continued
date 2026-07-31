/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IGunStateAccess {

    /**
     * 获取枪械的开火模式
     */
    FireModeType getFireModeType(ItemStack gunItem);
    void setFireModeType(ItemStack gunItem, FireModeType fireModeType);

    /**
     * 获取瞄准放大倍率
     */
    float getScopeZoomScale(ItemStack gunItem);

    /**
     * 获取热量
     */
    boolean hasHeat(ItemStack gunItem);
    float getHeatCount(ItemStack gunItem);
    void setHeatCount(ItemStack gunItem, float amount);
    /**
     * 获取过热锁
     */
    boolean hasOverheatLock(ItemStack gunItem);
    void setOverheatLock(ItemStack gunItem, boolean locked);

    /**
     * 枪械配件锁，只在网络包处理时使用，不干扰指令等其他方式修改NBT (避免需要先手动移除lock再恢复)
     */
    boolean hasAttachmentLock(ItemStack gunItem);
    void setAttachmentLock(ItemStack gunItem, boolean value);

    /**
     * 获取镭射颜色
     */
    boolean hasLaserColor(ItemStack gunItem);
    int getLaserColorInt(ItemStack gunItem);
    void setLaserColorInt(ItemStack gunItem, int colorInt);

    /**
     * 获取tooltip掩码 (服务端处理数据，客户端读取)
     */
    boolean hasTooltipMask(ItemStack gunItem);
    int getTooltipMask(ItemStack gunItem);
    void setTooltipMask(ItemStack gunItem, int tooltipMask);

    /**
     * 获取枪械当前使用的近战类型
     */
    @Nullable MeleeType getGunMeleeType(ItemStack gunItem);

    // --------Deprecated--------

    @Deprecated static FireModeType getMainHandFireMode(LivingEntity livingEntity) {
        var iGunGetter = IGunGetter.fromMainHand(livingEntity);
        return iGunGetter != null ? iGunGetter.getFireModeType(livingEntity.getMainHandItem()) : null;
    }
    @Deprecated default float getAimingZoom(ItemStack gunItem) {
        return getScopeZoomScale(gunItem);
    }

    // ↓把Heat属性的计算方法放IGun，那不是到处拉屎吗?
    @Deprecated default float lerpRPM(ItemStack gunItem) { return 1f; }
    @Deprecated default float lerpInaccuracy(ItemStack gunItem) { return 1f; }
}
