package dev.xcolorful.customgun.core.resource.network;

import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource._DataInstanceManager;
import dev.xcolorful.customgun.core.resource.data.index.GunIndex;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class _GunSortCache {

    private final Map<Identifier, Integer> gunSorts;

    @ApiStatus.Internal
    public _GunSortCache() {
        this.gunSorts = new HashMap<>();
    }

    /**
     * 主线程操作(线程不安全)
     * <p>
     * 命名跟{@link _DataInstanceManager#clear()}保持同构
     */
    public void clear() {
        this.gunSorts.clear();
    }
    /**
     * 主线程操作(线程不安全)
     * <p>
     * 命名跟{@link _DataInstanceManager#clear()}保持同构
     */
    public void reload() {
        for (Map.Entry<Identifier, GunIndexInstance> entry : ResourceApi.getAllGunIndexInstance()) {
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
