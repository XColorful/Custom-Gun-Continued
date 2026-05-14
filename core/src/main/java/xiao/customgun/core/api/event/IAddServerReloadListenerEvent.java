package xiao.customgun.core.api.event;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public interface IAddServerReloadListenerEvent extends IEvent {

    /**
     * @param registryKey 仅用于防重复
     * @since 1.21.4
     */
    void addListener(ResourceLocation registryKey,
                     PreparableReloadListener listener);

    ReloadableServerResources getServerResources();

    RegistryAccess getRegistryAccess();
}
