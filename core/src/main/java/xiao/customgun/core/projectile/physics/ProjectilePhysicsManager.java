package xiao.customgun.core.projectile.physics;

import net.minecraft.world.entity.Entity;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.projectile.ProjectileManagerGroup;
import xiao.customgun.core.api.projectile.physics.IProjectilePhysicsManager;
import xiao.customgun.core.api.projectile.process.IProjectileProcessRuntime;

public class ProjectilePhysicsManager implements IProjectilePhysicsManager {
    public static final ProjectilePhysicsManager INSTANCE = new ProjectilePhysicsManager();

    protected ProjectilePhysicsManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ProjectilePhysicsManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IProjectilePhysicsRuntime--------

    @Override
    public void physicTick(ProjectileManagerGroup group, IProjectileProcessRuntime.TickContext tickContext,
                           IGunProjectile iGunProjectile, Entity gunProjectile) {
    }

    @Override
    public void physicMove(ProjectileManagerGroup group, IProjectileProcessRuntime.TickContext tickContext,
                           IGunProjectile iGunProjectile, Entity gunProjectie) {
        group.projectileEffectManager().moveEffect(group, tickContext, iGunProjectile, gunProjectie);
    }
}
