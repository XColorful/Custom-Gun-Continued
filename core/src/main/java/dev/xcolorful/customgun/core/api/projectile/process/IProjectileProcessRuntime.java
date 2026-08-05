package dev.xcolorful.customgun.core.api.projectile.process;

import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.projectile.ProjectileManagerGroup;
import dev.xcolorful.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface IProjectileProcessRuntime {

    void processTick(TickContext tickContext,
                     IGunProjectile iGunProjectile, Entity gunProjectile);

    final class TickContext {
        public final ProjectileManagerGroup group;
        // ----preImpactTick----
        // ----physicTick----
        public Vec3 startPos;
        public Vec3 deltaMovement;
        public Vec3 endPos;
        public BlockHitResult blockHitResult;
        public List<IProjectilePhysicsRuntime.EntityHitResult> entityHitResults;
        // ----impactTick----
        // ----physicMove----

        public TickContext(ProjectileManagerGroup group) {
            this.group = group;
        }
    }
}
