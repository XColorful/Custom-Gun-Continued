/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.entity.victim;

import dev.xcolorful.customgun.core.api.entity.IBulletVictimEntity;
import dev.xcolorful.customgun.core.mixin.entity.LivingEntityMixin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface IBulletVictimEntityGetter {

    static @Nullable IBulletVictimEntity fromEntity(Entity entity) {
        return entity instanceof IBulletVictimEntity iBulletVictimEntity ? iBulletVictimEntity : null;
    }

    /**
     * {@link LivingEntityMixin} mixin到LivingEntity实现该接口
     */
    static IBulletVictimEntity fromLivingEntity(LivingEntity livingEntity) {
        return (IBulletVictimEntity) livingEntity;
    }
}
