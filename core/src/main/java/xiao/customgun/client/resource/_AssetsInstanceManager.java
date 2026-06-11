/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.resource.instance.data.ClientAmmoIndexInstance;
import xiao.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import xiao.customgun.client.resource.instance.data.ClientBlockIndexInstance;
import xiao.customgun.client.resource.instance.data.ClientGunIndexInstance;
import xiao.customgun.core.api.resource.ResourceApi;

import java.util.HashMap;
import java.util.Map;

import static xiao.customgun.core.resource._DataInstanceManager.buildPojoInstance;

/**
 * 存放Pojo二次校验后的实例，直接丢弃索引无效的ResourceLocation
 * <p>
 * Pojo自身的校验只包含自身(可并发各自同时校验)的类型检查，valid只保证自身接口的@Nullable/@NotNull生效，不保证跨Pojo索引生效
 */
@ApiStatus.Internal
public class _AssetsInstanceManager {

    // data
    public static final Map<Identifier, ClientGunIndexInstance> GUN_INDEX = new HashMap<>();
    public static final Map<Identifier, ClientAttachmentIndexInstance> ATTACHMENT_INDEX = new HashMap<>();
    public static final Map<Identifier, ClientAmmoIndexInstance> AMMO_INDEX = new HashMap<>();
    public static final Map<Identifier, ClientBlockIndexInstance> BLOCK_INDEX = new HashMap<>();

    // assets
    public static final Map<Identifier, GunDisplayInstance> GUN_DISPLAY = new HashMap<>(); // displayLocation -> GunDisplay

    private _AssetsInstanceManager() {}

    /**
     * 主线程操作(线程不安全)
     */
    public static void clear() {
        GUN_INDEX.clear();
        ATTACHMENT_INDEX.clear();
        AMMO_INDEX.clear();
        BLOCK_INDEX.clear();
        GUN_DISPLAY.clear();
    }
    /**
     * 主线程操作(线程不安全)
     */
    public static void reload() {
        clear();

        buildPojoInstance(ResourceApi.getAllGunIndex(), GUN_INDEX, ClientGunIndexInstance::fromPojo, ClientGunIndexInstance.class);
        buildPojoInstance(ResourceApi.getAllAttachmentIndex(), ATTACHMENT_INDEX, ClientAttachmentIndexInstance::fromPojo, ClientAttachmentIndexInstance.class);
        buildPojoInstance(ResourceApi.getAllAmmoIndex(), AMMO_INDEX, ClientAmmoIndexInstance::fromPojo, ClientAmmoIndexInstance.class);
        buildPojoInstance(ResourceApi.getAllBlockIndex(), BLOCK_INDEX, ClientBlockIndexInstance::fromPojo, ClientBlockIndexInstance.class);

        buildPojoInstance(ClientResourceApi.getAllGunDisplay(), GUN_DISPLAY, GunDisplayInstance::fromPojo, GunDisplayInstance.class);

        LocalPlayer player = Minecraft.getInstance().player;
        // TODO
    }
}
