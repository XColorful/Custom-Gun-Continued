package dev.xcolorful.customgun.core.api.resource;

import dev.xcolorful.customgun.core.resource.ResourcePojoManager;
import dev.xcolorful.customgun.core.resource.network.SyncDataType;
import org.jetbrains.annotations.ApiStatus;

/**
 * 扩展模组自行构造相同的 {@link INetworkCacheManager} 子接口，以复用 {@link ResourcePojoManager} 部分逻辑
 */
@ApiStatus.Internal
public interface INetworkCacheReloadListener extends INetworkCacheManager {

    SyncDataType getSyncDataType();
}
