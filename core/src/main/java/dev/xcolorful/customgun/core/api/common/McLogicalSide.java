package dev.xcolorful.customgun.core.api.common;

import dev.xcolorful.customgun.core.api.event.ITagsUpdatedEvent;

/**
 * 等价于 {@code net.minecraftforge.fml.LogicalSide}
 * <ul>
 *     已知失效场景：
 *     <li>见 {@link ITagsUpdatedEvent} 说明</li>
 * </ul>
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
