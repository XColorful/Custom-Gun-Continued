package dev.xcolorful.customgun.core.api.event.projectile;

import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IGunProjectileEvent {

    @Nullable IGunProjectile getIGunProjectile();
    @Nullable Entity getGunProjectile();

    // --------便利方法--------

    default @NotNull ResourceLocation getGunLocation() {
        IGunProjectile iGunProjectile = this.getIGunProjectile();
        return iGunProjectile != null ? iGunProjectile.getGunLocation(this.getGunProjectile()) : ResourceTag.NULL_LOCATION;
    }
    default @NotNull ResourceLocation getDisplayLocation() {
        IGunProjectile iGunProjectile = this.getIGunProjectile();
        return iGunProjectile != null ? iGunProjectile.getGunDisplayLocation(this.getGunProjectile()) : ResourceTag.NULL_LOCATION;
    }

    // --------Deprecated--------

    @Deprecated default @NotNull ResourceLocation getGunId() {
        return this.getGunLocation();
    }
    @Deprecated default @NotNull ResourceLocation getDisplayId() {
        return this.getDisplayLocation();
    }
}
