package xiao.customgun.core.api.entity.projectile;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.IGunProjectile;

public interface IGunProjectileGetter {

    static @Nullable IGunProjectile fromEntity(@Nullable Entity gunProjectile) {
        if (gunProjectile == null) return null;
        return gunProjectile instanceof IGunProjectile iGunProjectile ? iGunProjectile : null;
    }
}
