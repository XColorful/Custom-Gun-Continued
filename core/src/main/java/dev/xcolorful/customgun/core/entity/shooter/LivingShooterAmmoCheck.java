package dev.xcolorful.customgun.core.entity.shooter;

import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.config.GunConfig;
import dev.xcolorful.customgun.core.config.ShooterConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class LivingShooterAmmoCheck extends LivingShooterAspect {

    public LivingShooterAmmoCheck(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public boolean hasInfiniteAmmoFeed() {
        if (ShooterConfig.CREATIVE_MODE_INFINITE_FEED.get()) {
            // 创造模式玩家有无限弹药供应
            if (this.livingShooter instanceof Player player && player.isCreative()) return true;
        }

        return false;
    }

    public boolean bypassGunFireConsumption() {
        if (GunConfig.BYPASS_GUN_FIRE_CONSUMPTION.get()) {
            // 默认仅创造模式玩家
            if (this.livingShooter instanceof Player player && player.isCreative()) return true;
        }

        return false;
    }
}
