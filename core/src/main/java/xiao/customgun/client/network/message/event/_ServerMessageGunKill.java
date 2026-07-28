/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.network.message.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.util.ClientWorldUtils;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.IBulletVictimEntity;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.entity.projectile.IGunProjectileGetter;
import xiao.customgun.core.api.entity.victim.IBulletVictimEntityGetter;
import xiao.customgun.core.api.event.projectile.ProjectileHitEntityEvent;
import xiao.customgun.core.api.event.projectile.ProjectileKillEntityEvent;
import xiao.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;
import xiao.customgun.core.network.message.event.ServerMessageGunKill;

@ApiStatus.Internal
public class _ServerMessageGunKill {

    public static void onKill(ServerMessageGunKill message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        @Nullable Entity gunProjectile = ClientWorldUtils.getEntityById(level, message.bulletId());
        @Nullable LivingEntity victimEntity = ClientWorldUtils.getLivingEntityById(level, message.victimEntityId());
        @Nullable LivingEntity livingShooter = ClientWorldUtils.getLivingEntityById(level, message.shooterId());
        ProjectileHitEntityEvent.Context context = getContext(message, victimEntity, livingShooter);
        @Nullable IGunProjectile iGunProjectile = IGunProjectileGetter.fromEntity(gunProjectile);
        IProjectilePhysicsRuntime.EntityHitResult entityHitResult = new IProjectilePhysicsRuntime.EntityHitResult(victimEntity, null, message.isHeadShot());
        IBulletVictimEntity iBulletVictimEntity = IBulletVictimEntityGetter.fromEntity(victimEntity);

        ProjectileKillEntityEvent event = new ProjectileKillEntityEvent(McLogicalSide.CLIENT, context,
                iGunProjectile, gunProjectile,
                entityHitResult, iBulletVictimEntity);
        CustomGun.getEventPoster().postCustomEvent(event);
    }
    private static @NotNull ProjectileHitEntityEvent.Context getContext(ServerMessageGunKill message, @Nullable Entity victimEntity, @Nullable LivingEntity livingShooter) {
        var gunLocation = message.gunLocation();
        var gunDisplayLocation = message.gunDisplayLocation();
        float baseDamage = message.baseDamage();
        boolean isHeadshot = message.isHeadShot();
        float headshotMultiplier = message.headshotMultiplier();


        ProjectileHitEntityEvent.Context context = new ProjectileHitEntityEvent.Context(); {
            context.setVictimEntity(victimEntity);
            context.setCausingEntity(livingShooter);
            context.setGunLocation(gunLocation);
            context.setBaseDamage(baseDamage);
            context.setBulletDamage(null); // 客户端不使用
            context.setPiercerDamage(null);
            context.setHeadshot(isHeadshot);
            context.setHeadshotMultiplier(headshotMultiplier);
        }
        return context;
    }
}
