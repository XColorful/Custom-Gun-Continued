/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.network.message.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.util.ClientWorldUtils;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.event.shooter.ShooterFireEvent;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.network.message.event.ServerMessageGunShoot;

@ApiStatus.Internal
public class _ServerMessageGunShoot {

    public static void doClientEvent(ServerMessageGunShoot message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        @Nullable LivingEntity livingShooter = ClientWorldUtils.getLivingEntityById(level, message.shooterId());
        @Nullable ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromEntity(livingShooter);
        ItemStack gunItem = message.gunItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);

        ShooterFireEvent event = new ShooterFireEvent(McLogicalSide.CLIENT,
                iLivingShooter, livingShooter,
                iGun, gunItem);
        CustomGun.getEventPoster().postCustomEvent(event);
    }
}
