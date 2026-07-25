/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment.modifier;

import xiao.customgun.core.api.item.attachment.modifier.IAttachmentModifier;
import xiao.customgun.core.api.item.gun.modifier.IGunModifier;
import xiao.customgun.core.resource.data.data.attachment._SimpleModifierData;

import java.util.Collection;

public abstract class AttachmentModifier<K, V> implements IAttachmentModifier<K, V> {

    public static Float evalSimpleModifierData(Collection<_SimpleModifierData> modifiers, Float base) {
        float sharedBaseAdd = 0;
        float sharedPercentAdd = 0;
        float uniqueMultiplier = 1;
        for (_SimpleModifierData modifier : modifiers) {
            sharedBaseAdd += modifier.getSharedBaseAdd();
            sharedPercentAdd += modifier.getSharedPercentAdd();
            uniqueMultiplier *= modifier.getUniqueMultiplier();
        }
        float value = (base + sharedBaseAdd) * (1 + sharedPercentAdd) * uniqueMultiplier;
        for (_SimpleModifierData modifier : modifiers) {
            value = IGunModifier.evalSimpleModifierDataByScript(base, value, modifier.getScriptFunction());
        }
        return value;
    }
}
