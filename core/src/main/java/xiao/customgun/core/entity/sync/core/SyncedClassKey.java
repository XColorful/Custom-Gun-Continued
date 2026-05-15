/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.sync.core;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import xiao.customgun.CustomGun;

/**
 * Author: MrCrayfish.
 * Open source at <a href="https://github.com/MrCrayfish/Framework">Github</a> under LGPL License.
 */
public record SyncedClassKey<E extends Entity>(Class<E> entityClass, Identifier id) {
    public static final SyncedClassKey<LivingEntity> LIVING_ENTITY = new SyncedClassKey<>(LivingEntity.class, CustomGun.getMcRegistry().createResourceLocation("living_entity"));

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SyncedClassKey<?> that = (SyncedClassKey<?>) o;
        return this.entityClass.getName().equals(that.entityClass.getName());
    }

    @Override
    public int hashCode() {
        return this.entityClass.getName().hashCode();
    }
}
