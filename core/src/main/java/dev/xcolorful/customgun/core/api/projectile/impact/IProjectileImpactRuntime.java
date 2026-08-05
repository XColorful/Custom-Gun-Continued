package dev.xcolorful.customgun.core.api.projectile.impact;

import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.projectile.process.IProjectileProcessRuntime;
import net.minecraft.world.entity.Entity;

public interface IProjectileImpactRuntime {

    void preImpactTick(IProjectileProcessRuntime.TickContext tickContext,
                       IGunProjectile iGunProjectile, Entity gunProjectile);

    void impactTick(IProjectileProcessRuntime.TickContext tickContext,
                    IGunProjectile iGunProjectile, Entity gunProjectile);
}
