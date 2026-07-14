```java
package xiao.customgun.core.api.projectile;

public interface IProjectileMainManager extends IProjectileSubManager {
    boolean registerRuntimeGroup(ProjectileManagerGroup projectileManagerGroup);
    @NotNull ProjectileManagerGroup getProjectileManagerGroup(String managerGroupTag);
}
```