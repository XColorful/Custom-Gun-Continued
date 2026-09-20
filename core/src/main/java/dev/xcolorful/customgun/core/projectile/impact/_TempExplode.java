/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.projectile.impact;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.projectile.process.IProjectileProcessRuntime;
import dev.xcolorful.customgun.core.config.AmmoConfig;
import dev.xcolorful.customgun.core.config.SyncConfig;
import dev.xcolorful.customgun.core.init.registry.ModBlocks;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet._ExplosionData;
import dev.xcolorful.customgun.core.util.EntityUtils;
import dev.xcolorful.customgun.core.util.RayTraceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 目前考虑的设计是把子弹爆炸效果移到AmmoData里，但是优先取在GunData里定义的
 * 这个类临时复刻原爆炸效果
 * <p>
 * 对应原模组 {@code EntityKineticBullet} 的爆炸部分，以及 {@code ExplodeUtil} 与 {@code ProjectileExplosion}。
 * 原模组里爆炸相关的 Forge 事件（onExplosionStart / onExplosionDetonate）本类一律不发布
 */
@Deprecated
public class _TempExplode {

    private _TempExplode() {
    }

    /**
     * 原版 {@code Level.DEFAULT_EXPLOSION_BLOCK_PARTICLES} 是 private，这里照抄一份
     * （1.21.10 起随发包下发给客户端做方块破坏粒子）
     */
    private static final WeightedList<ExplosionParticleInfo> BLOCK_PARTICLES = WeightedList.<ExplosionParticleInfo>builder()
            .add(new ExplosionParticleInfo(ParticleTypes.POOF, 0.5F, 1.0F))
            .add(new ExplosionParticleInfo(ParticleTypes.SMOKE, 1.0F, 1.0F))
            .build();

    /**
     * 该枪射物是否带爆炸效果，等价于原模组 {@code EntityKineticBullet#explosion} 字段
     */
    public static boolean isExplode(IGunProjectile iGunProjectile, Entity gunProjectile) {
        return getExplosionData(iGunProjectile, gunProjectile) != null;
    }

    /**
     * 引信爆炸判定，对应原模组 {@code EntityKineticBullet#onBulletTick} 开头的延迟爆炸判定
     * <p>
     * 原模组用逐 tick 递减的计数器，这里用枪射物的存活 tick 等价替代（两者都每 tick 变动 1，
     * 且枪射物不存档，不存在跨存档续算的问题）
     */
    public static void preExplode(IProjectileProcessRuntime.TickContext tickContext,
                                  IGunProjectile iGunProjectile, Entity gunProjectile) {
        @Nullable _ExplosionData explosionData = getExplosionData(iGunProjectile, gunProjectile);
        if (explosionData == null) return;

        int delayTickCount = (int) (explosionData.getMaxDelaySeconds() * 20);
        // 防止越界，提前判定
        if (delayTickCount < 0) {
            delayTickCount = Integer.MAX_VALUE;
        }
        // 原模组计数器初值为 max(delayTickCount, 1)，归零后的下一个 tick 爆炸
        if (gunProjectile.tickCount <= Math.max(delayTickCount, 1)) return;

        explode(gunProjectile.position(), iGunProjectile, gunProjectile);
    }

    /**
     * 执行爆炸
     *
     * @return 是否爆炸（是否消耗pierce），爆炸时枪射物直接结束飞行
     */
    public static boolean explode(Vec3 hitPos, IGunProjectile iGunProjectile, Entity gunProjectile) {
        @Nullable _ExplosionData explosionData = getExplosionData(iGunProjectile, gunProjectile);
        if (explosionData == null) return false;

        if (!createExplosion(gunProjectile, hitPos, explosionData)) return false;

        // 爆炸直接结束不留弹孔，不处理之后的逻辑
        gunProjectile.discard();
        return true;
    }

    /**
     * @return 启用了爆炸的爆炸数据，未启用则返回 null
     */
    private static @Nullable _ExplosionData getExplosionData(IGunProjectile iGunProjectile, Entity gunProjectile) {
        @Nullable _ExplosionData explosionData = iGunProjectile.getExplosionData(gunProjectile);
        return explosionData != null && explosionData.getEnableExplode() ? explosionData : null;
    }

