package dev.xcolorful.customgun.core.api.minecraft;

public enum TriBool {
    TRUE,
    FALSE,
    UNKNOWN;

    public static TriBool of(Boolean bool) {
        if (bool == null) return UNKNOWN;
        return bool ? TRUE : FALSE;
    }
}
