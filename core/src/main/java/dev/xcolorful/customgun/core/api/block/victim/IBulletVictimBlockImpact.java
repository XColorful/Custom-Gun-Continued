package dev.xcolorful.customgun.core.api.block.victim;

import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public interface IBulletVictimBlockImpact {

    /**
     * @return 是否算作"已处理"
     */
    boolean cgc$onProjectileImpact(BlockHitResult blockHitResult, Block block,
                                   IGunProjectile iGunProjectile, Entity gunProjectile);
}
