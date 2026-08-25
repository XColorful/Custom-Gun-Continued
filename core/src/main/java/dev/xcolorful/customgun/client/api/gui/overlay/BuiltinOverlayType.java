package dev.xcolorful.customgun.client.api.gui.overlay;

import dev.xcolorful.customgun.core.api.gui.overlay.BuiltinOverlayTypeTag;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum BuiltinOverlayType implements ResourceTag.CategoryTag {
    CROSSHAIR(BuiltinOverlayTypeTag.CROSSHAIR)
    ;

    public final String typeName;
    BuiltinOverlayType(String name) {
        this.typeName = name;
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, BuiltinOverlayType> OVERLAY_TYPES = new HashMap<>();

    static {
        for (BuiltinOverlayType type : BuiltinOverlayType.values()) {
            OVERLAY_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable BuiltinOverlayType fromString(String name) {
        return name != null ? OVERLAY_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
