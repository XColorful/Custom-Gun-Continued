package dev.xcolorful.customgun.core.api.recipe;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum RecipeResultType implements ResourceTag.CategoryTag {
    GUN(RecipeResultTypeTag.GUN),
    ATTACHMENT(RecipeResultTypeTag.ATTACHMENT),
    AMMO(RecipeResultTypeTag.AMMO),
    CUSTOM(RecipeResultTypeTag.CUSTOM);

    public final String typeName;
    RecipeResultType(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, RecipeResultType> TYPES = new HashMap<>();

    static {
        for (RecipeResultType type : values()) {
            TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable RecipeResultType fromString(String name) {
        return name != null ? TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}