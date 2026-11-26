/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.entity.shooter;

import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShootState;
import dev.xcolorful.customgun.core.api.entity.ShooterAttribute;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import dev.xcolorful.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.item.gun.modifier.IWeightModifier;
import dev.xcolorful.customgun.core.api.minecraft.attribute.AttributeModifierOperation;
import dev.xcolorful.customgun.core.config.ShooterConfig;
import dev.xcolorful.customgun.core.config.SyncConfig;
import dev.xcolorful.customgun.core.util.EntityUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public final class LivingShooterAttribute extends LivingShooterAspect {

    public LivingShooterAttribute(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public void tickAttribute() {
        if (!this.livingShooter.isAlive()) return;

        @Nullable AttributeInstance speedAttribute = this.livingShooter.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        // 1. 手持枪械检查
        if (IGunGetter.fromMainHand(this.livingShooter) == null) {
            clearAttributes(speedAttribute);
            return;
        }

        @Nullable ShooterGunModifierCache shooterGunModifierCache = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter).cgc$getGunModifierCache();
        if (shooterGunModifierCache == null || !ShooterConfig.ENABLE_WEIGHT_MODIFIER.get()) {
            clearAttributes(speedAttribute);
            return;
        }

        this.tickWeightSpeed(shooterGunModifierCache, speedAttribute);
//        this.tickShootStateSpeed(shooterGunModifierCache, speedAttribute);
    }

    private void clearAttributes(AttributeInstance speedAttribute) {
        EntityUtils._Attribute.removeAttributeModifier(speedAttribute, ShooterAttribute.WEIGHT_SPEED.getAttributeLocation());
//            EntityUtils._Attribute.removeAttributeModifier(speedAttribute, ShooterAttribute.SHOOT_STATE_SPEED.getAttributeLocation());
    }

    private void tickWeightSpeed(ShooterGunModifierCache shooterGunModifierCache,
                                 AttributeInstance speedAttribute) {
        double weightFactor = SyncConfig.WEIGHT_SPEED_MULTIPLIER.get();
//        if (weightFactor <= 0) return; // 避免Attribute残留未清理

        @Nullable Float weightSpeed = IWeightModifier.getValue(shooterGunModifierCache, AttachmentModifierType.WEIGHT);
        if (weightSpeed == null) weightSpeed = 0f;
        weightSpeed *= (float) -weightFactor;

        var weightSpeedLocation = ShooterAttribute.WEIGHT_SPEED.getAttributeLocation();
        @Nullable AttributeModifier weightSpeedModifier = EntityUtils._Attribute.getAttributeModifier(speedAttribute, weightSpeedLocation);
        if (weightSpeedModifier == null || EntityUtils._Attribute.getAmount(weightSpeedModifier) != weightSpeed) {
            EntityUtils._Attribute.removeAttributeModifier(speedAttribute, weightSpeedLocation);
            if (weightSpeed == 0) return; // 没有数值加成(无效果)就跳过写入

            weightSpeedModifier = EntityUtils._Attribute.createAttributeModifier(weightSpeedLocation,
                    weightSpeed,
                    AttributeModifierOperation.SHARED_PERCENT_ADD.getOperation());
            speedAttribute.addTransientModifier(weightSpeedModifier);
        }
    }
    @Deprecated(forRemoval = true)
    private void tickShootStateSpeed(ShooterGunModifierCache shooterGunModifierCache,
                                     AttributeInstance speedAttribute) {
        Object speed = null;
        if (speed == null) return;

        double shootStateSpeed = _getShootStateSpeed(speed);
        var shootStateLocation = ShooterAttribute.SHOOT_STATE_SPEED.getAttributeLocation();
        @Nullable AttributeModifier movementSpeedModifier = EntityUtils._Attribute.getAttributeModifier(speedAttribute, shootStateLocation);
        if (movementSpeedModifier == null || EntityUtils._Attribute.getAmount(movementSpeedModifier) != shootStateSpeed) {
            EntityUtils._Attribute.removeAttributeModifier(speedAttribute, shootStateLocation);
            if (shootStateSpeed == 0) return; // 没有增量修正(无效果)就跳过写入

            movementSpeedModifier = EntityUtils._Attribute.createAttributeModifier(shootStateLocation,
                    shootStateSpeed,
                    AttributeModifierOperation.UNIQUE_MULTIPLIER.getOperation());
            speedAttribute.addTransientModifier(movementSpeedModifier);
        }
    }

    private double _getShootStateSpeed(Object moveSpeed) {
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        EnumSet<ShootState> shootStates = ShootState.of(iLivingShooter, this.livingShooter);

        if (this.shooterProperty.reloadStateType.isReloading()) {
            return 0;
        } else if (this.shooterProperty.isAiming) {
            return 0;
        }
        return 0;
    }
}
