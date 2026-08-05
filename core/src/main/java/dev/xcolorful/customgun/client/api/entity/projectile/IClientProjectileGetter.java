package dev.xcolorful.customgun.client.api.entity.projectile;

import dev.xcolorful.customgun.client.api.entity.IClientGunProjectile;
import dev.xcolorful.customgun.core.entity.projectile.GunProjectile;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IClientProjectileGetter {

    /**
     * mixin实现的扩展接口
     */
    static @NotNull IClientGunProjectile fromGunProjectile(@NotNull GunProjectile gunProjectile) {
        return (IClientGunProjectile) gunProjectile;
    }

    static @Nullable IClientGunProjectile fromEntity(@Nullable Entity gunProjectile) {
        if (gunProjectile == null) return null;
        return gunProjectile instanceof IClientGunProjectile iClientGunProjectile ? iClientGunProjectile : null;
    }
}
