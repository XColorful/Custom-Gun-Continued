package dev.xcolorful.customgun.client.api.entity;

import dev.xcolorful.customgun.client.api.entity.projectile.IClientGunProjectileAmmo;
import dev.xcolorful.customgun.client.api.entity.projectile.IClientGunProjectileTracer;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.resource.instance.data.ClientGunIndexInstance;
import org.jetbrains.annotations.Nullable;

public interface IClientGunProjectile extends IClientGunProjectileAmmo, IClientGunProjectileTracer {

    // --------Getter & Setter--------

    @Nullable ClientGunIndexInstance cgc$getClientGunIndexInstanceCache();
    @Nullable GunDisplayInstance cgc$getClientGunDisplayInstanceCache();

    void cgc$setClientGunIndexInstanceCache(ClientGunIndexInstance cache);
    void cgc$setClientGunDisplayInstanceCache(GunDisplayInstance cache);
}
