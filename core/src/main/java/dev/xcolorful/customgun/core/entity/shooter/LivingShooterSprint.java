package dev.xcolorful.customgun.core.entity.shooter;

import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ReloadState;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.config.SyncConfig;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class LivingShooterSprint extends LivingShooterAspect {

    public LivingShooterSprint(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    /**
     * 根据情况返回玩家应当处于的冲刺状态
     * @param isSprint 当前冲刺状态
     * @return 当前是否应该处于冲刺状态
     */
    public boolean onSprint(boolean isSprint) {
        ILivingShooter livingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        if ( // 2.2 检查状态
                // 禁止疾跑的状态
                _shouldForceDisableSprint(livingShooter, isSprint)
        ) return false;

        return isSprint;
    }
    @ApiStatus.Internal
    public boolean _shouldForceDisableSprint(ILivingShooter iLivingShooter, boolean isSprint) {
        if (isIllegalSprintState(iLivingShooter)) return true;

        return false;
    }
    /**
     * 用于排除一定不允许冲刺的情况
     * @return 是否不可能(是否禁止)处于冲刺状态
     */
    @ApiStatus.Internal
    public static boolean isIllegalSprintState(ILivingShooter iLivingShooter) {
        if (!SyncConfig.ENABLE_SPRINT_WHILE_AIMING.get()) {
            if (iLivingShooter.cgc$getSynIsAiming()) return true;
        }

        if (!SyncConfig.ENABLE_SPRINT_WHILE_RELOADING.get()) {
            ReloadState.StateType reloadStateType = iLivingShooter.cgc$getSynReloadState().getStateType();
            if (reloadStateType.isReloading() && !reloadStateType.isReloadFinishing()) return true;
        }

        return false;
    }

    /**
     * 上次执行{@link #tickSprint()}的时间戳，用于计算疾跑时间变化量
     */
    private long lastSprintTickTimestamp = -1;
    public void tickSprint() {
        long currentTimeMillis = System.currentTimeMillis();

        // 初始化时间戳
        if (this.lastSprintTickTimestamp < 0) this.lastSprintTickTimestamp = currentTimeMillis;

        _doTickSprint(currentTimeMillis);

        // 设置sprint时间戳
        this.lastSprintTickTimestamp = currentTimeMillis;
    }
    private void _doTickSprint(long currentTimeMillis) {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return;
        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        @Nullable IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return;

        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        ReloadState.StateType reloadStateType = iLivingShooter.cgc$getSynReloadState().getStateType();
        if ( // 2.2 禁止疾跑
                // 瞄准状态
                (this.shooterProperty.aimingProgress > 0 && !SyncConfig.ENABLE_SPRINT_WHILE_AIMING.get())
                // 换弹中
                || (reloadStateType.isReloading() && !reloadStateType.isReloadFinishing() && !SyncConfig.ENABLE_SPRINT_WHILE_RELOADING.get())
        ) {
            this.livingShooter.setSprinting(false);
        }

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(currentGunItem));
        if (gunIndexInstance == null) {
            this.shooterProperty.sprintTimeS = 0;
            return;
        }

        // 当前tick时间戳-上次tick时间戳
        float alphaGunSprintTime = (currentTimeMillis - this.lastSprintTickTimestamp) / 1000f;
        boolean isSprinting = this.livingShooter.isSprinting() && !this.livingShooter.isCrouching();
        float sprintTimeS = this.shooterProperty.sprintTimeS + (isSprinting ? alphaGunSprintTime : -alphaGunSprintTime);

        GunData gunData = gunIndexInstance.getGunData();
        float gunSprintSwitchTime = gunData.getSprintSwitchTime();
        this.shooterProperty.sprintTimeS = Mth.clamp(sprintTimeS, 0, gunSprintSwitchTime);
    }
}
