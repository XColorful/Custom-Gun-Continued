package dev.xcolorful.customgun.core.api.projectile.effect;

import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.projectile.process.IProjectileProcessRuntime;
import net.minecraft.world.entity.Entity;

public interface IProjectileEffectRuntime {

    void impactEffect(IProjectileProcessRuntime.TickContext tickContext,
                      IGunProjectile iGunProjectile, Entity gunProjectile);

    void moveEffect(IProjectileProcessRuntime.TickContext tickContext,
                    IGunProjectile iGunProjectile, Entity gunProjectile);
}
