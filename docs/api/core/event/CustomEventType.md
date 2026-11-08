```java
package dev.xcolorful.customgun.core.api.event;

public enum CustomEventType {
    // gun
    GUN_FIRE_EVENT(GunFireEvent.class),
    // projectile
    PROJECTILE_HIT_BLOCK_EVENT(ProjectileHitBlockEvent.class),
    PROJECTILE_HIT_ENTITY_EVENT(ProjectileHitEntityEvent.class),
    PROJECTILE_HIT_ENTITY_FINISH_EVENT(ProjectileHitEntityFinishEvent.class),
    PROJECTILE_KILL_ENTITY_EVENT(ProjectileKillEntityEvent.class),
    // shooter
    SHOOTER_DRAW_EVENT(ShooterDrawEvent.class),
    SHOOTER_FIRE_EVENT(ShooterFireEvent.class),
    SHOOTER_GUN_MODIFIER_CACHE_EVENT(ShooterGunModifierCacheEvent.class),
    SHOOTER_INSPECT_EVENT(ShooterInspectEvent.class),
    SHOOTER_PREPARE_MELEE_EVENT(ShooterMeleeEvent.class),
    SHOOTER_RELOAD_EVENT(ShooterReloadEvent.class),
    SHOOTER_RELOAD_FEED_EVENT(ShooterReloadFeedEvent.class),
    SHOOTER_RELOAD_FINISH_EVENT(ShooterReloadFinishEvent.class),
    SHOOTER_SWITCH_AIM_EVENT(ShooterSwitchAimEvent.class),
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
    CustomEventType(Class<? extends ICustomEvent> eventClass) {
        this.eventClass = eventClass;
    }
}
```