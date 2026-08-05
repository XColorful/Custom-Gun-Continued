/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.util;

import dev.xcolorful.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;
import dev.xcolorful.customgun.core.projectile.physics.ProjectilePhysicsManager;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * Go to {@link RayTraceUtils}
 */
@Deprecated(forRemoval = true)
public class EntityUtils {

    @Deprecated
    public static @Nullable IProjectilePhysicsRuntime.EntityHitResult findEntityOnPath(Projectile bulletEntity, Vec3 startVec, Vec3 endVec) {
        List<IProjectilePhysicsRuntime.EntityHitResult> results = findEntitiesOnPath(bulletEntity, startVec, endVec);
        if (results.isEmpty()) {
            return null;
        }
        results.sort(Comparator.comparingDouble(result -> startVec.distanceToSqr(result.hitPos())));
        return results.get(0);
    }

    @Deprecated
    public static @NotNull List<IProjectilePhysicsRuntime.EntityHitResult> findEntitiesOnPath(Projectile bulletEntity, Vec3 startVec, Vec3 endVec) {
        return RayTraceUtils.EntityTrace.rayTraceEntities(
                bulletEntity,
                bulletEntity.getOwner(),
                startVec,
                endVec,
                1.0,
                ProjectilePhysicsManager.PROJECTILE_TARGETS,
                (entity, hitPos) -> {
                    var factory = ProjectilePhysicsManager.INSTANCE.createWithFilter(entity, hitPos, false, bulletEntity);
                    if (factory == null) {
                        return null;
                    }
                    boolean headshot = ProjectilePhysicsManager.INSTANCE.checkHeadshot(entity, hitPos);
                    return new IProjectilePhysicsRuntime.EntityHitResult(entity, hitPos, headshot);
                }
        );
    }
}
