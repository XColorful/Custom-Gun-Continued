package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public interface IAddClientReloadListenerEvent extends IEvent {

    /**
     * @param registryKey 仅用于防重复
     * @since 1.21.4
     */
    void addListener(Identifier registryKey,
                     PreparableReloadListener listener);
}
