package xiao.customgun.core.projectile.process;

import net.minecraft.world.entity.Entity;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.projectile.ProjectileManagerGroup;
import xiao.customgun.core.api.projectile.process.IProjectileProcessManager;

public class ProjectileProcessManager implements IProjectileProcessManager {
    public static final ProjectileProcessManager INSTANCE = new ProjectileProcessManager();

    protected ProjectileProcessManager() {}
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ProjectileProcessManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IProjectileProcessManager

    @Override
    public void processTick(ProjectileManagerGroup group, TickContext tickContext,
                            IGunProjectile iGunProjectile, Entity gunProjectile) {
        iGunProjectile.preImpactTick(group, tickContext, iGunProjectile, gunProjectile);
        if (gunProjectile.isRemoved()) return;

        iGunProjectile.physicTick(group, tickContext, iGunProjectile, gunProjectile);
        iGunProjectile.impactTick(group, tickContext, iGunProjectile, gunProjectile);
        iGunProjectile.physicMove(group, tickContext, iGunProjectile, gunProjectile);

        int lifetimeTicks = iGunProjectile.getLifetimeTicks(gunProjectile) - 1;
        if (lifetimeTicks > 0) iGunProjectile.setLifetimeTicks(gunProjectile, lifetimeTicks);
        else gunProjectile.discard();
    }
}
