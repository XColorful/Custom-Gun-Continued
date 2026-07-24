/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment.modifier;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.data.attachment._BulletExplosionModifierData;
import xiao.customgun.core.resource.data.data.gun.bullet._ExplosionData;

import java.util.Collection;

public final class BulletExplosionModifier extends AttachmentModifier<_BulletExplosionModifierData, _BulletExplosionModifierData> {
    public static final BulletExplosionModifier INSTANCE = new BulletExplosionModifier();

    // --------IAttachmentModifier--------

    @Override
    public _BulletExplosionModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getBulletExplosionModifier();
    }

    @Override
    public _BulletExplosionModifierData eval(Collection<_BulletExplosionModifierData> modifiers, _BulletExplosionModifierData base) {
        // TODO: eval 不能复用父类函数 — 爆炸有多子属性（damage/scale/delay）+ boolean（OR语义）
        return base;
    }

    // --------IGunModifier--------

    @Override
    public _BulletExplosionModifierData getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem, @NotNull GunData gunData) {
        _ExplosionData explosion = gunData.getBulletData().getExplosionData();
        _BulletExplosionModifierData data = new _BulletExplosionModifierData();
        if (explosion != null) {
            data.setEnableExplode(explosion.getEnableExplode());
            data.setEnableKnockback(explosion.getEnableKnockback());
            data.setEnableWorldDestruction(explosion.getEnableWorldDestruction());
            // TODO: 将 explodeDamage/explodeScale/maxDelaySeconds 填入对应的 _SimpleModifierData
        }
        return data;
    }
}
