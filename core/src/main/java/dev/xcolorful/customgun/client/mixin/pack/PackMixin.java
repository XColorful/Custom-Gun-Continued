package dev.xcolorful.customgun.client.mixin.pack;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.xcolorful.customgun.client.config.ResourceConfig;
import dev.xcolorful.customgun.client.resource.pack.ResourcepackDetector;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Pack.class)
public abstract class PackMixin {

    /**
     * 枪包内容可跨版本复用，{@code pack_format} 与原版资源包无关
     * <ul>
     *     <li>不兼容时按内容识别后豁免</li>
     *     <li>客户端配置在 CONFIG_LOAD 阶段加载，早于客户端建立资源包仓库</li>
     * </ul>
     */
    @ModifyReturnValue(method = "readPackMetadata", at = @At("RETURN"))
    private static Pack.Metadata cgc$ignoreVanillaPackCompatibility(Pack.Metadata metadata,
                                                                    PackLocationInfo location, Pack.ResourcesSupplier resources, int packVersion) {
        if (metadata == null || metadata.compatibility().isCompatible()) return metadata;

        // readPackMetadata 拿不到 PackType，用当前资源包格式反推，只处理资源包
        if (packVersion != SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES)) return metadata;
        if (!ResourceConfig.IGNORE_VANILLA_PACK_COMPATIBILITY.get()) return metadata;

        try (PackResources pack = resources.openPrimary(location)) {
            if (!ResourcepackDetector.containsGunpack(pack)) return metadata;
        }

        return new Pack.Metadata(metadata.description(),
                PackCompatibility.COMPATIBLE,
                metadata.requestedFeatures(), metadata.overlays(), metadata.isHidden());
    }
}
