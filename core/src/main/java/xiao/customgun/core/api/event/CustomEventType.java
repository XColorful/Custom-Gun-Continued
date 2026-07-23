/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * 改成跟 BattleRoyale 同构的写法
 */

package xiao.customgun.core.api.event;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.event.gun.GunFireEvent;
import xiao.customgun.core.api.event.shooter.ShooterGunModifierCacheEvent;
import xiao.customgun.core.api.event.projectile.ProjectileHitBlockEvent;
import xiao.customgun.core.api.event.projectile.ProjectileHitEntityFinishEvent;
import xiao.customgun.core.api.event.projectile.GunProjectileEvent;
import xiao.customgun.core.api.event.projectile.ProjectileKillEntityEvent;
import xiao.customgun.core.api.event.shooter.*;

import java.util.HashMap;
import java.util.Map;

public enum CustomEventType {
    // gun
    GUN_FIRE_EVENT(GunFireEvent.class),
    // projectile
    PROJECTILE_HIT_BLOCK_EVENT(ProjectileHitBlockEvent.class),
    PROJECTILE_HIT_ENTITY_EVENT(GunProjectileEvent.class),
    PROJECTILE_HIT_ENTITY_FINISH_EVENT(ProjectileHitEntityFinishEvent.class),
    PROJECTILE_KILL_ENTITY_EVENT(ProjectileKillEntityEvent.class),
    // shooter
    SHOOTER_DRAW_EVENT(ShooterDrawEvent.class),
    SHOOTER_FIRE_EVENT(ShooterFireEvent.class),
    SHOOTER_GUN_MODIFIER_CACHE_EVENT(ShooterGunModifierCacheEvent.class),
    SHOOTER_MELEE_EVENT(ShooterMeleeEvent.class),
    SHOOTER_RELOAD_EVENT(ShooterReloadEvent.class),
    SHOOTER_RELOAD_FINISH_EVENT(ShooterReloadFinishEvent.class),
    SHOOTER_SWITCH_FIRE_MODE_EVENT(ShooterSwitchFireModeEvent.class),

    // ----client----

    // player
    SWAP_ITEM_WITH_OFFHAND_EVENT(null),
    // render
    BEFORE_RENDER_HAND_EVENT(null),
    ITEM_IN_HAND_BOB_HURT_EVENT(null),
    ITEM_IN_HAND_BOB_VIEW_EVENT(null),
    LEVEL_BOB_HURT_EVENT(null),
    LEVEL_BOB_VIEW_EVENT(null),

    // custom
    CUSTOM_EVENT(null);

    @ApiStatus.Internal private Class<? extends ICustomEvent> eventClass;
    @ApiStatus.Internal public void setEventClass(Class<? extends ICustomEvent> eventClass) {
        if (this.eventClass != null) throw new IllegalStateException("Cannot set event class twice");
        else this.eventClass = eventClass;
    }
    CustomEventType(Class<? extends ICustomEvent> eventClass) {
        this.eventClass = eventClass;
    }
    public @Nullable Class<? extends ICustomEvent> getEventClass() {
        return eventClass;
    }

    private static final Map<String, CustomEventType> CUSTOM_EVENT_TYPES = new HashMap<>();

    static {
        for (CustomEventType type : values()) {
            CUSTOM_EVENT_TYPES.put(type.name(), type);
        }
    }

    public static @Nullable CustomEventType fromString(String name) {
        if (name == null) return null;
        return CUSTOM_EVENT_TYPES.get(name);
    }

    public String getName() {
        return this.name();
    }
}
