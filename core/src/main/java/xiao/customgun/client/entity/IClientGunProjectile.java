package xiao.customgun.client.entity;

import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.resource.instance.data.ClientGunIndexInstance;

public interface IClientGunProjectile {

    // --------Getter & Setter--------

    ClientGunIndexInstance cgc$getClientGunIndexInstanceCache();
    GunDisplayInstance cgc$getClientGunDisplayInstanceCache();

    void cgc$setClientGunIndexInstanceCache(ClientGunIndexInstance cache);
    void cgc$setClientGunDisplayInstanceCache(GunDisplayInstance cache);
}
