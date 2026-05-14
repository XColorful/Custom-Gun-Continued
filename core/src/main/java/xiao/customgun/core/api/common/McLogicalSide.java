package xiao.customgun.core.api.common;

/**
 * 等价于 net.minecraftforge.fml.LogicalSide
 */
public enum McLogicalSide {
    CLIENT,
    SERVER;

    public boolean isServer() {
        return !isClient();
    }

    public boolean isClient() {
        return this == CLIENT;
    }
}
