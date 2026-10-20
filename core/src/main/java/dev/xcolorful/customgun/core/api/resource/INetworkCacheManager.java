package dev.xcolorful.customgun.core.api.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Map;

public interface INetworkCacheManager extends PreparableReloadListener {

    Map<ResourceLocation, String> getNetworkCache();
}
