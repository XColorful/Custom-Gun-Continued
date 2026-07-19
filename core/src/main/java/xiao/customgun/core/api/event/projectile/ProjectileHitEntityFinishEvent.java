/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.event.projectile;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.IBulletVictimEntity;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.event.CustomEventType;
import xiao.customgun.core.api.event.ICustomEvent;
import xiao.customgun.core.api.event.ICustomEventHandler;
import xiao.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;
import xiao.customgun.core.event.EventDispatcher;

public final class ProjectileHitEntityFinishEvent extends ProjectileHitEntityEvent {
    @Override
    public boolean isCancelable() {
        return false;
    }

    public ProjectileHitEntityFinishEvent(McLogicalSide logicalSide, Context context,
                                          @Nullable IGunProjectile iGunProjectile, @Nullable Entity gunProjectile,
                                          @NotNull IProjectilePhysicsRuntime.EntityHitResult entityHitResult, @Nullable IBulletVictimEntity iBulletVictimEntity) {
        super(logicalSide, context, iGunProjectile, gunProjectile, entityHitResult, iBulletVictimEntity);
    }
    @Override public CustomEventType getEventType() {
        return CustomEventType.PROJECTILE_HIT_ENTITY_FINISH_EVENT;
    }

    private static final EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> _EVENT_DISPATCHER = CustomGun.getEventPoster().getEventDispatcher(ProjectileHitEntityFinishEvent.class);
    @Override public @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher() {
        return _EVENT_DISPATCHER;
    }
}
