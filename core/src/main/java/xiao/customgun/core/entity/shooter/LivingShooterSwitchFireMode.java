/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.entity.shooter.modifier.ShooterGunModifierManager;
import xiao.customgun.core.network.message.event.ServerMessageGunSwitchFireMode;
import xiao.customgun.core.util.SendUtils;

public final class LivingShooterSwitchFireMode extends LivingShooterAspect {

    public LivingShooterSwitchFireMode(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public void switchFireMode() {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return;
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        if ( // 2.2 检查状态锁
                false
        ) return;

        // 3. IGunRuntime操作结果 -> Shooter状态
        boolean success = iGun.switchFireMode(this.shooterProperty, iGun, gunItem, ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter), this.livingShooter);
        if (!success) return;

        SendUtils.sendMessageToTrackingEntity(this.livingShooter,
                new ServerMessageGunSwitchFireMode(this.livingShooter.getId(), gunItem));

        // 刷新配件缓存
        ShooterGunModifierManager.postChangeEvent(this.livingShooter, gunItem);
    }
}
