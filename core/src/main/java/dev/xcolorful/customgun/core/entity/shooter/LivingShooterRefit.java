package dev.xcolorful.customgun.core.entity.shooter;

import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;

public class LivingShooterRefit {

    @ApiStatus.Internal
    public static boolean _shouldForceDisableRefit(LivingEntity livingShooter) {
        if (isIllegalRefitState(livingShooter)) return true;

        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(livingShooter);
        ShooterProperty shooterProperty = iLivingShooter.cgc$getShooterProperty();
        if ( // 2.2 检查状态
                // 瞄准（理论上该放行，但能防误触）
                shooterProperty.isAiming
                // 射击
                || iLivingShooter.cgc$getSynShootCooldown() > 0
        ) return true;

        return false;
    }

    public static boolean isIllegalRefitState(LivingEntity livingShooter) {
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(livingShooter);
        if (
                // 装弹
                /*
                防装弹时改配件修饰缓存
                游戏性上也可以放行，比如做个能直接改配件的背包面板
                但是你手都在装弹了，不能配件也还说得过去
                 */
                iLivingShooter.cgc$getSynReloadState().getStateType().isReloading()
        ) return true;

        return false;
    }
}
