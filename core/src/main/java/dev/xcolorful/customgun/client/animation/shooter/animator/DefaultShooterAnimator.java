/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.shooter.animator;

import dev.xcolorful.customgun.client.api.animation.shooter.IShooterAnimator;
import dev.xcolorful.customgun.client.api.item.gun.IShooterAnimationCategory;
import dev.xcolorful.customgun.client.api.item.gun.ShooterAnimationCategory;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class DefaultShooterAnimator implements IShooterAnimator {
    public static final DefaultShooterAnimator INSTANCE = new DefaultShooterAnimator();

    protected DefaultShooterAnimator() {
    }

    @Override
    public String getAnimatorName() {
        return "DefaultShooterAnimator";
    }

    @Override
    public IShooterAnimationCategory getAnimationCategory() {
        return ShooterAnimationCategory.DEFAULT;
    }

    @Override
    public void animateShooter(ModelPart head, ModelPart body, ModelPart leftArm, ModelPart rightArm,
                               ILivingShooter iLivingShooter, LivingEntity livingShooter,
                               GunDisplayInstance gunDisplayInstance) {
        // mixin注入点
    }

    @Override
    public void animateShooterAiming(ModelPart head, ModelPart body, ModelPart leftArm, ModelPart rightArm,
                                     ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                     GunDisplayInstance gunDisplayInstance) {
        // mixin注入点
    }
}
