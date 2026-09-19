package dev.xcolorful.customgun.client.entity.shooter;

import dev.xcolorful.customgun.client.api.entity.LocalShooterProperty;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterAspect;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;

public abstract class LocalShooterAspect {
    @ApiStatus.Internal public static final int RELOAD_COOLDOWN_MS = LivingShooterAspect.RELOAD_COOLDOWN_MS + 50;
    @ApiStatus.Internal public static final int SHOOT_COOLDOWN_MS = 50;
    /**
     * 持枪时的最大俯仰角，非持枪时不使用
     */
    @ApiStatus.Internal public static final float PRONE_PITCH_MAX = 25.0F;
    /**
     * 持枪时的最小俯仰角，非持枪时不使用
     */
    @ApiStatus.Internal public static final float PRONE_PITCH_MIN = -20.0F;

    protected final LocalPlayer localShooter;
    protected final LocalShooterProperty localShooterProperty;

    public LocalShooterAspect(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        this.localShooter = localShooter;
        this.localShooterProperty = localShooterProperty;
    }
}
