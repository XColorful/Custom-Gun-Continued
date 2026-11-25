package dev.xcolorful.customgun.core.api.minecraft.attribute;

import dev.xcolorful.customgun.core.api.resource.data.data.attachment.__ModifierDataTag;

public class AttributeModifierOperationTag {

    public static final String SHARED_BASE_ADD = __ModifierDataTag.SHARED_BASE_ADD; public static final String SHARED_BASE_ADD_OLD1 = "addition"; public static final String SHARED_BASE_ADD_OLD2 = "add_value";
    public static final String SHARED_PERCENT_ADD = __ModifierDataTag.SHARED_PERCENT_ADD; public static final String SHARED_PERCENT_ADD_OLD1 = "percent"; public static final String SHARED_PERCENT_ADD_OLD2 = "add_multiplied_base";
    public static final String UNIQUE_MULTIPLIER = __ModifierDataTag.UNIQUE_MULTIPLIER; public static final String UNIQUE_MULTIPLIER_OLD1 = "multiply"; public static final String UNIQUE_MULTIPLIER_OLD2 = "add_multiplied_total";

    private AttributeModifierOperationTag() {}
}
