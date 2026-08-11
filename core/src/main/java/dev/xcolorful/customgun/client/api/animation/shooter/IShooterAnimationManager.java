/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.animation.shooter;

import dev.xcolorful.customgun.client.api.item.gun.IShooterAnimationCategory;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface IShooterAnimationManager {

    boolean registerAnimator(IShooterAnimator animator);

    void setRotationAnglesHead(LivingEntity entityIn,
                               ModelPart head, ModelPart body, ModelPart leftArm, ModelPart rightArm,
                               float limbSwingAmount);

    @Nullable IShooterAnimator getAnimator(IShooterAnimationCategory animationCategory);
}
