package xiao.customgun.core.api.event.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.event.CustomEvent;

/**
 * 枪射物{@link IGunProjectile} 事件
 */
public abstract class GunProjectileEvent extends CustomEvent implements IGunProjectileEvent {

    protected @Nullable IGunProjectile igunProjectile;
    protected @Nullable Entity gunProjectile;

    protected GunProjectileEvent(@Nullable IGunProjectile iGunProjectile, @Nullable Entity gunProjectile) {
        this.igunProjectile = iGunProjectile;
        this.gunProjectile = gunProjectile;
    }

    public @Nullable IGunProjectile getIGunProjectile() {
        return this.igunProjectile;
    }
    public @Nullable Entity getGunProjectile() {
        return this.gunProjectile;
    }

    public @Nullable Level getLevel() {
        return this.gunProjectile != null ? gunProjectile.level() : null;
    }
}
