package dev.xcolorful.customgun.neoforgeclient.mixin.entity;

import dev.xcolorful.customgun.client.api.entity.IClientGunProjectile;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.model.AmmoModelObject;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAmmoIndexInstance;
import dev.xcolorful.customgun.client.resource.instance.data.ClientGunIndexInstance;
import dev.xcolorful.customgun.core.entity.projectile.GunProjectile;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(GunProjectile.class)
public abstract class GunProjectileMixin implements IClientGunProjectile {

    // --------Cache--------
    private @Nullable ClientGunIndexInstance cgc$clientGunIndexInstanceCache;
    private @Nullable GunDisplayInstance cgc$gunDisplayInstanceCache;

    private @Nullable ClientAmmoIndexInstance cgc$clientAmmoIndexInstance;
    private @Nullable AmmoModelObject cgc$clientAmmoEntityModelObjectCache;

    private float cgc$cameraXRot;
    private float cgc$cameraYRot;
    private float @Nullable [] cgc$firstPersonRenderOffset;

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

    @Override public @Nullable ClientGunIndexInstance cgc$getClientGunIndexInstanceCache() {
        return this.cgc$clientGunIndexInstanceCache;
    }
    @Override public @Nullable GunDisplayInstance cgc$getClientGunDisplayInstanceCache() {
        return this.cgc$gunDisplayInstanceCache;
    }

    @Override public void cgc$setClientGunIndexInstanceCache(ClientGunIndexInstance cache) {
        this.cgc$clientGunIndexInstanceCache = cache;
    }
    @Override public void cgc$setClientGunDisplayInstanceCache(GunDisplayInstance cache) {
        this.cgc$gunDisplayInstanceCache = cache;
    }

    // --------IClientGunProjectile--------

    @Override public @Nullable ClientAmmoIndexInstance cgc$getClientAmmoIndexInstanceCache() {
        return this.cgc$clientAmmoIndexInstance;
    }
    @Override public @Nullable AmmoModelObject cgc$getAmmoEntityModelObjectCache() {
        return this.cgc$clientAmmoEntityModelObjectCache;
    }

    @Override public void cgc$setClientAmmoIndexInstance(ClientAmmoIndexInstance clientAmmoIndexInstance) {
        this.cgc$clientAmmoIndexInstance = clientAmmoIndexInstance;
    }
    @Override public void cgc$setAmmoEntityModelObjectCache(AmmoModelObject ammoModelObject) {
        this.cgc$clientAmmoEntityModelObjectCache = ammoModelObject;
    }

    // --------IClientGunProjectileTracer--------

    @Override public float cgc$getCameraXRot() {
        return this.cgc$cameraXRot;
    }
    @Override public float cgc$getCameraYRot() {
        return this.cgc$cameraYRot;
    }
    @Override public float @Nullable [] cgc$getFirstPersonRenderOffset() {
        return this.cgc$firstPersonRenderOffset;
    }
    @Override public int cgc$getTracerColorInt(Entity gunProjectile) {
//        return NBTUtils.getInt(gunProjectile, "tracer_color"); // 默认返回0
        return Color.WHITE.getRGB();
    }
    @Override public float cgc$getTracerScaleModifier(Entity gunProjectile) {
//        return NBTUtils.getFloat(gunProjectile, "tracer_size"); // 默认返回0
        return 1f;
    }

    @Override public void cgc$setCameraXRot(float cameraXRot) {
        this.cgc$cameraXRot = cameraXRot;
    }
    @Override public void cgc$setCameraYRot(float cameraYRot) {
        this.cgc$cameraYRot = cameraYRot;
    }
    @Override public void cgc$setFirstPersonRenderOffset(float[] firstPersonRenderOffset) {
        this.cgc$firstPersonRenderOffset = firstPersonRenderOffset;
    }
    @Override public void cgc$setTracerColorInt(Entity gunProjectile, int color) {
//        NBTUtils.setInt(gunProjectile, "tracer_color", color);
    }
    @Override public void cgc$setTracerScaleModifier(Entity gunProjectile, float modifier) {
//        NBTUtils.setFloat(gunProjectile, "tracer_size", modifier);
    }
}
