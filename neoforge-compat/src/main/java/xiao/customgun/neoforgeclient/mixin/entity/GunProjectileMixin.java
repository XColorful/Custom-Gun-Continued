package xiao.customgun.neoforgeclient.mixin.entity;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.api.entity.IClientGunProjectile;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.resource.instance.data.ClientGunIndexInstance;
import xiao.customgun.core.entity.projectile.GunProjectile;

@Mixin(GunProjectile.class)
public abstract class GunProjectileMixin implements IClientGunProjectile {

    // --------Cache--------
    private @Nullable ClientGunIndexInstance cgc$clientGunIndexInstanceCache;
    private @Nullable GunDisplayInstance cgc$gunDisplayInstanceCache;

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
    public @Nullable ClientGunIndexInstance cgc$getClientGunIndexInstanceCache() {
        return this.cgc$clientGunIndexInstanceCache;
    }
    @Override
    public @Nullable GunDisplayInstance cgc$getClientGunDisplayInstanceCache() {
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
