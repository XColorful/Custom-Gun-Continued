package xiao.customgun.core.resource.network;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.GunDataAccessor;
import xiao.customgun.core.resource._DataInstanceManager;
import xiao.customgun.core.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 查询声明的Attachment安装关系
 * <ul>
 *     <li>仅判断是否包含 "Attachment->Gun" 映射</li>
 *     <li>实际 "Gun->Attachment" 应使用{@link IGun}加一层动态拦截, 如{@link GunDataAccessor#canInstallAttachment}</li>
 * </ul>
 */
public class _AttachmentInstallabilityCache {

    private final Map<ResourceLocation, ClassUtils.ArraySet<ResourceLocation>> attachmentInstallability;

    @ApiStatus.Internal
    public _AttachmentInstallabilityCache() {
        this.attachmentInstallability = new HashMap<>();
    }

    /**
     * 主线程操作(线程不安全)
     * <p>
     * 命名跟{@link _DataInstanceManager#clear()}保持同构
     */
    public void clear() {
        this.attachmentInstallability.clear();
    }
    /**
     * 主线程操作(线程不安全)
     * <p>
     * 命名跟{@link _DataInstanceManager#clear()}保持同构
     */
    public void reload() {
    }

    public boolean hasAttachmentInstallability(ResourceLocation attachmentLocation, ResourceLocation gunLocation) {
        var installability = this.getAttachmentInstallability(attachmentLocation);
        return installability != null && installability.contains(gunLocation);
    }
    public @Nullable ClassUtils.ArraySet<ResourceLocation> getAttachmentInstallability(ResourceLocation attachmentLocation) {
        return this.attachmentInstallability.get(attachmentLocation);
    }
}
