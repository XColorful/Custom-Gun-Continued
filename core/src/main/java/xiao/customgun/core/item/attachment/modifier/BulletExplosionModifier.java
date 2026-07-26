/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment.modifier;

import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.gun.modifier.IBulletExplosionModifier;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.attachment._BulletExplosionModifierData;
import xiao.customgun.core.resource.data.data.gun.bullet._ExplosionData;

import java.util.Collection;

public final class BulletExplosionModifier extends AttachmentModifier<_BulletExplosionModifierData, _ExplosionData>
        implements IBulletExplosionModifier<AttachmentData> {
    public static final BulletExplosionModifier INSTANCE = new BulletExplosionModifier();

    // --------IAttachmentModifier--------

    @Override
    public _BulletExplosionModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getBulletExplosionModifier();
    }

    @Override
    public _ExplosionData eval(Collection<_BulletExplosionModifierData> modifiers, _ExplosionData base) {
        // TODO: eval 不能复用父类函数 — 爆炸有多子属性（damage/scale/delay）+ boolean（OR语义）
        return base;
    }
}
