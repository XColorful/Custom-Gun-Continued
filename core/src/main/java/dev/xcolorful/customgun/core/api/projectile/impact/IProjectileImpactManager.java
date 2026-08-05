package dev.xcolorful.customgun.core.api.projectile.impact;

import dev.xcolorful.customgun.core.api.projectile.IProjectileSubManager;

/*
Effect是效果(偏视觉/炫技), Impact是影响(实际出伤/触发机制)
Hit/Damage属于Impact
 */
public interface IProjectileImpactManager extends IProjectileSubManager, IProjectileImpactRuntime {
}
