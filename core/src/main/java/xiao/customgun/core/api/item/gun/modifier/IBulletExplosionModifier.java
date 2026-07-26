/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun.modifier;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import xiao.customgun.core.resource.data.data.attachment._BulletExplosionModifierData;

public interface IBulletExplosionModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _BulletExplosionModifierData, _BulletExplosionModifierData> {

    @Override
    default @Nullable _BulletExplosionModifierData getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                            @NotNull GunData gunData) {
        var explosion = gunData.getBulletData().getExplosionData();
        if (explosion == null) return null;
        // Build a new instance (copy) so eval doesn't pollute the original ExplosionData
        var base = new _BulletExplosionModifierData();
        base.setEnableExplode(explosion.getEnableExplode());
        base.setEnableKnockback(explosion.getEnableKnockback());
        base.setEnableWorldDestruction(explosion.getEnableWorldDestruction());
        // TODO: explodeDamage/explodeScale/maxDelaySeconds → _SimpleModifierData
        return base;
    }

    static @Nullable _BulletExplosionModifierData getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IBulletExplosionModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, _BulletExplosionModifierData value) {
        cache.setValue(modifierHolder, IBulletExplosionModifier.class, value);
    }
}
