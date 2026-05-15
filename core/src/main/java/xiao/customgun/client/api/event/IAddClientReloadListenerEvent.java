package xiao.customgun.client.api.event;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import xiao.customgun.core.api.event.IEvent;

public interface IAddClientReloadListenerEvent extends IEvent {

    /**
     * @param registryKey 仅用于防重复
     * @since 1.21.4
     */
    void addListener(Identifier registryKey,
                     PreparableReloadListener listener);
}
