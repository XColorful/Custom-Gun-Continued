/*
 * Go to BattleRoyale GameManager 的设计
 */

package dev.xcolorful.customgun.core.api.projectile;

import org.jetbrains.annotations.NotNull;

public interface IProjectileMainManager extends IProjectileSubManager {

    boolean registerRuntimeGroup(ProjectileManagerGroup projectileManagerGroup);

    @NotNull ProjectileManagerGroup getProjectileManagerGroup(String managerGroupTag);
}
