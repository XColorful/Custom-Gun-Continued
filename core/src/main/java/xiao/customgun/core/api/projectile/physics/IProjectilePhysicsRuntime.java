package xiao.customgun.core.api.projectile.physics;

import net.minecraft.world.entity.Entity;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.projectile.ProjectileManagerGroup;
import xiao.customgun.core.api.projectile.process.IProjectileProcessRuntime;

public interface IProjectilePhysicsRuntime {

    void physicTick(ProjectileManagerGroup group, IProjectileProcessRuntime.TickContext tickContext,
                    IGunProjectile iGunProjectile, Entity gunProjectile);

    void physicMove(ProjectileManagerGroup group, IProjectileProcessRuntime.TickContext tickContext,
                    IGunProjectile iGunProjectile, Entity gunProjectile);
}
