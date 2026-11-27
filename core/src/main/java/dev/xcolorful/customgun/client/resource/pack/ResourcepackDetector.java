package dev.xcolorful.customgun.client.resource.pack;

import dev.xcolorful.customgun.core.api.resource.assets.AssetsFolderName;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

/**
 * 按内容识别枪包资源包，用于豁免原版的 {@code pack_format} 兼容性检查
 * <ul>
 *     <li>{@link PackResources#listResources} 无法列出 namespace 根目录</li>
 *     <li>空目录参数在 zip 实现里会拼出 {@code assets/ns//}，文件夹实现里会被 {@code decomposePath} 判非法</li>
 *     <li>因此按目录名逐个探测</li>
 * </ul>
 * 注：旧格式根目录下的 {@code gunpack_info.json} 为非标准数据包/资源包内容，不纳入
 */
public final class ResourcepackDetector {

    private static final String[] MARKERS = {
            AssetsFolderName.GUNPACK_INFO,
            AssetsFolderName.DISPLAY, AssetsFolderName.DISPLAY_OLD1,
            AssetsFolderName.SCRIPT, AssetsFolderName.SCRIPT_OLD1,
            AssetsFolderName.MODEL, AssetsFolderName.MODEL_OLD1
    };

    private ResourcepackDetector() {}

    public static boolean containsGunpack(PackResources pack) {
        for (String namespace : pack.getNamespaces(PackType.CLIENT_RESOURCES)) {
            for (String marker : MARKERS) {
                if (hasResource(pack, namespace, marker)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasResource(PackResources pack, String namespace, String folder) {
        boolean[] found = new boolean[1];
        pack.listResources(PackType.CLIENT_RESOURCES, namespace, folder, (location, supplier) -> found[0] = true);
        return found[0];
    }
}
