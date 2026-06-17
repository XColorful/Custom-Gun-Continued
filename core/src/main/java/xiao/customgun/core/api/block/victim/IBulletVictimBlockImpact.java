package xiao.customgun.core.api.block.victim;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import xiao.customgun.core.api.entity.IGunProjectile;

public interface IBulletVictimBlockImpact {

    /**
     * @return 是否算作"已处理"
     */
    boolean cgc$onProjectileImpact(BlockHitResult blockHitResult,
                                   IGunProjectile iGunProjectile, Entity gunProjectile);
}
