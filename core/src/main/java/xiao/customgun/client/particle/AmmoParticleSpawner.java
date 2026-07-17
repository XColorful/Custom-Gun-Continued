/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.resource.assets.display.ammo._AmmoParticle;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.resource.instance.data.ClientAmmoIndexInstance;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.developer.PlannedRefactor;

public class AmmoParticleSpawner {
    public static int PARTICLE_START_DISTANCE_SQR = 3 * 3;

    public static void addParticle(IGunProjectile iGunProjectile, Projectile gunProjectile) {
        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(iGunProjectile.getGunDisplayLocation(gunProjectile));
        if (gunDisplayInstance == null) return;

        _AmmoParticle gunParticle = gunDisplayInstance.getAmmoParticle();
        if (gunParticle != null) {
            // 优先调用枪械的粒子效果
            spawnParticle(gunProjectile, gunParticle, gunDisplayInstance.getParticleOptions());
        } else {
            // 其次调用子弹的
            var ammoLocation = iGunProjectile.getAmmoLocation(gunProjectile);
            @Nullable ClientAmmoIndexInstance ammoIndexInstance = ClientResourceApi.getClientAmmoIndexInstance(ammoLocation);
            if (ammoIndexInstance == null) return;

            _AmmoParticle ammoParticle = ammoIndexInstance.getAmmoDisplay().getAmmoParticle();
            if (ammoParticle != null) {
                spawnParticle(gunProjectile, ammoParticle, ammoIndexInstance.getParticleOptions());
            }
        }
    }

    private static final RandomSource random = RandomSource.create();
    private static void spawnParticle(Projectile gunProjectile, _AmmoParticle ammoParticle,
                                      @Nullable ParticleOptions particleOptions) {
        if (particleOptions == null) return;

        if (PlannedRefactor.ON_SPAWN_BULLET_PARTICLE) return;

        int count = ammoParticle.getCount();
        float[] delta = ammoParticle.getDelta();
        if (delta.length < 3) delta = new float[]{0, 0, 0};
        float speed = ammoParticle.getSpeed();
        ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;

        if (count == 0) {
            // 单个粒子
            double xSpeed = speed * delta[0];
            double ySpeed = speed * delta[1];
            double zSpeed = speed * delta[2];
            Particle result = particleEngine.createParticle(particleOptions,
                    gunProjectile.getX(), gunProjectile.getY(), gunProjectile.getZ(),
                    xSpeed, ySpeed, zSpeed);
            if (result != null) {
                result.setLifetime(ammoParticle.getLifetimeTicks());
            }
        } else {
            // 多个粒子
            Entity owner = gunProjectile.getOwner();
            for (int i = 0; i < count; i++) {
                Vec3 deltaMovement = gunProjectile.getDeltaMovement();
                double deltaMovementRandom = random.nextDouble();
                double offsetX = random.nextGaussian() * delta[0] + deltaMovementRandom * deltaMovement.x;
                double offsetY = random.nextGaussian() * delta[1] + deltaMovementRandom * deltaMovement.y;
                double offsetZ = random.nextGaussian() * delta[2] + deltaMovementRandom * deltaMovement.z;
                double xSpeed = random.nextGaussian() * speed;
                double ySpeed = random.nextGaussian() * speed;
                double zSpeed = random.nextGaussian() * speed;

                double posX = gunProjectile.getX() + offsetX;
                double posY = gunProjectile.getY() + offsetY;
                double posZ = gunProjectile.getZ() + offsetZ;

                // 如果太贴近发射者，不进行粒子生成
                if (owner == null || owner.distanceToSqr(posX, posY, posZ) > PARTICLE_START_DISTANCE_SQR) {
                    Particle result = particleEngine.createParticle(particleOptions, posX, posY, posZ, xSpeed, ySpeed, zSpeed);
                    if (result != null) {
                        result.setLifetime(ammoParticle.getLifetimeTicks());
                    }
                }
            }
        }
    }
}
