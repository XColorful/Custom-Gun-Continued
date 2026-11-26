package dev.xcolorful.customgun.core.api.entity;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum ShooterAttribute implements ResourceTag.CategoryTag, ResourceTag.ConstantTag {
    WEIGHT_SPEED(ShooterAttributeTag.WEIGHT_SPEED),
    @Deprecated SHOOT_STATE_SPEED(ShooterAttributeTag.SHOOT_STATE_SPEED),
    ;

    public final String tagName;
    public final String categoryName;
    public final String constantName;
    public final Identifier attributeLocation;
    ShooterAttribute(String name) {
        this.tagName = name;
        this.categoryName = name;
        this.constantName = name;
        this.attributeLocation = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, name));
    }
    @Override public String getTagName() {
        return this.tagName;
    }
    @Override public String getCategoryName() {
        return this.categoryName;
    }
    @Override public String getConstantName() {
        return this.constantName;
    }

    public Identifier getAttributeLocation() {
        return this.attributeLocation;
    }

    private static final Map<String, ShooterAttribute> ATTRIBUTES = new HashMap<>();

    static {
        for (ShooterAttribute attribute : values()) {
            ATTRIBUTES.put(attribute.tagName, attribute);
            ATTRIBUTES.put(attribute.categoryName, attribute);
            ATTRIBUTES.put(attribute.constantName, attribute);
        }
    }

    public static @Nullable ShooterAttribute fromString(String name) {
        return name != null ? ATTRIBUTES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.categoryName;
    }
}
