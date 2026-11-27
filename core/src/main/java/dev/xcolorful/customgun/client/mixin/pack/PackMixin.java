package dev.xcolorful.customgun.client.mixin.pack;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.xcolorful.customgun.client.config.ResourceConfig;
import dev.xcolorful.customgun.client.resource.pack.ResourcepackDetector;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
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
    @ModifyReturnValue(method = "readPackInfo", at = @At("RETURN"))
    private static Pack.Info cgc$ignoreVanillaPackCompatibility(Pack.Info info,
                                                                String packId, Pack.ResourcesSupplier resources) {
        if (info == null) return null;

        if (info.compatibility(PackType.CLIENT_RESOURCES).isCompatible()) return info;
        if (!ResourceConfig.IGNORE_VANILLA_PACK_COMPATIBILITY.get()) return info;

        try (PackResources pack = resources.open(packId)) {
            if (!ResourcepackDetector.containsGunpack(pack)) return info;
        }

        return new Pack.Info(info.description(),
                info.dataFormat(),
                SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES),
                info.requestedFeatures(), info.hidden());
    }
}
