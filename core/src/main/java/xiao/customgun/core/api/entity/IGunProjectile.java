package xiao.customgun.core.api.entity;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.projectile.IGunProjectileDataAccess;
import xiao.customgun.core.api.projectile.IProjectileRuntime;
import xiao.customgun.core.resource.instance.data.AmmoIndexInstance;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

/*
不用IShootProjectile, 使Shoot跟Shooter -> Shoot绑定

文档译名: 枪射物 (XiaoColorful译); 否决译名: 枪射实体(此处没必要指明Entity)
- "枪射物 (gun projectile)"对应"投掷物 (projectile)"
- "枪射物实体 (gun projectile entity)"对应"投掷物实体 (projectile entity)"
 */
public interface IGunProjectile extends IProjectileRuntime,
        IGunProjectileDataAccess {

    // --------Getter & Setter--------

    @Nullable GunIndexInstance getGunIndexInstanceCache();
    @Nullable AmmoIndexInstance getAmmoIndexInstanceCache();

    void setGunIndexInstanceCache(GunIndexInstance cache);
    void setAmmoIndexInstanceCache(AmmoIndexInstance cache);
}
