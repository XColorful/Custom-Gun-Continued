package dev.xcolorful.customgun.client.api.entity.projectile;

import dev.xcolorful.customgun.client.model.AmmoModelObject;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAmmoIndexInstance;
import org.jetbrains.annotations.Nullable;

public interface IClientGunProjectileAmmo {

    // --------Getter & Setter--------

    @Nullable ClientAmmoIndexInstance cgc$getClientAmmoIndexInstanceCache();
    @Nullable AmmoModelObject cgc$getAmmoEntityModelObjectCache();

    void cgc$setClientAmmoIndexInstance(ClientAmmoIndexInstance clientAmmoIndexInstance);
    void cgc$setAmmoEntityModelObjectCache(AmmoModelObject ammoModelObject);
}
