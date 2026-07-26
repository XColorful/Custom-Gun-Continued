/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.event.shooter.ShooterSwitchFireModeEvent;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.event.EventPoster;
import xiao.customgun.core.entity.shooter.modifier.ShooterGunModifierManager;
import xiao.customgun.core.network.message.event.ServerMessageGunSwitchFireMode;
import xiao.customgun.core.util.SendUtils;

public final class LivingShooterSwitchFireMode extends LivingShooterAspect {

    public LivingShooterSwitchFireMode(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public void switchFireMode() {
        if (this.shooterProperty.currentGunItem == null) return;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return;

        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        if (EventPoster.get().postCustomEvent(new ShooterSwitchFireModeEvent(McLogicalSide.SERVER,
                iLivingShooter, this.livingShooter, iGun, currentGunItem))) {
            return;
        }
        SendUtils.sendMessageToTrackingEntity(this.livingShooter,
                new ServerMessageGunSwitchFireMode(this.livingShooter.getId(), currentGunItem));

        iGun.switchFireMode(this.shooterProperty, iGun, currentGunItem);
        // 刷新配件缓存
        ShooterGunModifierManager.postChangeEvent(this.livingShooter, currentGunItem);
    }
}