    /**
     * @return 是否真的执行了爆炸（客户端不执行）
     */
    private static boolean createExplosion(Entity gunProjectile, Vec3 hitPos, _ExplosionData explosionData) {
        Level level = gunProjectile.level();
        // 客户端不执行
        if (!(level instanceof ServerLevel serverLevel)) return false;

        // 依据配置文件决定方块破坏方式
        Explosion.BlockInteraction mode = AmmoConfig.EXPLOSIVE_AMMO_DESTROYS_BLOCK.get() && explosionData.getEnableWorldDestruction()
                ? Explosion.BlockInteraction.DESTROY
                : Explosion.BlockInteraction.KEEP;
        float explosionDamage = (float) Math.max(explosionData.getExplodeDamage() * SyncConfig.DAMAGE_BASE_MULTIPLIER.get(), 0);
        float explosionRadius = Math.max(explosionData.getExplodeScale(), 0);
        @Nullable Entity owner = gunProjectile instanceof Projectile projectile
                ? projectile.getOwner()
                : null;

        ProjectileExplosion explosion = new ProjectileExplosion(serverLevel, owner, gunProjectile, hitPos,
                explosionDamage, explosionRadius, explosionData.getEnableKnockback(), mode);
        // 1.21.10 起 explode() 返回被炸方块数，随发包下发给客户端
        int blockCount = explosion.explode();

        // 客户端发包，发送爆炸相关信息
        int visibleDistance = AmmoConfig.EXPLOSIVE_AMMO_VISIBLE_DISTANCE.get();
        // 1.21.2 起粒子由服务端选定一个后下发（原版 ServerLevel 同款判定：小爆炸用小粒子）
        ParticleOptions explosionParticle = explosion.isSmall() ? ParticleTypes.EXPLOSION : ParticleTypes.EXPLOSION_EMITTER;
        for (ServerPlayer player : serverLevel.players()) {
            if (Mth.sqrt((float) player.distanceToSqr(hitPos)) < visibleDistance) {
                player.connection.send(new ClientboundExplodePacket(hitPos, explosionRadius, blockCount,
                        Optional.ofNullable(explosion.getHitPlayers().get(player)),
                        explosionParticle, SoundEvents.GENERIC_EXPLODE, BLOCK_PARTICLES));
            }
        }
        return true;
    }

    /**
     * 复刻原模组 {@code com.tacz.guns.util.block.ProjectileExplosion}
     * <p>
     * 与原版爆炸的区别：伤害与半径分开（原版用同一个 radius 派生伤害），
     * 且对生物使用延迟补偿后的碰撞箱计算视线，而不是原版的取样点法
     * <p>
     * 1.21.2 起 {@code Explosion} 变成接口，实体逻辑只有 {@code ServerExplosion} 一份且内部方法都是
     * private，无法再像 1.20.x 那样只覆写 {@code explode()} 复用方块流程。因此这里改为继承
     * {@code ServerExplosion}（构造、{@code getHitPlayers}、{@code isSmall} 等接口实现在父类已有），
     * 并整体覆写 {@code explode()}，方块破坏/掉落/火焰仍按原版算法
     */
    private static class ProjectileExplosion extends ServerExplosion {
        private static final ExplosionDamageCalculator DEFAULT_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();

        private final ServerLevel level;
        private final double x;
        private final double y;
        private final double z;
        private final float explosionDamage;
        private final float radius;
        private final boolean knockback;
        private final @Nullable Entity owner;
        private final Entity exploder;

        ProjectileExplosion(ServerLevel level, @Nullable Entity owner, Entity exploder, Vec3 hitPos,
                            float explosionDamage, float radius, boolean knockback, Explosion.BlockInteraction mode) {
            // 爆炸的视觉大小取 radius，伤害由 explosionDamage 决定
            super(level, exploder, null, DEFAULT_DAMAGE_CALCULATOR, hitPos, radius,
                    AmmoConfig.EXPLOSIVE_AMMO_FIRE.get(), mode);
            this.level = level;
            this.x = hitPos.x();
            this.y = hitPos.y();
            this.z = hitPos.z();
            this.explosionDamage = explosionDamage;
            this.radius = radius;
            this.knockback = knockback;
            this.owner = owner;
            this.exploder = exploder;
        }

        @Override
        public int explode() {
            this.level.gameEvent(this.exploder, GameEvent.EXPLODE, BlockPos.containing(this.x, this.y, this.z));
            Set<BlockPos> set = new HashSet<>();
            int i = 16;

            for (int dx = 0; dx < i; ++dx) {
                for (int dy = 0; dy < i; ++dy) {
                    for (int dz = 0; dz < i; ++dz) {
                        if (dx == 0 || dx == i - 1 || dy == 0 || dy == i - 1 || dz == 0 || dz == i - 1) {
                            double d0 = (float) dx / (i - 1) * 2.0F - 1.0F;
                            double d1 = (float) dy / (i - 1) * 2.0F - 1.0F;
                            double d2 = (float) dz / (i - 1) * 2.0F - 1.0F;
                            double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                            d0 /= d3;
                            d1 /= d3;
                            d2 /= d3;
                            float f = this.radius * (0.7F + this.level.getRandom().nextFloat() * 0.6F);
                            double blockX = this.x;
                            double blockY = this.y;
                            double blockZ = this.z;

                            for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                                BlockPos pos = BlockPos.containing(blockX, blockY, blockZ);
                                BlockState blockState = this.level.getBlockState(pos);
                                FluidState fluidState = this.level.getFluidState(pos);
                                if (!this.level.isInWorldBounds(pos)) {
                                    break;
                                }

                                Optional<Float> optional = DEFAULT_DAMAGE_CALCULATOR.getBlockExplosionResistance(this, this.level, pos, blockState, fluidState);
                                if (optional.isPresent()) {
                                    f -= (optional.get() + f1) * f1;
                                }

                                if (f > 0.0F && DEFAULT_DAMAGE_CALCULATOR.shouldBlockExplode(this, this.level, pos, blockState, f)) {
                                    set.add(pos);
                                }

                                blockX += d0 * (double) f1;
                                blockY += d1 * (double) f1;
                                blockZ += d2 * (double) f1;
                            }
                        }
                    }
                }
            }

