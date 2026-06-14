package xiao.customgun.forgeclient.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.entity.IClientGunProjectile;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.resource.instance.data.ClientGunIndexInstance;
import xiao.customgun.core.entity.projectile.GunProjectile;

@Mixin(GunProjectile.class)
public abstract class GunProjectileMixin implements IClientGunProjectile {

    // --------Cache--------
    public ClientGunIndexInstance cgc$clientGunIndexInstanceCache;
    public GunDisplayInstance cgc$gunDisplayInstanceCache;

    @Inject(method = "rebuildCache()V",
            at = @At("TAIL"),
            remap = false
    )
    private void cgc$rebuildCache(CallbackInfo ci) {
        GunProjectile gunProjectile = (GunProjectile) (Object) this;
        this.cgc$clientGunIndexInstanceCache = ClientResourceApi.getClientGunIndexInstance(gunProjectile.getGunLocation(gunProjectile));
        this.cgc$gunDisplayInstanceCache = ClientResourceApi.getGunDisplayInstance(gunProjectile.getGunDisplayLocation(gunProjectile));
    }

    // --------IClientGunProjectile--------

    @Override
    public ClientGunIndexInstance cgc$getClientGunIndexInstanceCache() {
        return this.cgc$clientGunIndexInstanceCache;
    }
    @Override
    public GunDisplayInstance cgc$getClientGunDisplayInstanceCache() {
        return this.cgc$gunDisplayInstanceCache;
    }

    @Override
    public void cgc$setClientGunIndexInstanceCache(ClientGunIndexInstance cache) {
        this.cgc$clientGunIndexInstanceCache = cache;
    }
    @Override
    public void cgc$setClientGunDisplayInstanceCache(GunDisplayInstance cache) {
        this.cgc$gunDisplayInstanceCache = cache;
    }
}
