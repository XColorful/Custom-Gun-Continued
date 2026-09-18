package dev.xcolorful.customgun.core.api.resource;

import dev.xcolorful.customgun.core.resource.network.SyncDataType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Map;

public interface INetworkCacheReloadListener extends PreparableReloadListener {

    SyncDataType getSyncDataType();

    Map<ResourceLocation, String> getNetworkCache();
}
