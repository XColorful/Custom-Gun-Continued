package xiao.customgun.core.api.entity;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.projectile.IGunProjectileDataAccess;
import xiao.customgun.core.api.projectile.IProjectileRuntime;
import xiao.customgun.core.resource.instance.data.AmmoIndexInstance;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

/*
不用IShootProjectile, 使Shoot跟Shooter -> Shoot绑定
 */
public interface IGunProjectile extends IProjectileRuntime,
        IGunProjectileDataAccess {

    // --------Getter & Setter--------

    @Nullable GunIndexInstance getGunIndexInstanceCache();
    @Nullable AmmoIndexInstance getAmmoIndexInstanceCache();

    void setGunIndexInstanceCache(GunIndexInstance cache);
    void setAmmoIndexInstanceCache(AmmoIndexInstance cache);
}
