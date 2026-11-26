package dev.xcolorful.customgun.core.item.attachment.modifier;

import dev.xcolorful.customgun.core.api.item.attachment.modifier.IAttachmentModifier;
import dev.xcolorful.customgun.core.api.item.gun.modifier.IGunModifier;
import dev.xcolorful.customgun.core.api.minecraft.attribute.AttributeModifierOperation;
import dev.xcolorful.customgun.core.resource.data.data.attachment._SimpleModifierData;

import java.util.Collection;

public abstract class AttachmentModifier<K, V> implements IAttachmentModifier<K, V> {

    /**
     * 数值语义同 {@link AttributeModifierOperation}
     */
    public static Float evalSimpleModifierData(Collection<_SimpleModifierData> modifiers, Float base) {
        float sharedBaseAdd = 0;
        float sharedPercentAdd = 0;
        float uniqueMultiplier = 1;
        for (_SimpleModifierData modifier : modifiers) {
            sharedBaseAdd += modifier.getSharedBaseAdd();
            sharedPercentAdd += modifier.getSharedPercentAdd();
            uniqueMultiplier *= 1 + modifier.getUniqueMultiplier(); // Vanilla 语义
        }
        float value = (base + sharedBaseAdd) * (1 + sharedPercentAdd) * uniqueMultiplier;
        for (_SimpleModifierData modifier : modifiers) {
            value = IGunModifier.evalSimpleModifierDataByScript(base, value, modifier.getScriptFunction());
        }
        return value;
    }
}
