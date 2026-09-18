package dev.xcolorful.customgun.core.api.common;

/**
 * 等价于 net.minecraftforge.api.distmarker.Dist
 */
public enum McSide {
    CLIENT,
    DEDICATED_SERVER;

    public boolean isServerSide() {
        return this == DEDICATED_SERVER;
    }

    public boolean isClientSide() {
        return this == CLIENT;
    }
}
