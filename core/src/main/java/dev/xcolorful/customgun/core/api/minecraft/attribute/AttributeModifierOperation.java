package dev.xcolorful.customgun.core.api.minecraft.attribute;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.resource.data.data.attachment.__ModifierData;
import dev.xcolorful.customgun.core.resource.data.data.gun._ShootStateSpeed;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * 原版属性修饰符操作，与 {@link AttributeModifier.Operation} 一一对应
 * <ul>
 *     <li>计算公式：value = (base + Σ sharedBaseAdd) × (1 + Σ sharedPercentAdd) × Π (1 + uniqueMultiplier)</li>
 *     <li>三个操作的值都是<b>增量</b>，因此三个操作的默认（不修正）值都是 {@code 0}，不是 {@code 1}</li>
 *     <li>数据层的 {@link __ModifierData} 与 {@link _ShootStateSpeed} 使用同一套增量语义</li>
 * </ul>
 */
public enum AttributeModifierOperation implements ResourceTag.CategoryTag {
    /**
     * 当值为 {@code 0} 时，相当于 {@code sharedBaseAdd = 0}
     */
    SHARED_BASE_ADD(AttributeModifierOperationTag.SHARED_BASE_ADD,
            AttributeModifier.Operation.values()[0],
            AttributeModifierOperationTag.SHARED_BASE_ADD_OLD1, AttributeModifierOperationTag.SHARED_BASE_ADD_OLD2),
    /**
     * 当值为 {@code 0} 时，相当于 {@code sharedPercentAdd = 0}
     */
    SHARED_PERCENT_ADD(AttributeModifierOperationTag.SHARED_PERCENT_ADD,
            AttributeModifier.Operation.values()[1],
            AttributeModifierOperationTag.SHARED_PERCENT_ADD_OLD1, AttributeModifierOperationTag.SHARED_PERCENT_ADD_OLD2),
    /**
     * 当值为 {@code 0} 时，相当于 {@code uniqueMultiplier = 1} (Vanilla 语义)
     */
    UNIQUE_MULTIPLIER(AttributeModifierOperationTag.UNIQUE_MULTIPLIER,
            AttributeModifier.Operation.values()[2],
            AttributeModifierOperationTag.UNIQUE_MULTIPLIER_OLD1, AttributeModifierOperationTag.UNIQUE_MULTIPLIER_OLD2);

    public final String typeName;
    public final String[] typeNameOld;
    public final AttributeModifier.Operation operation;
    AttributeModifierOperation(String name,
                               AttributeModifier.Operation operation,
                               String... nameOld) {
        this.typeName = name;
        this.typeNameOld = nameOld;
        this.operation = operation;
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    public AttributeModifier.Operation getOperation() {
        return this.operation;
    }

    private static final Map<String, AttributeModifierOperation> OPERATION_TYPES = new HashMap<>();
    private static final Map<AttributeModifier.Operation, AttributeModifierOperation> vanilla_to_custom = new EnumMap<>(AttributeModifier.Operation.class);

    static {
        for (AttributeModifierOperation operation : AttributeModifierOperation.values()) {
            OPERATION_TYPES.put(operation.typeName, operation);
            if (operation.typeNameOld != null) {
                for(String oldName : operation.typeNameOld) {
                    OPERATION_TYPES.put(oldName, operation);
                }
            }
            vanilla_to_custom.put(operation.operation, operation);
        }
    }

    public static @Nullable AttributeModifierOperation fromString(String name) {
        return name != null ? OPERATION_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }

    public static AttributeModifierOperation of(AttributeModifier.Operation operation) {
        return vanilla_to_custom.get(operation);
    }
}
