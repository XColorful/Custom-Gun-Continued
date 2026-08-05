package dev.xcolorful.customgun.core.api.entity.projectile;

import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface IGunProjectileGetter {

    static @Nullable IGunProjectile fromEntity(@Nullable Entity gunProjectile) {
        if (gunProjectile == null) return null;
        return gunProjectile instanceof IGunProjectile iGunProjectile ? iGunProjectile : null;
    }
}
