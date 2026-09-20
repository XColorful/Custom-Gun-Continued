package dev.xcolorful.customgun.core.api.entity.victim;

import dev.xcolorful.customgun.core.api.entity.IGunProjectile;

public interface IBulletVictimVision {

    /**
     * @return 被枪射物{@link IGunProjectile}命中时，是否能触发特殊视觉效果 (如弹孔，受伤粒子)
     */
    default boolean cgc$hasProjectileHitVisual() {
        return true;
    }
}
