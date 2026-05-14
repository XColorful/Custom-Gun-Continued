package xiao.customgun.core.api.event;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.Nullable;

public interface ITagsUpdatedEvent extends IEvent {

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
