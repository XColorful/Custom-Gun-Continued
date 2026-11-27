package dev.xcolorful.customgun.client.config;

import dev.xcolorful.customgun.core.api.config.ClientModConfigTag;
import dev.xcolorful.customgun.core.api.config.IModConfigSpec;
import dev.xcolorful.customgun.core.api.config.IModConfigSpecBuilder;

public class ResourceConfig {
    public static IModConfigSpec<Boolean> IGNORE_VANILLA_PACK_COMPATIBILITY;

    public static void init(IModConfigSpecBuilder builder) {
        builder.startBuild(ClientModConfigTag.resource_path);

        builder.addComments(ClientModConfigTag.ignoreVanillaPackCompatibility_comment);
        IGNORE_VANILLA_PACK_COMPATIBILITY = builder.addConfig(ClientModConfigTag.ignoreVanillaPackCompatibility_path, true);

        builder.addComments(ClientModConfigTag.enableLazyClientAssetLoad_comment);
        ENABLE_LAZY_CLIENT_ASSET_LOAD = builder.addConfig(ClientModConfigTag.enableLazyClientAssetLoad_path, true);

        builder.finishBuild();
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true) public static IModConfigSpec<Boolean> ENABLE_LAZY_CLIENT_ASSET_LOAD;
}