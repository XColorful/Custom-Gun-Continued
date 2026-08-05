/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.entity.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.config.SyncConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 枪械移速影响抽到单独的模组
 */
@Deprecated(forRemoval = true)
public final class LivingShooterSpeedModifier extends LivingShooterAspect {

    public static final @NotNull ResourceLocation EXTRA_SPEED_MODIFIER = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:extra_speed_modifier", CustomGun.MOD_ID));
    public static final @NotNull ResourceLocation WEIGHT_SPEED_MODIFIER = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:weight_speed_modifier", CustomGun.MOD_ID));

    public LivingShooterSpeedModifier(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public void updateSpeedModifier() {
        if (!this.livingShooter.isAlive()) return;

        ItemStack stack = this.livingShooter.getMainHandItem();
        var speedModifier = this.livingShooter.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);
        if (speedModifier == null) return;

        IGun iGun = IGunGetter.fromItemStack(stack);
        if (iGun == null) {
            speedModifier.removeModifier(WEIGHT_SPEED_MODIFIER);
            speedModifier.removeModifier(EXTRA_SPEED_MODIFIER);
            return;
        }

        // 处理重量带来的修正
        @Nullable ShooterGunModifierCache shooterGunModifierCache = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter).cgc$getGunModifierCache();
        if (shooterGunModifierCache == null) return;

        double weightFactor = SyncConfig.WEIGHT_SPEED_MULTIPLIER.get();
        if (weightFactor > 0) {
            float targetSpeed = 0;
            targetSpeed *= (float) -weightFactor;
            AttributeModifier modifier = speedModifier.getModifier(WEIGHT_SPEED_MODIFIER);
            if (modifier == null) {
                speedModifier.removeModifier(WEIGHT_SPEED_MODIFIER);
                speedModifier.addTransientModifier(new AttributeModifier(WEIGHT_SPEED_MODIFIER,
                        targetSpeed,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        Object speed = null;
        if (speed != null) {
            double targetSpeed = _getTargetSpeed(speed);
            AttributeModifier modifier = speedModifier.getModifier(EXTRA_SPEED_MODIFIER);
            if (modifier == null) {
                speedModifier.removeModifier(EXTRA_SPEED_MODIFIER);
                speedModifier.addTransientModifier(new AttributeModifier(EXTRA_SPEED_MODIFIER,
                        targetSpeed,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }
    private double _getTargetSpeed(Object moveSpeed) {
        if (this.shooterProperty.reloadStateType.isReloading()) {
            return 0;
        } else if (this.shooterProperty.isAiming) {
            return 0;
        }
        return 0;
    }
}
