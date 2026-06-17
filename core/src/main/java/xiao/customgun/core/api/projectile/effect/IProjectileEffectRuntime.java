package xiao.customgun.core.api.projectile.effect;

import net.minecraft.world.entity.Entity;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.projectile.process.IProjectileProcessRuntime;

public interface IProjectileEffectRuntime {

    void impactEffect(IProjectileProcessRuntime.TickContext tickContext,
                      IGunProjectile iGunProjectile, Entity gunProjectile);

    void moveEffect(IProjectileProcessRuntime.TickContext tickContext,
                    IGunProjectile iGunProjectile, Entity gunProjectile);
}
