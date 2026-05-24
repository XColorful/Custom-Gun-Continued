/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.entity.shooter;

import net.minecraft.world.entity.LivingEntity;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.mixin.entity.LivingEntityMixin;

public interface ILivingShooterGetter {

    /**
     * {@link LivingEntityMixin} mixin到LivingEntity实现该接口
     */
    static ILivingShooter cgc$fromLivingEntity(LivingEntity livingEntity) {
        return (ILivingShooter) livingEntity;
    }
}
