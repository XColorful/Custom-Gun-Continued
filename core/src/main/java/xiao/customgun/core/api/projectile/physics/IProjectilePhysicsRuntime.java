package xiao.customgun.core.api.projectile.physics;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.projectile.process.IProjectileProcessRuntime;

public interface IProjectilePhysicsRuntime extends IProjectilePhysicsExtension {

    void physicTick(IProjectileProcessRuntime.TickContext tickContext,
                    IGunProjectile iGunProjectile, Entity gunProjectile);

    void physicMove(IProjectileProcessRuntime.TickContext tickContext,
                    IGunProjectile iGunProjectile, Entity gunProjectile);

    /**
     * @param entity 服务端{@link NotNull}, 客户端{@link Nullable}
     * @param hitPos 服务端{@link NotNull}, 客户端{@link Nullable}
     * @param headshot
     */
    record EntityHitResult(Entity entity,
                           Vec3 hitPos,
                           boolean headshot) {
    }
}
