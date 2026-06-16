package xiao.customgun.core.api.entity.victim;

import net.minecraft.world.entity.Entity;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;

public interface IBulletVictimEntityImpact {

    /**
     * @return 是否算作"已处理"
     */
    boolean cgc$onProjectileImpact(IProjectilePhysicsRuntime.EntityHitResult entityHitResult,
                                   IGunProjectile iGunProjectile, Entity gunProjectile);
}
