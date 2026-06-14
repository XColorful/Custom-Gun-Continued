package xiao.customgun.core.api.entity;

import xiao.customgun.core.api.entity.projectile.IGunProjectileDataAccess;
import xiao.customgun.core.api.projectile.IProjectileRuntime;

/*
不用IShootProjectile, 使Shoot跟Shooter -> Shoot绑定
 */
public interface IGunProjectile extends IProjectileRuntime,
        IGunProjectileDataAccess {
}
