package dev.xcolorful.customgun.core.api.projectile;

import dev.xcolorful.customgun.core.api.projectile.effect.IProjectileEffectManager;
import dev.xcolorful.customgun.core.api.projectile.impact.IProjectileImpactManager;
import dev.xcolorful.customgun.core.api.projectile.physics.IProjectilePhysicsManager;
import dev.xcolorful.customgun.core.api.projectile.process.IProjectileProcessManager;
import org.jetbrains.annotations.NotNull;

public record ProjectileManagerGroup(String managerGroupTag,
                                     @NotNull IProjectileEffectManager projectileEffectManager,
                                     @NotNull IProjectileImpactManager projectileImpactManager,
                                     @NotNull IProjectilePhysicsManager projectilePhysicsManager,
                                     @NotNull IProjectileProcessManager projectileProcessManager) {
}
