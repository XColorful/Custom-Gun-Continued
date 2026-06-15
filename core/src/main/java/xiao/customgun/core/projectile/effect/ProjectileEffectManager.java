package xiao.customgun.core.projectile.effect;

import net.minecraft.world.entity.Entity;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.projectile.ProjectileManagerGroup;
import xiao.customgun.core.api.projectile.effect.IProjectileEffectManager;
import xiao.customgun.core.api.projectile.process.IProjectileProcessRuntime;

public class ProjectileEffectManager implements IProjectileEffectManager {
    public static final ProjectileEffectManager INSTANCE = new ProjectileEffectManager();

    protected ProjectileEffectManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ProjectileEffectManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IProjectileEffectRuntime--------

    @Override
    public void impactEffect(ProjectileManagerGroup group, IProjectileProcessRuntime.TickContext tickContext,
                             IGunProjectile iGunProjectile, Entity gunProjectile) {
    }

    @Override
    public void moveEffect(ProjectileManagerGroup group, IProjectileProcessRuntime.TickContext tickContext,
                           IGunProjectile iGunProjectile, Entity gunProjectile) {
    }
}
