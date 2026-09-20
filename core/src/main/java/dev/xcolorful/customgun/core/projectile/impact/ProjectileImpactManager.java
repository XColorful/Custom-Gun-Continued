package dev.xcolorful.customgun.core.projectile.impact;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.block.IBulletVictimBlock;
import dev.xcolorful.customgun.core.api.block.victim.IBulletVictimBlockGetter;
import dev.xcolorful.customgun.core.api.entity.IBulletVictimEntity;
import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.entity.victim.IBulletVictimEntityGetter;
import dev.xcolorful.customgun.core.api.event.projectile.ProjectileHitBlockEvent;
import dev.xcolorful.customgun.core.api.projectile.impact.IProjectileImpactManager;
import dev.xcolorful.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;
import dev.xcolorful.customgun.core.api.projectile.process.IProjectileProcessRuntime;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.util.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

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
        if (PlannedRefactor.ON_NON_BULLET_VICTIM_HIT) {}
        // TODO ↓暂时先用默认的
        return cgc$onProjectileImpact(entityHitResult, iGunProjectile, gunProjectile);
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
        if (true) return cgc$onProjectileImpact(blockHitResult, iGunProjectile, gunProjectile);
        else return true;
    }

    // --------IProjectileImpactRuntime--------

    @Override
    public void preImpactTick(IProjectileProcessRuntime.TickContext tickContext,
                              IGunProjectile iGunProjectile, Entity gunProjectile) {
        if (tickContext.logicalSide.isClient()) return;
        // ----仅逻辑服务端执行----

        if (true) _TempExplode.preExplode(tickContext, iGunProjectile, gunProjectile);
    }

    @Override
    public void impactTick(IProjectileProcessRuntime.TickContext tickContext,
                           IGunProjectile iGunProjectile, Entity gunProjectile) {
        if (tickContext.logicalSide.isClient()) return;
        // ----仅逻辑服务端执行----

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

    /**
     * 枪射物命中受弹实体的默认逻辑
     * @param iGunProjectile 枪射物
     * @param gunProjectile 枪射物实体
     * @return 是否算作处理了
     */
    @ApiStatus.Internal
    public static boolean cgc$onProjectileImpact(IProjectilePhysicsRuntime.EntityHitResult entityHitResult,
                                                 IGunProjectile iGunProjectile, Entity gunProjectile) {
        boolean processed = _ProjectileHit.onProjectileHitEntity(entityHitResult, iGunProjectile, gunProjectile);
        if (!processed) return false;

        // ↑先出伤再爆炸↓
        if (_TempExplode.isExplode(iGunProjectile, gunProjectile)) {
            EntityUtils.setInvulnerableTime(entityHitResult.entity(), 0);
            _TempExplode.explode(entityHitResult.hitPos(), iGunProjectile, gunProjectile);
        }
        return true;
    }

    // --------IBulletVictimImpactBlock--------

    @ApiStatus.Internal
    public static boolean cgc$onProjectileImpact(BlockHitResult blockHitResult,
                                                 IGunProjectile iGunProjectile, Entity gunProjectile) {
        gunProjectile.setDeltaMovement(blockHitResult.getLocation().subtract(gunProjectile.position()));

        if (true) if (_TempExplode.explode(blockHitResult.getLocation(), iGunProjectile, gunProjectile)) return true;

        // TODO 命中效果 (弹孔/点燃等)
        gunProjectile.discard();
        return true;
    }
}
