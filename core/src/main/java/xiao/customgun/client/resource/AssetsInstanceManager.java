/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource;

import net.minecraft.resources.ResourceLocation;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.resource.instance.data.AmmoIndexInstance;
import xiao.customgun.client.resource.instance.data.AttachmentIndexInstance;
import xiao.customgun.client.resource.instance.data.BlockIndexInstance;
import xiao.customgun.client.resource.instance.data.GunIndexInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * 存放Pojo二次校验后的实例，直接丢弃索引无效的ResourceLocation
 * <p>
 * Pojo自身的校验只包含自身(可并发各自同时校验)的类型检查，valid只保证自身接口的@Nullable/@NotNull生效，不保证跨Pojo索引生效
 */
public class AssetsInstanceManager {

    // data
    public static final Map<ResourceLocation, GunIndexInstance> GUN_INDEX = new HashMap<>();
    public static final Map<ResourceLocation, AttachmentIndexInstance> ATTACHMENT_INDEX = new HashMap<>();
    public static final Map<ResourceLocation, AmmoIndexInstance> AMMO_INDEX = new HashMap<>();
    public static final Map<ResourceLocation, BlockIndexInstance> BLOCK_INDEX = new HashMap<>();

    // assets
    public static final Map<ResourceLocation, GunDisplayInstance> GUN_DISPLAY = new HashMap<>(); // displayLocation -> GunDisplay

    private AssetsInstanceManager() {}

    public static void clear() {
        GUN_INDEX.clear();
        ATTACHMENT_INDEX.clear();
        AMMO_INDEX.clear();
        BLOCK_INDEX.clear();
        GUN_DISPLAY.clear();
    }

    public static void reload() {
        clear();
    }
}
