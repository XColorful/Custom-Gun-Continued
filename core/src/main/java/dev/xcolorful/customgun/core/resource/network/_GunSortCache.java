package dev.xcolorful.customgun.core.resource.network;

import dev.xcolorful.customgun.core.api.event.ITagsUpdatedEvent;
import dev.xcolorful.customgun.core.resource._DataInstanceManager;
import dev.xcolorful.customgun.core.resource.data.index.GunIndex;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/*
文档译名：枪械排序缓存 (XiaoColorful译)
 */
public class _GunSortCache {

    private final Map<Identifier, Integer> gunSorts;

    @ApiStatus.Internal
    public _GunSortCache() {
        this.gunSorts = new HashMap<>();
    }

    /**
     * 主线程操作(线程不安全)
     * <li>命名跟{@link _DataInstanceManager#clear()}保持同构</li>
     */
    public void clear() {
        this.gunSorts.clear();
    }
    /**
     * 主线程操作(线程不安全)
     * <li>命名跟{@link _DataInstanceManager#clear()}保持同构</li>
     * <li>必须由调用方传入自己这一侧的索引实例，原因见 {@link ITagsUpdatedEvent}</li>
     */
    public void reload(@NotNull Map<Identifier, GunIndexInstance> gunIndexInstances) {
        for (Map.Entry<Identifier, GunIndexInstance> entry : gunIndexInstances.entrySet()) {
            GunIndexInstance gunIndexInstance = entry.getValue();
            GunIndex gunIndex = gunIndexInstance.getPojo();
            this.gunSorts.put(entry.getKey(), gunIndex.getSlotSort());
        }
    }

    // --------Getter--------

    public @Nullable Integer getGunSort(Identifier gunLocation) {
        return this.gunSorts.get(gunLocation);
    }
    public @NotNull Map<Identifier, Integer> getAllGunSort() {
        return this.gunSorts;
    }
}
