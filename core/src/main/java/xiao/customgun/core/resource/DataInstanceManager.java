/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.instance.PojoInstance;
import xiao.customgun.core.resource.instance.data.AmmoIndexInstance;
import xiao.customgun.core.resource.instance.data.AttachmentIndexInstance;
import xiao.customgun.core.resource.instance.data.BlockIndexInstance;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 存放Pojo二次校验后的实例，直接丢弃索引无效的ResourceLocation
 * <p>
 * Pojo自身的校验只包含自身(可并发各自同时校验)的类型检查，valid只保证自身接口的@Nullable/@NotNull生效，不保证跨Pojo索引生效
 */
public class DataInstanceManager {

    // data
    public static final Map<ResourceLocation, GunIndexInstance> GUN_INDEX = new HashMap<>();
    public static final Map<ResourceLocation, AttachmentIndexInstance> ATTACHMENT_INDEX = new HashMap<>();
    public static final Map<ResourceLocation, AmmoIndexInstance> AMMO_INDEX = new HashMap<>();
    public static final Map<ResourceLocation, BlockIndexInstance> BLOCK_INDEX = new HashMap<>();

    private DataInstanceManager() {}

    /**
     * 主线程操作(线程不安全)
     */
    public static void clear() {
        GUN_INDEX.clear();
        ATTACHMENT_INDEX.clear();
        AMMO_INDEX.clear();
        BLOCK_INDEX.clear();
    }
    /**
     * 主线程操作(线程不安全)
     */
    public static void reload() {
        clear();

        buildPojoInstance(ResourceApi.getAllGunIndex(), GUN_INDEX, GunIndexInstance::fromPojo, GunIndexInstance.class);
        buildPojoInstance(ResourceApi.getAllAttachmentIndex(), ATTACHMENT_INDEX, AttachmentIndexInstance::fromPojo, AttachmentIndexInstance.class);
        buildPojoInstance(ResourceApi.getAllAmmoIndex(), AMMO_INDEX, AmmoIndexInstance::fromPojo, AmmoIndexInstance.class);
        buildPojoInstance(ResourceApi.getAllBlockIndex(), BLOCK_INDEX, BlockIndexInstance::fromPojo, BlockIndexInstance.class);
    }
    public static <T extends ResourcePojo<T>, I extends PojoInstance<T>> void buildPojoInstance(
            Set<Map.Entry<ResourceLocation, T>> entries,
            Map<ResourceLocation, I> targetMap,
            Function<T, @Nullable I> instanceFactory,
            Class<I> instanceClass) {

        for (Map.Entry<ResourceLocation, T> entry : entries) {
            var location = entry.getKey();
            T pojo = entry.getValue();
            I instance = instanceFactory.apply(pojo);
            if (instance != null) targetMap.put(location, instance);
            else CustomGun.LOGGER.warn("{} {} validation failed", instanceClass, location);
        }
    }
}
