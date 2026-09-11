/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.projectile.impact;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.IBulletVictimEntity;
import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.entity.victim.IBulletVictimEntityGetter;
import dev.xcolorful.customgun.core.api.event.projectile.ProjectileHitEntityEvent;
import dev.xcolorful.customgun.core.api.event.projectile.ProjectileHitEntityFinishEvent;
import dev.xcolorful.customgun.core.api.event.projectile.ProjectileKillEntityEvent;
import dev.xcolorful.customgun.core.api.minecraft.damage.CustomDamageType;
import dev.xcolorful.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;
import dev.xcolorful.customgun.core.init.registry.ModDamageTypes;
import dev.xcolorful.customgun.core.network.message.projectile.S2CMessageProjectileHit;
import dev.xcolorful.customgun.core.network.message.projectile.S2CMessageProjectileKill;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;
import dev.xcolorful.customgun.core.util.SendUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class _ProjectileHit {

    /**
     * 该功能的位置可以当作原模组{@code EntityKineticBullet#onHitEntity}，但整个体系流程都完全变了
     */
    protected static boolean onProjectileHitEntity(IProjectilePhysicsRuntime.EntityHitResult entityHitResult,
                                                   IGunProjectile iGunProjectile, Entity gunProjectile) {
        Entity victimEntity;
        Entity directEntity = gunProjectile; // 出伤工具 (子弹)
        @Nullable Entity causingEntity; // 使用工具的实体 (枪手)

        // --------预处理--------

        // 构造事件
        ProjectileHitEntityEvent hitEvent; {
            victimEntity = entityHitResult.entity();
            @Nullable Projectile projectile = gunProjectile instanceof Projectile _projectile ? _projectile : null;
            causingEntity = projectile != null ? projectile.getOwner() : null;
            if (causingEntity == null) causingEntity = gunProjectile;

            ProjectileHitEntityEvent.Context context = new ProjectileHitEntityEvent.Context(); {
                context.setVictimEntity(victimEntity);
                context.setCausingEntity(causingEntity);
                context.setGunLocation(iGunProjectile.getGunLocation(gunProjectile));
                context.setBaseDamage(_calculateDamage(entityHitResult.hitPos(), iGunProjectile, gunProjectile));
                context_setDamageSource(context, victimEntity, directEntity, causingEntity);
                context.setHeadshot(entityHitResult.headshot());
                context.setHeadshotMultiplier(iGunProjectile.getHeadshotMultiplier(gunProjectile));
            }
            hitEvent = new ProjectileHitEntityEvent(McLogicalSide.SERVER, context,
                    iGunProjectile, gunProjectile,
                    entityHitResult,
                    IBulletVictimEntityGetter.fromEntity(victimEntity));
        }
        if (CustomGun.getEventPoster().postCustomEvent(hitEvent)) {
            // 事件取消，忽略
            return false;
        }

        // --------执行--------

        return _executeProjectileHitEntity(entityHitResult, iGunProjectile, gunProjectile, hitEvent);
    }
    private static boolean _executeProjectileHitEntity(IProjectilePhysicsRuntime.EntityHitResult entityHitResult,
                                                       IGunProjectile iGunProjectile, Entity gunProjectile,
                                                       ProjectileHitEntityEvent hitEvent) {
        Entity victimEntity = hitEvent.context.getVictimEntity();
        @Nullable LivingEntity victimLivingEntity = victimEntity instanceof LivingEntity _livingEntity ? _livingEntity : null;
        Entity directEntity = gunProjectile; // 出伤工具 (子弹)
        @Nullable Entity causingEntity = hitEvent.context.getCausingEntity();
        if (victimEntity == null || victimEntity.isRemoved()) {
            // 目标在事件里被清掉了?
            return true;
        }

        { // 击退效果
            @Nullable IBulletVictimEntity iBulletVictimEntity = IBulletVictimEntityGetter.fromEntity(victimEntity);
            if (iBulletVictimEntity != null) {
                iBulletVictimEntity.cgc$setKnockbackStrength(iGunProjectile.getKnockbackStrength(gunProjectile));
            }
        }

        // 伤害计算
        float baseDamage = hitEvent.context.getBaseDamage();
        float damage; {
            if (hitEvent.context.isHeadshot()) {
                damage = baseDamage * hitEvent.context.getHeadshotMultiplier();
            } else {
                damage = baseDamage;
            }
        }

        boolean isDeadBefore = victimLivingEntity != null && victimLivingEntity.isDeadOrDying();
        { // 出伤
            assert victimEntity != null;

            @Nullable DamageSource bulletDamage;
            @Nullable DamageSource pierceDamage;
            float armorIgnorePercent = Mth.clamp(iGunProjectile.getArmorIgnorePercent(gunProjectile), 0.0f, 1.0f);

            if ( // 普通伤害
                    // 没被移除
                    !victimEntity.isRemoved()
                    // 没死亡 (生物)
                    && (victimLivingEntity != null && !victimLivingEntity.isDeadOrDying())
                    // 有普通伤害
                    && (bulletDamage = hitEvent.context.getBulletDamage()) != null && armorIgnorePercent < 1
            ) {
                victimEntity.invulnerableTime = 0; // 取消无敌时间
                victimEntity.hurt(bulletDamage, damage * (1 - armorIgnorePercent));
            }

            if ( // 穿甲伤害
                    // 没被移除
                    !victimEntity.isRemoved()
                    // 没死亡 (生物)
                    && (victimLivingEntity != null && !victimLivingEntity.isDeadOrDying())
                    // 有穿甲伤害
                    && (pierceDamage = hitEvent.context.getPiercerDamage()) != null && armorIgnorePercent > 0
            ) {
                victimEntity.invulnerableTime = 0; // 取消无敌时间
                victimEntity.hurt(pierceDamage, damage * armorIgnorePercent);
            }
        }

        if (!isDeadBefore // 只有本次击杀才算在内
                && victimLivingEntity != null && victimLivingEntity.isDeadOrDying()) {
            // 枪射物击杀事件
            CustomGun.getEventPoster().postCustomEvent(new ProjectileKillEntityEvent(McLogicalSide.SERVER,
                    hitEvent.context,
                    iGunProjectile, gunProjectile,
                    entityHitResult, hitEvent.getIBulletVictimEntity()));
            if (causingEntity != null) {
                // 仅在有伤害源实体才发包
                S2CMessageProjectileKill message; {
                    message = new S2CMessageProjectileKill(gunProjectile.getId(), victimEntity.getId(), causingEntity.getId(),
                            hitEvent.context.getGunLocation(), iGunProjectile.getGunDisplayLocation(gunProjectile),
                            baseDamage, hitEvent.context.isHeadshot(), hitEvent.context.getHeadshotMultiplier());
                }
                SendUtils.sendMessageToAllPlayers(message);
            }
        }

        { // 枪射物命中事件
            CustomGun.getEventPoster().postCustomEvent(new ProjectileHitEntityFinishEvent(McLogicalSide.SERVER,
                    hitEvent.context,
                    iGunProjectile, gunProjectile,
                    entityHitResult, hitEvent.getIBulletVictimEntity()));
            if (causingEntity != null) {
                // 仅在有伤害源实体才发包
                S2CMessageProjectileHit message; {
                    message = new S2CMessageProjectileHit(gunProjectile.getId(), victimEntity.getId(), causingEntity.getId(),
                            hitEvent.context.getGunLocation(), iGunProjectile.getGunDisplayLocation(gunProjectile),
                            baseDamage, hitEvent.context.isHeadshot(), hitEvent.context.getHeadshotMultiplier());
                }
                SendUtils.sendMessageToAllPlayers(message);
            }
        }

        return true;
    }

    private static float _calculateDamage(@NotNull Vec3 hitPos,
                                         IGunProjectile iGunProjectile, Entity gunProjectile) {
        @Nullable List<_DistanceDamageData> damageCalculation = iGunProjectile.getDamageCalculation(gunProjectile);
        if (damageCalculation == null) return 0;

        @Nullable Vec3 shootPos = iGunProjectile.getShootPos(gunProjectile);
        double shootDistance = shootPos != null ? hitPos.distanceTo(shootPos) : 0;
        float damage = 0;
        for (int i = 0; i < damageCalculation.size(); i++) {
            _DistanceDamageData damageData = damageCalculation.get(i);
            damage = damageData.getDamage();

            // 取第一个>=shootDistance时伤害值
            if (shootDistance < damageData.getDistance()) {
                break;
            }
        }

        return damage;
    }
    private static void context_setDamageSource(ProjectileHitEntityEvent.Context context,
                                                Entity victimEntity,
                                                Entity directEntity,
                                                @Nullable Entity causingEntity) {
        EntityType<?> victimEntityType = victimEntity.getType();

        DamageSource bulletDamage;
        DamageSource pierceDamage; {
            RegistryAccess registryAccess = directEntity.level().registryAccess();
            bulletDamage = ModDamageTypes.createDamage(registryAccess, CustomDamageType.BULLET, directEntity, causingEntity);
            pierceDamage = ModDamageTypes.createDamage(registryAccess, CustomDamageType.PIERCER, directEntity, causingEntity);
        }
        context.setBulletDamage(bulletDamage);
        context.setPiercerDamage(pierceDamage);
    }
}
