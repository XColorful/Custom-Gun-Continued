package dev.xcolorful.customgun.client.api.entity.shooter;

public interface ILocalShooterState {

    /**
     * 客户端是否处于瞄准状态
     */
    boolean cgc$isAim();

    /**
     * 是否爬行
     */
    boolean cgc$isProne();

    boolean cgc$isReadyToDraw();

    boolean cgc$isCharging();

    /**
     * 客户端瞄准进度
     * @return 0-1，1 代表开镜进度到 100%
     */
    float cgc$getAimingProgress();
    /**
     * 客户端渲染用的瞄准进度
     * @return 0-1，1 代表开镜进度到 100%
     */
    float cgc$getRenderAimingProgress(float partialTicks);

    /**
     * 客户端射击冷却时间
     * @return <=0即无冷却; >0为有冷却
     */
    long cgc$getShootCooldown();

    float cgc$getChargeProgress();
}