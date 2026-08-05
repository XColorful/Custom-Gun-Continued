package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public interface IAddClientReloadListenerEvent extends IEvent {

    /**
     * @param registryKey 仅用于防重复
     * @since 1.21.4
     */
    void addListener(ResourceLocation registryKey,
                     PreparableReloadListener listener);
}
