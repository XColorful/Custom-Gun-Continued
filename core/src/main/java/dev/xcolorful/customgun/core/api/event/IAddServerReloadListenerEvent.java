package dev.xcolorful.customgun.core.api.event;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.Nullable;

public interface IAddServerReloadListenerEvent extends IEvent {

    /**
     * @param registryKey 仅用于防重复
     * @since 1.21.4
     */
    void addListener(ResourceLocation registryKey,
                     PreparableReloadListener listener);

    ReloadableServerResources getServerResources();

    /**
     * 改用 {@link ITagsUpdatedEvent#getLookupProvider()}
     * @deprecated {@link IAddServerReloadListenerEvent}触发的时候还不包含当前reload的tag
     */
    @Deprecated(since = "1.20.1", forRemoval = true)
    default @Nullable RegistryAccess getRegistryAccess() {
        return null;
    }
}
