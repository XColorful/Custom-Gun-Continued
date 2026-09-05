/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.gun.modifier;

import dev.xcolorful.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import dev.xcolorful.customgun.core.api.script.context.GunScriptApi;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._BulletExplosionModifierData;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet._ExplosionData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IBulletExplosionModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _BulletExplosionModifierData, _ExplosionData> {

    @Override
    default @Nullable _ExplosionData getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                            @NotNull GunData gunData) {
        var explosion = gunData.getBulletData().getExplosionData();
        if (explosion == null) return null;

        var base = new _ExplosionData(); {
            base.setEnableExplode(explosion.getEnableExplode());

            base.setExplodeDamage(explosion.getExplodeDamage());
            base.setExplodeScale(explosion.getExplodeScale());
            base.setMaxDelaySeconds(explosion.getMaxDelaySeconds());

            base.setEnableKnockback(explosion.getEnableKnockback());
            base.setEnableWorldDestruction(explosion.getEnableWorldDestruction());
        }
        return base;
    }

    static @Nullable _ExplosionData getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IBulletExplosionModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, _ExplosionData value) {
        cache.setValue(modifierHolder, IBulletExplosionModifier.class, value);
    }
    static @NotNull _ExplosionData evalByScript(GunScriptApi scriptApi, @NotNull _ExplosionData value) {
        return scriptApi.getIGun().evalByScript(scriptApi.getGunItem(), scriptApi, GunModifierType.BULLET_EXPLOSION, value);
    }
}
