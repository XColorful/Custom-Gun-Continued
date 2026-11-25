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
import dev.xcolorful.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.item.gun.modifier.IWeightModifier;
import dev.xcolorful.customgun.core.api.minecraft.attribute.AttributeModifierOperation;
import dev.xcolorful.customgun.core.config.SyncConfig;
import dev.xcolorful.customgun.core.util.EntityUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LivingShooterAttribute extends LivingShooterAspect {

    public static final @NotNull ResourceLocation WEIGHT_SPEED_MODIFIER_LOCATION = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:weight_speed", CustomGun.MOD_ID));
    public static final @NotNull ResourceLocation SHOOT_STATE_SPEED_MODIFIER_LOCATION = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:shoot_state_speed", CustomGun.MOD_ID));

    public LivingShooterAttribute(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public void tickAttribute() {
        if (!this.livingShooter.isAlive()) return;

        @Nullable AttributeInstance speedAttribute = this.livingShooter.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        // 1. 手持枪械检查
        if (IGunGetter.fromMainHand(this.livingShooter) == null) {
            EntityUtils._Attribute.removeAttributeModifier(speedAttribute, WEIGHT_SPEED_MODIFIER_LOCATION);
            EntityUtils._Attribute.removeAttributeModifier(speedAttribute, SHOOT_STATE_SPEED_MODIFIER_LOCATION);
            return;
        }

        @Nullable ShooterGunModifierCache shooterGunModifierCache = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter).cgc$getGunModifierCache();
        if (shooterGunModifierCache == null) return;

        this.tickWeightSpeed(shooterGunModifierCache, speedAttribute);
        this.tickShootStateSpeed(shooterGunModifierCache, speedAttribute);

    }
    private void tickWeightSpeed(ShooterGunModifierCache shooterGunModifierCache,
                                 AttributeInstance speedAttribute) {
        double weightFactor = SyncConfig.WEIGHT_SPEED_MULTIPLIER.get();
        if (weightFactor <= 0) return;

        float weightSpeed = IWeightModifier.getValue(shooterGunModifierCache, AttachmentModifierType.WEIGHT);
        weightSpeed *= (float) -weightFactor;

        @Nullable AttributeModifier weightSpeedModifier = EntityUtils._Attribute.getAttributeModifier(speedAttribute, WEIGHT_SPEED_MODIFIER_LOCATION);
        if (weightSpeedModifier == null || EntityUtils._Attribute.getAmount(weightSpeedModifier) != weightSpeed) {
            EntityUtils._Attribute.removeAttributeModifier(speedAttribute, WEIGHT_SPEED_MODIFIER_LOCATION);
            weightSpeedModifier = EntityUtils._Attribute.createAttributeModifier(SHOOT_STATE_SPEED_MODIFIER_LOCATION,
                    weightSpeed,
                    AttributeModifierOperation.SHARED_PERCENT_ADD.getOperation());
            speedAttribute.addTransientModifier(weightSpeedModifier);
        }
    }
    private void tickShootStateSpeed(ShooterGunModifierCache shooterGunModifierCache,
                                     AttributeInstance speedAttribute) {
        Object speed = null;
        if (speed == null) return;

        double movementSpeed = _getShootStateSpeed(speed);
        @Nullable AttributeModifier movementSpeedModifier = EntityUtils._Attribute.getAttributeModifier(speedAttribute, SHOOT_STATE_SPEED_MODIFIER_LOCATION);
        if (movementSpeedModifier == null || EntityUtils._Attribute.getAmount(movementSpeedModifier) != movementSpeed) {
            EntityUtils._Attribute.removeAttributeModifier(speedAttribute, SHOOT_STATE_SPEED_MODIFIER_LOCATION);
            movementSpeedModifier = EntityUtils._Attribute.createAttributeModifier(SHOOT_STATE_SPEED_MODIFIER_LOCATION,
                    movementSpeed,
                    AttributeModifierOperation.UNIQUE_MULTIPLIER.getOperation());
            speedAttribute.addTransientModifier(movementSpeedModifier);
        }
    }

    private double _getShootStateSpeed(Object moveSpeed) {
        if (this.shooterProperty.reloadStateType.isReloading()) {
            return 0;
        } else if (this.shooterProperty.isAiming) {
            return 0;
        }
        return 0;
    }
}
