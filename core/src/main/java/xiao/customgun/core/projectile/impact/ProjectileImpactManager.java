package xiao.customgun.core.projectile.impact;

import net.minecraft.world.entity.Entity;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.projectile.ProjectileManagerGroup;
import xiao.customgun.core.api.projectile.impact.IProjectileImpactManager;
import xiao.customgun.core.api.projectile.process.IProjectileProcessRuntime;

public class ProjectileImpactManager implements IProjectileImpactManager {
    public static final ProjectileImpactManager INSTANCE = new ProjectileImpactManager();

    protected ProjectileImpactManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ProjectileImpactManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IProjectileImpactRuntime--------

    @Override
    public void preImpactTick(ProjectileManagerGroup group, IProjectileProcessRuntime.TickContext tickContext,
                              IGunProjectile iGunProjectile, Entity gunProjectile) {
    }

    @Override
    public void impactTick(ProjectileManagerGroup group, IProjectileProcessRuntime.TickContext tickContext,
                           IGunProjectile iGunProjectile, Entity gunProjectile) {
        group.projectileEffectManager().impactEffect(group, tickContext, iGunProjectile, gunProjectile);
    }
}
