/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.network.message.event;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.util.ClientWorldUtils;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.event.shooter.ShooterSwitchFireModeEvent;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.network.message.event.ServerMessageGunSwitchFireMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class _ServerMessageGunSwitchFireMode {

    public static void doClientEvent(ServerMessageGunSwitchFireMode message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        @Nullable LivingEntity livingShooter = ClientWorldUtils.getLivingEntityById(level, message.shooterId());
        @Nullable ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromEntity(livingShooter);
        ItemStack gunItem = message.gunItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);

        ShooterSwitchFireModeEvent event = new ShooterSwitchFireModeEvent(McLogicalSide.CLIENT,
                iLivingShooter, livingShooter,
                iGun, gunItem);
        CustomGun.getEventPoster().postCustomEvent(event);
    }
}
