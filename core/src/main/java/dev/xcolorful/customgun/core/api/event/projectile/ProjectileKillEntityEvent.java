/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.event.projectile;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.IBulletVictimEntity;
import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.event.CustomEventType;
import dev.xcolorful.customgun.core.api.event.ICustomEvent;
import dev.xcolorful.customgun.core.api.event.ICustomEventHandler;
import dev.xcolorful.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;
import dev.xcolorful.customgun.core.event.EventDispatcher;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 枪射物{@link IGunProjectile} 击杀 实体{@link Entity} 事件
 */
public final class ProjectileKillEntityEvent extends ProjectileHitEntityEvent {
    @Override
    public boolean isCancelable() {
        return false;
    }

    public ProjectileKillEntityEvent(McLogicalSide mcLogicalSide, @NotNull Context context,
                                     @Nullable IGunProjectile iGunProjectile, @Nullable Entity gunProjectile,
                                     @NotNull IProjectilePhysicsRuntime.EntityHitResult entityHitResult, @Nullable IBulletVictimEntity iBulletVictimEntity) {
        super(mcLogicalSide, context, iGunProjectile, gunProjectile, entityHitResult, iBulletVictimEntity);
    }
    @Override public CustomEventType getEventType() {
        return CustomEventType.PROJECTILE_KILL_ENTITY_EVENT;
    }

    private static final EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> _EVENT_DISPATCHER = CustomGun.getEventPoster().getEventDispatcher(ProjectileKillEntityEvent.class);
    @Override public @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher() {
        return _EVENT_DISPATCHER;
    }
}
