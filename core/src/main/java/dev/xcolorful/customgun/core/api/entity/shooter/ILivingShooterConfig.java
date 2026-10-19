package dev.xcolorful.customgun.core.api.entity.shooter;

public interface ILivingShooterConfig {

    default boolean cgc$enableDefaultLivingShooterTick() {
        return false;
    }
}
