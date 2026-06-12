package xiao.customgun.core.api.projectile;

import xiao.customgun.core.api.projectile.effect.IProjectileEffectRuntime;
import xiao.customgun.core.api.projectile.impact.IProjectileImpactRuntime;
import xiao.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;

public interface IProjectileRuntime extends
        IProjectileEffectRuntime,
        IProjectileImpactRuntime,
        IProjectilePhysicsRuntime {
}
