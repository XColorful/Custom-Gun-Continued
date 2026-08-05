package dev.xcolorful.customgun.core.api.event;

import dev.xcolorful.customgun.core.api.common.ILogicalSideOnly;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.Nullable;

public interface ITagsUpdatedEvent extends IEvent, ILogicalSideOnly {

    /**
     * [1.20.1, 1.21.1] 为 RegistryAccess
     */
    HolderLookup.Provider getLookupProvider();
    @Deprecated(since = "1.21.4") @Nullable RegistryAccess getRegistryAccess();

    UpdateCause getUpdateCause();

    boolean shouldUpdateStaticData();

    enum UpdateCause {
        SERVER_DATA_LOAD,
        CLIENT_PACKET_RECEIVED;
    }
}
