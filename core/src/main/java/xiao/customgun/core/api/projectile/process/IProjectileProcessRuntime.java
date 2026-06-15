package xiao.customgun.core.api.projectile.process;

import net.minecraft.world.entity.Entity;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.projectile.ProjectileManagerGroup;

public interface IProjectileProcessRuntime {

    void processTick(ProjectileManagerGroup group, TickContext tickContext,
                     IGunProjectile iGunProjectile, Entity gunProjectile);

    final class TickContext {
        public TickContext() {
        }
    }
}
