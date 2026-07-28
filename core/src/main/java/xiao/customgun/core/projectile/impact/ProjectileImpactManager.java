package xiao.customgun.core.projectile.impact;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.block.IBulletVictimBlock;
import xiao.customgun.core.api.block.victim.IBulletVictimBlockGetter;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.IBulletVictimEntity;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.entity.victim.IBulletVictimEntityGetter;
import xiao.customgun.core.api.event.projectile.ProjectileHitBlockEvent;
import xiao.customgun.core.api.event.projectile.ProjectileHitEntityEvent;
import xiao.customgun.core.api.projectile.impact.IProjectileImpactManager;
import xiao.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;
import xiao.customgun.core.api.projectile.process.IProjectileProcessRuntime;
import xiao.customgun.core.developer.PlannedRefactor;

import java.util.List;

public class ProjectileImpactManager implements IProjectileImpactManager {
    public static final ProjectileImpactManager INSTANCE = new ProjectileImpactManager();

    protected ProjectileImpactManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ProjectileImpactManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    /**
     * 方便扩展模组重载
     */
    protected boolean onBulletVictimHit(IProjectilePhysicsRuntime.EntityHitResult entityHitResult, IBulletVictimEntity iBulletVictimEntity,
                                        IGunProjectile iGunProjectile, Entity gunProjectile) {
        return iBulletVictimEntity.cgc$onProjectileImpact(entityHitResult, iGunProjectile, gunProjectile);
    }
    /**
     * 对一般Entity的处理, 属于 {@link IProjectileImpactManager} 级别
     */
    protected boolean onNonBulletVictimHit(IProjectilePhysicsRuntime.EntityHitResult entityHitResult,
                                           IGunProjectile iGunProjectile, Entity gunProjectile) {
        return PlannedRefactor.ON_NON_BULLET_VICTIM_HIT;
    }

    /**
     * 方便扩展模组重载
     */
    protected boolean onBulletVictimHit(BlockHitResult blockHitResult, IBulletVictimBlock iBulletVictimBlock, Block block,
                                        IGunProjectile iGunProjectile, Entity gunProjectile) {
        return iBulletVictimBlock.cgc$onProjectileImpact(blockHitResult, block, iGunProjectile, gunProjectile);
    }
    /**
     * 对一般Block的处理, 属于 {@link IProjectileImpactManager} 级别
     */
    protected boolean onNonBulletVictimHit(BlockHitResult blockHitResult, Block block,
                                           IGunProjectile iGunProjectile, Entity gunProjectile) {
        gunProjectile.setDeltaMovement(blockHitResult.getLocation().subtract(gunProjectile.position()));
        gunProjectile.discard();
        return true;
//        ↑楼上等楼下完成后删除
        // TODO AmmoHitBlockEvent
        // TODO ExplodeUtil
    }

    // --------IProjectileImpactRuntime--------

    @Override
    public void preImpactTick(IProjectileProcessRuntime.TickContext tickContext,
                              IGunProjectile iGunProjectile, Entity gunProjectile) {
        // TODO 爆炸的信息在 什么时候 什么方式 写入比较好? (要通用性抽象)
    }

    @Override
    public void impactTick(IProjectileProcessRuntime.TickContext tickContext,
                           IGunProjectile iGunProjectile, Entity gunProjectile) {
        int pierce = iGunProjectile.getPierce(gunProjectile);
        if (pierce <= 0) {
            gunProjectile.discard();
            return;
        }

        // Entity impact
        List<IProjectilePhysicsRuntime.EntityHitResult> entityHitResults = tickContext.entityHitResults;
        if (entityHitResults != null && !entityHitResults.isEmpty()) {
            int size = entityHitResults.size();
            for (int i = 0; i < size; i++) {

                IProjectilePhysicsRuntime.EntityHitResult entityHitResult = entityHitResults.get(i);
                @Nullable IBulletVictimEntity iBulletVictimEntity = IBulletVictimEntityGetter.fromEntity(entityHitResult.entity());
                boolean processed = iBulletVictimEntity != null
                        ? this.onBulletVictimHit(entityHitResult, iBulletVictimEntity, iGunProjectile, gunProjectile)
                        : this.onNonBulletVictimHit(entityHitResult, iGunProjectile, gunProjectile);

                if (processed) {
                    iGunProjectile.setPierce(gunProjectile, --pierce);
                    tickContext.group.projectileEffectManager().impactEffect(tickContext, iGunProjectile, gunProjectile);
                    if (pierce <= 0) gunProjectile.discard();

                    // 爆炸的boolean复用discard (爆炸时决定是否移除)
                    if (gunProjectile.isRemoved()) return;
                }
            }
        }

        // Block impact
        BlockHitResult blockHitResult = tickContext.blockHitResult;
        if (blockHitResult != null && blockHitResult.getType() != HitResult.Type.MISS) {
            {
                Block block = gunProjectile.level().getBlockState(blockHitResult.getBlockPos()).getBlock();
                @Nullable IBulletVictimBlock iBulletVictimBlock = IBulletVictimBlockGetter.fromBlock(block);

                // 事件钩子
                if (CustomGun.getEventPoster().postCustomEvent(new ProjectileHitBlockEvent(iGunProjectile, gunProjectile, blockHitResult, iBulletVictimBlock, block))) return;

                boolean processed = iBulletVictimBlock != null
                        ? this.onBulletVictimHit(blockHitResult, iBulletVictimBlock, block, iGunProjectile, gunProjectile)
                        : this.onNonBulletVictimHit(blockHitResult, block, iGunProjectile, gunProjectile);
                if (processed) {
                    iGunProjectile.setPierce(gunProjectile, --pierce);
                    tickContext.group.projectileEffectManager().impactEffect(tickContext, iGunProjectile, gunProjectile);
                    if (pierce <= 0) gunProjectile.discard();

                    // 爆炸的boolean复用discard (爆炸时决定是否移除)
                    if (gunProjectile.isRemoved()) return;
                }
            }
        }

        tickContext.deltaMovement = gunProjectile.getDeltaMovement();
    }

    // --------IBulletVictimEntityImpact--------

    public static boolean cgc$onProjectileImpact(IProjectilePhysicsRuntime.EntityHitResult entityHitResult,
                                                 IGunProjectile iGunProjectile, Entity gunProjectile) {
        return true;
    }

    // --------IBulletVictimImpactBlock--------

    public static boolean cgc$onProjectileImpact(BlockHitResult blockHitResult,
                                                 IGunProjectile iGunProjectile, Entity gunProjectile) {
        // TODO
        gunProjectile.setDeltaMovement(blockHitResult.getLocation().subtract(gunProjectile.position()));
        gunProjectile.discard();
        return true;
    }
}
