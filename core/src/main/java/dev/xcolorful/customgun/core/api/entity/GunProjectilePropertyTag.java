package dev.xcolorful.customgun.core.api.entity;

public final class GunProjectilePropertyTag {

    // --------IGunProjectileDataAccess--------
    public static final String MANAGER_GROUP = "manager_group";
    public static final String GUN_LOCATION = "gun_rl";
    public static final String GUN_DISPLAY_LOCATION = "gun_display_rl";
    public static final String AMMO_LOCATION = "ammo_rl";
    public static final String EXTRA_DATA = "extra_data";

    // --------IGunProjectileStateAccess--------
    public static final String SHOOT_POS = "shoot_pos";
    public static final String ARMOR_IGNORE_PERCENT = "armor_ignore_percent";
    public static final String HEADSHOT_MULTIPLIER = "headshot_multiplier";
    public static final String DAMAGE_CALCULATION = "damage_calculation";
    public static final String LIFETIME_TICKS = "lifetime_ticks";
    public static final String BULLET_SPEED = "bullet_speed";
    public static final String GRAVITY = "gravity";
    public static final String FRICTION = "friction";
    public static final String PIERCE = "pierce";
    public static final String IS_TRACER = "is_tracer";
    public static final String FIRE_ASPECT = "fire_aspect";
    public static final String FIRE_ASPECT_SECONDS = "fire_aspect_seconds";
    public static final String KNOCKBACK_STRENGTH = "knockback_strength";
    public static final String EXTRA_STATE = "extra_state";
    public static final String EXPLOSION_DATA = "explosion_data";

    private GunProjectilePropertyTag() {}
}
