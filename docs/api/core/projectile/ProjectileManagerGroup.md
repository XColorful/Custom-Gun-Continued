```java
package xiao.customgun.core.api.projectile;

public record ProjectileManagerGroup(String managerGroupTag,
                                     @NotNull IProjectileEffectManager projectileEffectManager,
                                     @NotNull IProjectileImpactManager projectileImpactManager,
                                     @NotNull IProjectilePhysicsManager projectilePhysicsManager,
                                     @NotNull IProjectileProcessManager projectileProcessManager) {
}
```