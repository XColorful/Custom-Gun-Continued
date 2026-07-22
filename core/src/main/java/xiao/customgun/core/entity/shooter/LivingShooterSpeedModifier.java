/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.ShooterGunPropertyCache;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.config.SyncConfig;

import java.util.UUID;

public final class LivingShooterSpeedModifier extends LivingShooterAspect {

    public static final @NotNull ResourceLocation EXTRA_SPEED_MODIFIER = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:extra_speed_modifier", CustomGun.MOD_ID));
    public static final @NotNull ResourceLocation WEIGHT_SPEED_MODIFIER = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:weight_speed_modifier", CustomGun.MOD_ID));
    private static final UUID EXTRA_SPEED_MODIFIER_UUID = UUID.nameUUIDFromBytes(EXTRA_SPEED_MODIFIER.toString().getBytes());
    private static final UUID WEIGHT_SPEED_MODIFIER_UUID = UUID.nameUUIDFromBytes(WEIGHT_SPEED_MODIFIER.toString().getBytes());

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
            speedModifier.removeModifier(WEIGHT_SPEED_MODIFIER_UUID);
            speedModifier.removeModifier(EXTRA_SPEED_MODIFIER_UUID);
            return;
        }

        // 处理重量带来的修正
        @Nullable ShooterGunPropertyCache shooterGunPropertyCache = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter).cgc$getGunPropertyCache();
        if (shooterGunPropertyCache == null) return;

        double weightFactor = SyncConfig.WEIGHT_SPEED_MULTIPLIER.get();
        if (weightFactor > 0) {
            float targetSpeed = 0; // TODO WeightModifier: GunPropertyCache.getCache(WeightModifier.ID)
            targetSpeed *= (float) -weightFactor;
            AttributeModifier modifier = speedModifier.getModifier(WEIGHT_SPEED_MODIFIER_UUID);
            if (modifier == null) {
                speedModifier.removeModifier(WEIGHT_SPEED_MODIFIER_UUID);
                speedModifier.addTransientModifier(new AttributeModifier(WEIGHT_SPEED_MODIFIER_UUID, "Gun Speed Modifier",
                        targetSpeed,
                        AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }

        Object speed = null; // TODO ExtraMovementModifier: GunPropertyCache.getCache(ExtraMovementModifier.ID)
        if (speed != null) {
            double targetSpeed = _getTargetSpeed(speed);
            AttributeModifier modifier = speedModifier.getModifier(EXTRA_SPEED_MODIFIER_UUID);
            if (modifier == null) {
                speedModifier.removeModifier(EXTRA_SPEED_MODIFIER_UUID);
                speedModifier.addTransientModifier(new AttributeModifier(EXTRA_SPEED_MODIFIER_UUID, "Extra Gun Speed Modifier",
                        targetSpeed,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
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
