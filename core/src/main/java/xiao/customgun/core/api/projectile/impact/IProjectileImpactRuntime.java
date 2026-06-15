package xiao.customgun.core.api.projectile.impact;

import net.minecraft.world.entity.Entity;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.projectile.ProjectileManagerGroup;
import xiao.customgun.core.api.projectile.process.IProjectileProcessRuntime;

public interface IProjectileImpactRuntime {

    void preImpactTick(ProjectileManagerGroup group, IProjectileProcessRuntime.TickContext tickContext,
                       IGunProjectile iGunProjectile, Entity gunProjectile);

    void impactTick(ProjectileManagerGroup group, IProjectileProcessRuntime.TickContext tickContext,
                    IGunProjectile iGunProjectile, Entity gunProjectile);
}
