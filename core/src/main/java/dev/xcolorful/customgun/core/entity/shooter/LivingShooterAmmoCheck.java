package dev.xcolorful.customgun.core.entity.shooter;

import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.config.GunConfig;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class LivingShooterAmmoCheck extends LivingShooterAspect {

    public LivingShooterAmmoCheck(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public boolean hasInfiniteAmmoFeed() {
        if (PlannedRefactor.ON_CREATIVE_NO_AMMO_CHECK) return false;
        // 创造模式玩家有无限弹药供应
        return (this.livingShooter instanceof Player player) && player.isCreative();
    }

    public boolean bypassGunFireConsumption() {
        // 默认仅创造模式玩家
        return this.livingShooter instanceof Player player && player.isCreative()
                && GunConfig.BYPASS_GUN_FIRE_CONSUMPTION.get();
    }
}
