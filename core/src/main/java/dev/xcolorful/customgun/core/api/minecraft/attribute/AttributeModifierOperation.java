package dev.xcolorful.customgun.core.api.minecraft.attribute;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum AttributeModifierOperation implements ResourceTag.CategoryTag {
    SHARED_BASE_ADD(AttributeModifierOperationTag.SHARED_BASE_ADD,
            AttributeModifier.Operation.values()[0],
            AttributeModifierOperationTag.SHARED_BASE_ADD_OLD1, AttributeModifierOperationTag.SHARED_BASE_ADD_OLD2),
    SHARED_PERCENT_ADD(AttributeModifierOperationTag.SHARED_PERCENT_ADD,
            AttributeModifier.Operation.values()[1],
            AttributeModifierOperationTag.SHARED_PERCENT_ADD_OLD1, AttributeModifierOperationTag.SHARED_PERCENT_ADD_OLD2),
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

    static {
        for (AttributeModifierOperation operation : AttributeModifierOperation.values()) {
            OPERATION_TYPES.put(operation.typeName, operation);
            if (operation.typeNameOld != null) {
                for(String oldName : operation.typeNameOld) {
                    OPERATION_TYPES.put(oldName, operation);
                }
            }
        }
    }

    public static @Nullable AttributeModifierOperation fromString(String name) {
        return name != null ? OPERATION_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
