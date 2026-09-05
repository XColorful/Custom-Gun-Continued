/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.item.attachment.modifier;

import dev.xcolorful.customgun.core.api.item.gun.modifier.IRecoilDataModifier;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._RecoilDataModifierData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._SimpleModifierData;
import dev.xcolorful.customgun.core.resource.data.data.gun._RecoilData;
import dev.xcolorful.customgun.core.resource.data.data.gun.recoil._RecoilEntryData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class RecoilDataModifier extends AttachmentModifier<_RecoilDataModifierData, _RecoilData>
        implements IRecoilDataModifier<AttachmentData> {
    public static final RecoilDataModifier INSTANCE = new RecoilDataModifier();

    // --------IAttachmentModifier--------

    @Override
    public @Nullable _RecoilDataModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getRecoilDataModifier();
    }

    @Override
    public _RecoilData eval(Collection<_RecoilDataModifierData> modifiers, _RecoilData base) {
        if (modifiers.isEmpty()) return base;

        List<_SimpleModifierData> pitchModifiers = new ArrayList<>();
        List<_SimpleModifierData> yawModifiers = new ArrayList<>();
        for (_RecoilDataModifierData modifier : modifiers) {
            @Nullable _SimpleModifierData pitchRecoilModifier = modifier.getPitchRecoilModifier();
            if (pitchRecoilModifier != null) pitchModifiers.add(pitchRecoilModifier);

            @Nullable _SimpleModifierData yawRecoilModifier = modifier.getYawRecoilModifier();
            if (yawRecoilModifier != null) yawModifiers.add(yawRecoilModifier);
        }

        // 每个轴得到单个倍率，统一乘到该轴所有关键帧的 range 上
        float pitchScale = evalSimpleModifierData(pitchModifiers, 1f);
        float yawScale = evalSimpleModifierData(yawModifiers, 1f);

        _RecoilData result = new _RecoilData();
        result.setPitchRecoils(_scaleRecoilEntries(base.getPitchRecoils(), pitchScale));
        result.setYawRecoils(_scaleRecoilEntries(base.getYawRecoils(), yawScale));
        return result;
    }
    private static List<_RecoilEntryData> _scaleRecoilEntries(List<_RecoilEntryData> source, float scale) {
        List<_RecoilEntryData> result = new ArrayList<>(source.size());
        for (_RecoilEntryData entry : source) {
            _RecoilEntryData copy = new _RecoilEntryData();
            copy.setTime(entry.getTime());
            float[] range = entry.getRange();
            copy.setRange(new float[]{range[0] * scale, range[1] * scale});
            result.add(copy);
        }
        return result;
    }
}