            List<BlockPos> toBlow = new ArrayList<>(set);
            float radius = this.radius;
            int minX = Mth.floor(this.x - (double) radius - 1.0D);
            int maxX = Mth.floor(this.x + (double) radius + 1.0D);
            int minY = Mth.floor(this.y - (double) radius - 1.0D);
            int maxY = Mth.floor(this.y + (double) radius + 1.0D);
            int minZ = Mth.floor(this.z - (double) radius - 1.0D);
            int maxZ = Mth.floor(this.z + (double) radius + 1.0D);
            radius *= 2;
            // 1.21.11 起原版对半径趋近 0 的爆炸跳过实体伤害与击退（原版与本移植都以 radius 作除数）
            List<Entity> entities = this.radius < 1.0E-5F
                    ? List.of()
                    : this.level.getEntities(this.exploder, new AABB(minX, minY, minZ, maxX, maxY, maxZ));
            Vec3 explosionPos = new Vec3(this.x, this.y, this.z);

            for (Entity entity : entities) {
                if (entity.ignoreExplosion(this)) {
                    continue;
                }

                AABB boundingBox = EntityUtils.Hitbox.getTracedHitbox(entity, this.owner);
                double strength;
                double deltaX;
                double deltaY;
                double deltaZ;
                double minDistance = radius;
                Vec3[] d = new Vec3[15];

                if (!(entity instanceof LivingEntity)) {
                    strength = Math.sqrt(entity.distanceToSqr(explosionPos)) * 2 / radius;
                    deltaX = entity.getX() - this.x;
                    deltaY = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
                    deltaZ = entity.getZ() - this.z;
                } else {
                    deltaX = (boundingBox.maxX + boundingBox.minX) / 2;
                    deltaY = (boundingBox.maxY + boundingBox.minY) / 2;
                    deltaZ = (boundingBox.maxZ + boundingBox.minZ) / 2;
                    d[0] = new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
                    d[1] = new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
                    d[2] = new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
                    d[3] = new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
                    d[4] = new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
                    d[5] = new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
                    d[6] = new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
                    d[7] = new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                    d[8] = new Vec3(boundingBox.minX, deltaY, deltaZ);
                    d[9] = new Vec3(boundingBox.maxX, deltaY, deltaZ);
                    d[10] = new Vec3(deltaX, boundingBox.minY, deltaZ);
                    d[11] = new Vec3(deltaX, boundingBox.maxY, deltaZ);
                    d[12] = new Vec3(deltaX, deltaY, boundingBox.minZ);
                    d[13] = new Vec3(deltaX, deltaY, boundingBox.maxZ);
                    d[14] = new Vec3(deltaX, deltaY, deltaZ);
                    // 视线被方块挡住的距离越远，爆炸伤害越低
                    for (int s = 0; s < 15; s++) {
                        BlockHitResult result = rayTraceBlocks(this.level, explosionPos, d[s]);
                        minDistance = (result.getType() != HitResult.Type.BLOCK)
                                ? Math.min(minDistance, explosionPos.distanceTo(d[s]))
                                : minDistance;
                    }
                    strength = minDistance * 2 / radius;
                    deltaX -= this.x;
                    deltaY -= this.y;
                    deltaZ -= this.z;
                }

                if (strength > 1.0D) {
                    continue;
                }

                double distanceToExplosion = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                if (distanceToExplosion != 0.0D) {
                    deltaX /= distanceToExplosion;
                    deltaY /= distanceToExplosion;
                    deltaZ /= distanceToExplosion;
                }

                double damage = 1.0D - strength;
                entity.hurtServer(this.level, this.getDamageSource(), (float) damage * this.explosionDamage);

                if (entity instanceof LivingEntity livingEntity) {
                    // 1.21 起 ProtectionEnchantment 被移除，爆炸击退减伤改由 EXPLOSION_KNOCKBACK_RESISTANCE 属性表达
                    // （其值就是爆炸保护提供的减伤比例，等价于 1.20.x 的 getExplosionKnockbackAfterDampener）
                    damage *= 1.0D - livingEntity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
                }

                float multiplier = this.explosionDamage * radius / 500;
                // 启用击退效果
                if (AmmoConfig.EXPLOSIVE_AMMO_KNOCK_BACK.get() && this.knockback) {
                    Vec3 knockback = new Vec3(deltaX * damage * multiplier, deltaY * damage * multiplier, deltaZ * damage * multiplier);
                    entity.push(knockback);
                    if (entity instanceof Player player) {
                        if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                            this.getHitPlayers().put(player, knockback);
                        }
                    }
                }
                // 原版 1.21 起在爆炸击退后追加此调用（玩家据此把摔落伤害归因到本次冲量）
                // 1.21.10 起原版新增：被爆炸波及的可转向弹射物改认爆炸源为发射者
                if (entity.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && entity instanceof Projectile projectile) {
                    projectile.setOwner(this.getDamageSource().getEntity());
                }
                entity.onExplosionHit(this.exploder);
            }

