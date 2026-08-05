/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.core.api.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public interface ILivingDamageEvent extends IEvent {

    /**
     * neoforge1.21.1的LivingDamageEvent.Post不支持设置伤害值(取消事件)
     * 请移步至更早的 {@link ILivingHurtEvent}
     */
    @Deprecated(since = "neoforge1.21.1", forRemoval = false)
    @Override
    default void setCanceled(boolean cancel) {}

    @NotNull LivingEntity getEntity();

    @NotNull DamageSource getSource();

    float getDamageAmount();
}
