package dev.xcolorful.customgun.client.entity.shooter;

import dev.xcolorful.customgun.client.api.entity.ILocalShooter;
import dev.xcolorful.customgun.client.api.entity.LocalShooterProperty;
import dev.xcolorful.customgun.client.api.entity.shooter.ILocalShooterGetter;
import dev.xcolorful.customgun.client.entity.shooter.player._LocalAnimHandler;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterRefit;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;

public class LocalShooterRefit {

    @ApiStatus.Internal public static final String REFIT_CHECK_OPERATION = "LocalShooterRefit#_shouldForceDisableRefit";
    /**
     * 对应 {@link LivingShooterRefit#_shouldForceDisableRefit}
     * <li>同时也对应 {@link _LocalAnimHandler#_isActionBusy} 的动画状态拦截</li>
     */
    @ApiStatus.Internal
    public static boolean _shouldForceDisableRefit(LocalPlayer localShooter) {
        if (LivingShooterRefit.isIllegalRefitState(localShooter)) return true;

        ILocalShooter iLocalShooter = ILocalShooterGetter.fromLocalPlayer(localShooter);
        LocalShooterProperty localShooterProperty = iLocalShooter.cgc$getLocalShooterProperty();
        if ( // 检查状态锁
                localShooterProperty.clientStateLock(REFIT_CHECK_OPERATION)
                || localShooterProperty.isCharging
        ) return true;
        if ( // 2.2 检查状态
                // 瞄准（理论上该放行，但能防误触）
                iLocalShooter.cgc$isAim()
                // 射击
                || iLocalShooter.cgc$getShootCooldown() > 0
        ) return true;

        return false;
    }
}
