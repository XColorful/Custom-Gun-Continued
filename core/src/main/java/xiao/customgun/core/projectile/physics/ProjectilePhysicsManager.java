package xiao.customgun.core.projectile.physics;

import xiao.customgun.CustomGun;
import xiao.customgun.core.api.projectile.physics.IProjectilePhysicsManager;

public class ProjectilePhysicsManager implements IProjectilePhysicsManager {
    public static final ProjectilePhysicsManager INSTANCE = new ProjectilePhysicsManager();

    protected ProjectilePhysicsManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ProjectilePhysicsManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }
}
