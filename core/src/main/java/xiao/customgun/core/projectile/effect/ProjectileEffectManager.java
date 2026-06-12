package xiao.customgun.core.projectile.effect;

import xiao.customgun.CustomGun;
import xiao.customgun.core.api.projectile.effect.IProjectileEffectManager;

public class ProjectileEffectManager implements IProjectileEffectManager {
    public static final ProjectileEffectManager INSTANCE = new ProjectileEffectManager();

    protected ProjectileEffectManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ProjectileEffectManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }
}
