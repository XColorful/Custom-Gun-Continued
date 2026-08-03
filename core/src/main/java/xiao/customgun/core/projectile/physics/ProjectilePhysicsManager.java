/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.projectile.physics;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.minecraft.IMcRegistry;
import xiao.customgun.core.api.projectile.physics.IProjectilePhysicsManager;
import xiao.customgun.core.api.projectile.process.IProjectileProcessRuntime;
import xiao.customgun.core.config.AmmoConfig;
import xiao.customgun.core.config.sync.HeadAABBData;
import xiao.customgun.core.entity.projectile.GunProjectile;
import xiao.customgun.core.init.registry.ModBlocks;
import xiao.customgun.core.util.RayTraceUtils;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class ProjectilePhysicsManager implements IProjectilePhysicsManager {
    public static final ProjectilePhysicsManager INSTANCE = new ProjectilePhysicsManager();

    /**
     * 构造子弹散布方向时使用的前向基准分量
     * <p>
     * 该值用于平衡散布偏移与前向方向的比例，不代表实际飞行距离
     * 在方向向量归一化后，仅影响散布角度大小
     */
    @ApiStatus.Internal
    public static final double SPREAD_FORWARD_DISTANCE = 8.0D;

    public static Predicate<Entity> PROJECTILE_TARGETS =
            input -> input != null
                    && input.isAlive()
                    && input.isPickable()
                    && !input.isSpectator();

    protected ProjectilePhysicsManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, ProjectilePhysicsManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IProjectilePhysicsRuntime--------

    @Override
    public void physicTick(IProjectileProcessRuntime.TickContext tickContext,
                           IGunProjectile iGunProjectile, Entity gunProjectile) {
        tickContext.startPos = gunProjectile.position();
        tickContext.deltaMovement = gunProjectile.getDeltaMovement();
        tickContext.endPos = tickContext.startPos.add(tickContext.deltaMovement);

        // ----Block射线检测----

        ClipContext context = new ClipContext(tickContext.startPos, tickContext.endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, gunProjectile);
        IMcRegistry mcRegistry = CustomGun.getMcRegistry();
        List<String> ignoreBlocks = AmmoConfig.PASS_THROUGH_BLOCKS.get();
        BlockHitResult blockHitResult = RayTraceUtils.BlockTrace.rayTraceBlocksWithFilter(
                gunProjectile.level(),
                context.getFrom(),
                context.getTo(),
                (blockState, blockPos) -> context.getBlockShape(blockState, gunProjectile.level(), blockPos),
                (fluidState, blockPos) -> context.getFluidShape(fluidState, gunProjectile.level(), blockPos),
                blockState -> {
                    if (blockState == null) {
                        return false;
                    }
                    // 检查是否属于配置文件中的穿透方块
                    var blockRl = mcRegistry.getBlockRl(blockState.getBlock());
                    if (blockRl != null && ignoreBlocks.contains(blockRl.toString())) {
                        return true; // 返回 true 代表需要忽略该方块（即穿透）
                    }
                    // 检查是否包含忽略标签
                    return blockState.is(ModBlocks.BULLET_IGNORE_BLOCKS);
                }
        );
        if (blockHitResult.getType() != HitResult.Type.MISS) {
            tickContext.endPos = blockHitResult.getLocation();
        }
        tickContext.blockHitResult = blockHitResult;

        // ----Entity射线检测----

        tickContext.entityHitResults = RayTraceUtils.EntityTrace.rayTraceEntities(
                gunProjectile,
                gunProjectile instanceof Projectile projectile ? projectile.getOwner() : null,
                tickContext.startPos,
                tickContext.endPos,
                1.0,
                PROJECTILE_TARGETS,
                (entity, hitPos) -> {
                    boolean headshot = this.checkHeadshot(entity, hitPos);
                    return this.createWithFilter(entity, hitPos, headshot, gunProjectile);
                }
        );
        // 按距离排序，使 for (int i = 0) 就从近到远
        // 放在Impact里处理的话职责越界
        this.sortHitResults(tickContext.entityHitResults, tickContext.startPos);
    }

    @Override
    public void physicMove(IProjectileProcessRuntime.TickContext tickContext,
                           IGunProjectile iGunProjectile, Entity gunProjectile) {
        Vec3 thisTickMovement = tickContext.deltaMovement;

        // ----暂时照搬原版----
        Entity _this = gunProjectile;
        double x = thisTickMovement.x;
        double y = thisTickMovement.y;
        double z = thisTickMovement.z;
        double distance = thisTickMovement.horizontalDistance();
        gunProjectile.setYRot((float) Math.toDegrees(Mth.atan2(x, z)));
        _this.setXRot((float) Math.toDegrees(Mth.atan2(y, distance)));
        // 初始朝向设置
        if (_this.xRotO == 0.0F && _this.yRotO == 0.0F) {
            _this.yRotO = _this.getYRot();
            _this.xRotO = _this.getXRot();
        }
        // 运动时的旋转（不包含自转）
        _this.setXRot(GunProjectile.lerpRotation(_this.xRotO, _this.getXRot()));
        _this.setYRot(GunProjectile.lerpRotation(_this.yRotO, _this.getYRot()));
        // 位置更新
        double nextPosX = _this.getX() + x;
        double nextPosY = _this.getY() + y;
        double nextPosZ = _this.getZ() + z;
        _this.setPos(nextPosX, nextPosY, nextPosZ);
        // 基础环境阻力与重力
        float friction = iGunProjectile.getFriction(_this);
        float gravity = iGunProjectile.getGravity(_this);
        // 入水后的调整
        if (_this.isInWater()) {
            var _this_level = _this.level();
            for (int i = 0; i < 4; i++) {
                _this_level.addParticle(ParticleTypes.BUBBLE, nextPosX - x * 0.25F, nextPosY - y * 0.25F, nextPosZ - z * 0.25F, x, y, z);
            }
            // 在水中的阻力与重力更新
            friction = 0.4F;
            gravity *= 0.6F;
        }
        // 重力与阻力更新速度状态
        _this.setDeltaMovement(thisTickMovement
                .scale(1.0F - friction)
                .add(0.0D, -gravity, 0.0D));
        // ----照搬原版结束----

        tickContext.group.projectileEffectManager().moveEffect(tickContext, iGunProjectile, gunProjectile);
    }

    // --------IProjectilePhysicsExtension--------

    @Override
    public void shootFromRotation(Entity livingShooter, @NotNull Projectile projectile, float xRot, float yRot, float yOffset, float pow, Vec2 spreadOffset) {
        // ----暂时照搬原版----

        // 根据散布和射击角度计算子弹方向
        Vec3 projectileDirection = new Vec3(spreadOffset.x, spreadOffset.y, SPREAD_FORWARD_DISTANCE)
                .xRot(xRot * Mth.DEG_TO_RAD)
                .yRot(yRot * Mth.DEG_TO_RAD);
        // 将方向向量转换为指定速度的子弹速度
        Vec3 projectileVelocity = projectileDirection.normalize().scale(pow);

        // 根据速度方向同步子弹实体朝向
        double horizontalDistance = projectileVelocity.horizontalDistance();
        projectile.setYRot((float) (Mth.atan2(projectileVelocity.x, projectileVelocity.z) * (double) (180f / (float) Math.PI)));
        projectile.setXRot((float) (Mth.atan2(projectileVelocity.y, horizontalDistance) * (double) (180f / (float) Math.PI)));

        // 初始化上一帧旋转，避免首帧插值异常
        projectile.yRotO = projectile.getYRot();
        projectile.xRotO = projectile.getXRot();

        // 叠加射击者自身速度，继承移动惯性
        Vec3 shooterVelocity = livingShooter.getDeltaMovement();
        projectile.setDeltaMovement(
                projectileVelocity.add(
                        shooterVelocity.x,
                        livingShooter.onGround() ? 0 : shooterVelocity.y,
                        shooterVelocity.z
                )
        );
    }

    // --------便利方法--------
    // 如果要引入其他filter/checker，直接调用自己写的就行

    /**
     * 包含自主合法性过滤的实例化工厂方法
     * @param targetEntity   射线碰撞到的目标实体
     * @param hitPos         精确碰撞坐标点
     * @param headshot       是否判定为爆头
     * @param contextEntity  引发当前检测的射线基准实体（用于判定所有者与骑乘状态）
     * @return 如果目标实体合法则返回实例，若属于自身/载具/乘客则返回 null
     */
    public @Nullable EntityHitResult createWithFilter(
            Entity targetEntity,
            Vec3 hitPos,
            boolean headshot,
            Entity contextEntity
    ) {
        Entity owner = null;
        if (contextEntity instanceof Projectile projectile) {
            owner = projectile.getOwner();
        }

        // 禁止对自己造成伤害
        if (targetEntity.equals(owner)) return null;

        // 射击无视自己的载具和该载具上的其他乘客
        if (owner != null
                && (targetEntity.isPassengerOfSameVehicle(owner) || targetEntity.equals(owner.getVehicle()))
        ) return null;

        return new EntityHitResult(targetEntity, hitPos, headshot);
    }

    /**
     * 缓存字段，避免每次都重新拿
     * 其他模组不应该在模组主类初始化时调用
     */
    public static final IMcRegistry mcRegistry = CustomGun.getMcRegistry();

    /**
     * 判定目标实体在指定碰撞点下是否达成爆头
     * @param targetEntity 目标实体
     * @param hitPos 碰撞点绝对坐标
     * @return 是否爆头
     */
    public boolean checkHeadshot(Entity targetEntity, Vec3 hitPos) {
        Vec3 hitBoxPos = hitPos.subtract(targetEntity.position());
        var entityRl = mcRegistry.getEntityTypeRl(targetEntity.getType());

        if (entityRl != null) {
            AABB aabb = HeadAABBData.getHeadAABB(entityRl);
            if (aabb != null) {
                return aabb.contains(hitBoxPos);
            }
        }

        // 没有配置的默认给一个眼部高度范围判定
        float eyeHeight = targetEntity.getEyeHeight();
        return (eyeHeight - 0.25) < hitBoxPos.y && hitBoxPos.y < (eyeHeight + 0.25);
    }

    /**
     * 对检测出的命中实体按照离射击起点的距离进行升序排序
     */
    public void sortHitResults(List<EntityHitResult> hitResults, Vec3 startPos) {
        if (hitResults.isEmpty()) return;

        hitResults.sort(Comparator.comparingDouble(result -> result.hitPos().distanceToSqr(startPos)));
    }
}
