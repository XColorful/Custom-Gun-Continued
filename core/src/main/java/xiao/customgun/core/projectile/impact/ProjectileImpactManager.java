package xiao.customgun.core.projectile.impact;

import xiao.customgun.CustomGun;
import xiao.customgun.core.api.projectile.impact.IProjectileImpactManager;

public class ProjectileImpactManager implements IProjectileImpactManager {
    public static final ProjectileImpactManager INSTANCE = new ProjectileImpactManager();

    protected ProjectileImpactManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ProjectileImpactManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }
}
