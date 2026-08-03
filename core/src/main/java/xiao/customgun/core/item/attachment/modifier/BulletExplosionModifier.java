/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment.modifier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;import xiao.customgun.core.api.item.gun.modifier.IBulletExplosionModifier;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.attachment._BulletExplosionModifierData;
import xiao.customgun.core.resource.data.data.attachment._SimpleModifierData;
import xiao.customgun.core.resource.data.data.gun.bullet._ExplosionData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class BulletExplosionModifier extends AttachmentModifier<_BulletExplosionModifierData, _ExplosionData>
        implements IBulletExplosionModifier<AttachmentData> {
    public static final BulletExplosionModifier INSTANCE = new BulletExplosionModifier();

    // --------IAttachmentModifier--------

    @Override
    public @Nullable _BulletExplosionModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getBulletExplosionModifier();
    }

    @Override
    public _ExplosionData eval(Collection<_BulletExplosionModifierData> modifiers, _ExplosionData base) {
        boolean enableExplode = base.getEnableExplode();

        List<_SimpleModifierData> explodeDamageModifiers = new ArrayList<>();
        List<_SimpleModifierData> explodeScaleModifiers = new ArrayList<>();
        List<_SimpleModifierData> maxDelaySecondsModifiers = new ArrayList<>();

        boolean enableKnockBack = base.getEnableKnockback();
        boolean enableWorldDestruction = base.getEnableWorldDestruction();

        for (_BulletExplosionModifierData modifier : modifiers) {
            if (!modifier.getEnableExplode()) continue;
            enableExplode = true;

            @Nullable _SimpleModifierData explodeDamageModifier = modifier.getExplodeDamageModifier();
            if (explodeDamageModifier != null) explodeDamageModifiers.add(explodeDamageModifier);
            @Nullable _SimpleModifierData explodeScaleModifier = modifier.getExplodeScaleModifier();
            if (explodeScaleModifier != null) explodeScaleModifiers.add(explodeScaleModifier);
            @Nullable _SimpleModifierData maxDelaySecondsModifier = modifier.getMaxDelaySecondsModifier();
            if (maxDelaySecondsModifier != null) maxDelaySecondsModifiers.add(maxDelaySecondsModifier);

            enableKnockBack |= modifier.getEnableKnockback();
            enableWorldDestruction |= modifier.getEnableWorldDestruction();
        }

        base.setEnableExplode(enableExplode);

        base.setExplodeDamage(evalSimpleModifierData(explodeDamageModifiers, base.getExplodeDamage()));
        base.setExplodeScale(evalSimpleModifierData(explodeScaleModifiers, base.getExplodeScale()));
        base.setMaxDelaySeconds(evalSimpleModifierData(maxDelaySecondsModifiers, base.getMaxDelaySeconds()));

        base.setEnableKnockback(enableKnockBack);
        base.setEnableWorldDestruction(enableWorldDestruction);

        return base;
    }
}
