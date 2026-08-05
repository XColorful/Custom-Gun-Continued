/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.network.message.event;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.util.ClientWorldUtils;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.IBulletVictimEntity;
import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.entity.projectile.IGunProjectileGetter;
import dev.xcolorful.customgun.core.api.entity.victim.IBulletVictimEntityGetter;
import dev.xcolorful.customgun.core.api.event.projectile.ProjectileHitEntityEvent;
import dev.xcolorful.customgun.core.api.event.projectile.ProjectileKillEntityEvent;
import dev.xcolorful.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;
import dev.xcolorful.customgun.core.network.message.event.ServerMessageGunHurt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class _ServerMessageGunHurt {

    public static void onHurt(ServerMessageGunHurt message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        @Nullable Entity gunProjectile = ClientWorldUtils.getEntityById(level, message.bulletId());
        @Nullable Entity victimEntity = ClientWorldUtils.getEntityById(level, message.victimEntityId());
        @Nullable LivingEntity livingShooter = ClientWorldUtils.getLivingEntityById(level, message.shooterId());
        ProjectileHitEntityEvent.Context context = getContext(message, victimEntity, livingShooter);
        @Nullable IGunProjectile iGunProjectile = IGunProjectileGetter.fromEntity(gunProjectile);
        IProjectilePhysicsRuntime.EntityHitResult entityHitResult = new IProjectilePhysicsRuntime.EntityHitResult(victimEntity, null, message.isHeadShot());
        IBulletVictimEntity iBulletVictimEntity = IBulletVictimEntityGetter.fromEntity(victimEntity);

        ProjectileHitEntityEvent event = new ProjectileKillEntityEvent(McLogicalSide.CLIENT, context,
                iGunProjectile, gunProjectile,
                entityHitResult, iBulletVictimEntity);
        CustomGun.getEventPoster().postCustomEvent(event);
    }
    private static @NotNull ProjectileHitEntityEvent.Context getContext(ServerMessageGunHurt message, @Nullable Entity victimEntity, @Nullable LivingEntity livingShooter) {
        var gunLocation = message.gunLocation();
        var gunDisplayLocation = message.gunDisplayLocation();
        float damage = message.damage();
        boolean isHeadshot = message.isHeadShot();
        float headshotMultiplier = message.headshotMultiplier();

        ProjectileHitEntityEvent.Context context = new ProjectileHitEntityEvent.Context(); {
            context.setVictimEntity(victimEntity);
            context.setCausingEntity(livingShooter);
            context.setGunLocation(gunLocation);
            context.setBaseDamage(damage);
            context.setBulletDamage(null); // 客户端不使用
            context.setPiercerDamage(null);
            context.setHeadshot(isHeadshot);
            context.setHeadshotMultiplier(headshotMultiplier);
        }
        return context;
    }
}
