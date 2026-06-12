package xiao.customgun.core.api.projectile;

import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.projectile.effect.IProjectileEffectManager;
import xiao.customgun.core.api.projectile.impact.IProjectileImpactManager;
import xiao.customgun.core.api.projectile.physics.IProjectilePhysicsManager;

public record ProjectileManagerGroup(String managerGroupTag,
                                     @NotNull IProjectileEffectManager projectileEffectManager,
                                     @NotNull IProjectileImpactManager projectileImpactManager,
                                     @NotNull IProjectilePhysicsManager projectilePhysicsManager) {
}