            // 与原版一致：KEEP 模式不破坏方块，也就不做方块交互（没有掉落物）
            if (this.getBlockInteraction() != Explosion.BlockInteraction.KEEP) {
                this.interactWithBlocks(toBlow);
            }
            if (AmmoConfig.EXPLOSIVE_AMMO_FIRE.get()) {
                this.createFire(toBlow);
            }

            return toBlow.size();
        }

        /**
         * 复刻原版 {@code ServerExplosion#interactWithBlocks}：交给方块自身处理爆炸，并合并掉落物
         */
        private void interactWithBlocks(List<BlockPos> toBlow) {
            List<StackCollector> drops = new ArrayList<>();
            Util.shuffle(toBlow, this.level.getRandom());
            for (BlockPos pos : toBlow) {
                this.level.getBlockState(pos).onExplosionHit(this.level, pos, this,
                        (stack, dropPos) -> addOrAppendStack(drops, stack, dropPos));
            }
            for (StackCollector drop : drops) {
                Block.popResource(this.level, drop.pos, drop.stack);
            }
        }

        private static void addOrAppendStack(List<StackCollector> drops, ItemStack stack, BlockPos pos) {
            for (StackCollector drop : drops) {
                if (drop.tryMerge(stack)) {
                    return;
                }
            }
            drops.add(new StackCollector(pos, stack));
        }

        /**
         * 复刻原版 {@code ServerExplosion#createFire}
         */
        private void createFire(List<BlockPos> toBlow) {
            for (BlockPos pos : toBlow) {
                if (this.level.getRandom().nextInt(3) == 0 && this.level.getBlockState(pos).isAir()
                        && this.level.getBlockState(pos.below()).isSolidRender()) {
                    this.level.setBlockAndUpdate(pos, BaseFireBlock.getState(this.level, pos));
                }
            }
        }

        private static class StackCollector {
            final BlockPos pos;
            ItemStack stack;

            StackCollector(BlockPos pos, ItemStack stack) {
                this.pos = pos;
                this.stack = stack;
            }

            boolean tryMerge(ItemStack other) {
                if (!ItemEntity.areMergable(this.stack, other)) return false;
                this.stack = ItemEntity.merge(this.stack, other, 16);
                return other.isEmpty();
            }
        }
    }

    /**
     * 复刻原模组 {@code BlockRayTrace#rayTraceBlocks} 的爆炸视线检测
     */
    private static BlockHitResult rayTraceBlocks(Level level, Vec3 startPos, Vec3 endPos) {
        ClipContext clipContext = new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
        IMcRegistry mcRegistry = CustomGun.getMcRegistry();
        List<String> passThroughBlocks = AmmoConfig.PASS_THROUGH_BLOCKS.get();
        return RayTraceUtils.BlockTrace.rayTraceBlocksWithFilter(
                level,
                startPos,
                endPos,
                (blockState, blockPos) -> clipContext.getBlockShape(blockState, level, blockPos),
                (fluidState, blockPos) -> clipContext.getFluidShape(fluidState, level, blockPos),
                blockState -> {
                    if (blockState == null) {
                        return false;
                    }
                    // 检查是否属于配置文件中的穿透方块
                    var blockRl = mcRegistry.getBlockRl(blockState.getBlock());
                    if (blockRl != null && passThroughBlocks.contains(blockRl.toString())) {
                        return true;
                    }
                    // 检查是否包含忽略标签
                    return blockState.is(ModBlocks.BULLET_IGNORE_BLOCKS);
                }
        );
    }
}
