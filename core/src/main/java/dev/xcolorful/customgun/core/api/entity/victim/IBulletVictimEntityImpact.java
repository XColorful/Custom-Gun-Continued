package dev.xcolorful.customgun.core.api.entity.victim;

import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;
import net.minecraft.world.entity.Entity;

public interface IBulletVictimEntityImpact {

    /**
     * @return 是否算作"已处理"
     */
    boolean cgc$onProjectileImpact(IProjectilePhysicsRuntime.EntityHitResult entityHitResult,
                                   IGunProjectile iGunProjectile, Entity gunProjectile);
}
