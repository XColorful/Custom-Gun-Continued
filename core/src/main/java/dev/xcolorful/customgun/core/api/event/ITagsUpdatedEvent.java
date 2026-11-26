package dev.xcolorful.customgun.core.api.event;

import dev.xcolorful.customgun.core.api.common.ILogicalSideOnly;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * <ul>
 *     <li>{@link ITagsUpdatedEvent} (Forge的{@code TagsUpdatedEvent}) 是在 {@link MinecraftServer#reloadResources} 的 {@code thenAcceptAsync(...)} 回调里 {@code EVENT_BUS.post} 出来的</li>
 *     <li>这个回调实际跑在 reload 的完成线程上、并不在 {@code SidedThreadGroup} 里</li>
 *     <li>逻辑端会被判定为 {@link McLogicalSide#CLIENT}，导致 {@link ResourceApi} 被误判，从而读了 {@code SyncDataCache}（空）</li>
 * </ul>
 */
public interface ITagsUpdatedEvent extends IEvent, ILogicalSideOnly {

    /**
     * [1.20.1, 1.21.1] 为 RegistryAccess
     * [26.2, ) 为 RegistryAccess
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
