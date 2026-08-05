package dev.xcolorful.customgun.core.projectile.effect;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.projectile.effect.IProjectileEffectManager;
import dev.xcolorful.customgun.core.api.projectile.process.IProjectileProcessRuntime;
import net.minecraft.world.entity.Entity;

public class ProjectileEffectManager implements IProjectileEffectManager {
    public static final ProjectileEffectManager INSTANCE = new ProjectileEffectManager();

    protected ProjectileEffectManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ProjectileEffectManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IProjectileEffectRuntime--------

    @Override
    public void impactEffect(IProjectileProcessRuntime.TickContext tickContext,
                             IGunProjectile iGunProjectile, Entity gunProjectile) {
    }

    @Override
    public void moveEffect(IProjectileProcessRuntime.TickContext tickContext,
                           IGunProjectile iGunProjectile, Entity gunProjectile) {
        if (!gunProjectile.level().isClientSide()) return;
        // ----仅逻辑客户端执行----

    }
}
