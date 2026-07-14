package xiao.customgun.client.api.entity;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.resource.instance.data.ClientGunIndexInstance;

public interface IClientGunProjectile {

    // --------Getter & Setter--------

    @Nullable ClientGunIndexInstance cgc$getClientGunIndexInstanceCache();
    @Nullable GunDisplayInstance cgc$getClientGunDisplayInstanceCache();

    void cgc$setClientGunIndexInstanceCache(ClientGunIndexInstance cache);
    void cgc$setClientGunDisplayInstanceCache(GunDisplayInstance cache);
}
