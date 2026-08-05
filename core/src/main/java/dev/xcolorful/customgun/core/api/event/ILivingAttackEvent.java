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

public interface ILivingAttackEvent extends IEvent {

    @NotNull LivingEntity getEntity();

    @NotNull DamageSource getSource();

    float getDamageAmount();
}