/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.projectile.physics;

import net.minecraft.world.entity.Entity;
import org.joml.Vector2d;

public interface IProjectilePhysicsExtension {

    void shootFromRotation(Entity source,
                           float xRot, float yRot, float yOffset, float pow,
                           Vector2d spreadOffset);
}
