package xiao.customgun.core.api.entity;

public final class GunProjectilePropertyTag {

    // --------IGunProjectileDataAccess--------
    public static final String MANAGER_GROUP = "manager_group";
    public static final String GUN_LOCATION = "gun_rl";
    public static final String GUN_DISPLAY_LOCATION = "gun_display_rl";
    public static final String AMMO_LOCATION = "ammo_rl";
    public static final String EXTRA_DATA = "extra_data";

    // --------IGunProjectileStateAccess--------
    public static final String LIFETIME_TICKS = "lifetime_ticks";
    public static final String BULLET_SPEED = "bullet_speed";
    public static final String GRAVITY = "gravity";
    public static final String FRICTION = "friction";
    public static final String IS_TRACER = "is_tracer";
    public static final String FIRE_ASPECT = "fire_aspect";
    public static final String KNOCKBACK_STRENGTH = "knockback_strength";
    public static final String EXTRA_STATE = "extra_state";

    private GunProjectilePropertyTag() {}
}
