package xiao.customgun.core.api.projectile.physics;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.projectile.process.IProjectileProcessRuntime;

public interface IProjectilePhysicsRuntime {

    void physicTick(IProjectileProcessRuntime.TickContext tickContext,
                    IGunProjectile iGunProjectile, Entity gunProjectile);

    void physicMove(IProjectileProcessRuntime.TickContext tickContext,
                    IGunProjectile iGunProjectile, Entity gunProjectile);

    record EntityHitResult(Entity entity,
                           Vec3 hitPos,
                           boolean headshot) {
    }
}
