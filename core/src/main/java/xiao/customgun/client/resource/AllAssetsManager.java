/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.event.IAddClientReloadListenerEvent;
import xiao.customgun.client.api.resource.assets.AssetsFolderType;
import xiao.customgun.client.resource.assets.GunpackInfoManager;
import xiao.customgun.core.api.common.McSide;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEvent;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.core.resource.AllDataManager;
import xiao.customgun.core.resource.ResourcePojoManager;

import java.util.ArrayList;
import java.util.List;

public class AllAssetsManager implements IEventHandler {
    public static final AllAssetsManager INSTANCE = new AllAssetsManager();

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case ADD_CLIENT_RELOAD_LISTENER_EVENT -> onAddClientReloadListenerEvent((IAddClientReloadListenerEvent) event);
            default -> onReceiveWrongEvent(eventType);
        }
    }

    private final List<ResourcePojoManager<?>> reloadListeners;

    /**
     * ./resourcepacks/{resourcepack}/assets/{namespace}/{@link AssetsFolderType#GUNPACK_INFO}
     */
    public @Nullable GunpackInfoManager gunpackInfoManager;

    private AllAssetsManager() {
        this.reloadListeners = new ArrayList<>();
    }

    protected void reloadAndRegister(IAddClientReloadListenerEvent event) {
        this.reloadListeners.clear();
        // 注册时按顺序重载
        this.gunpackInfoManager = addToListener(this.reloadListeners, new GunpackInfoManager());

        this.reloadListeners.forEach((pojoManager) -> event.addListener(pojoManager.getRegistryName(), pojoManager));
        event.addListener(
                CustomGun.getMcRegistry().createResourceLocation(CustomGun.MOD_ID + ":all_assets_manager"),
                (barrier, resourceManager, preparationProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
                    return barrier
                            .wait(Void.TYPE)
                            .thenRunAsync(() -> {// TODO
                            }, gameExecutor);
                }
        );
    }
    private <T extends ResourcePojoManager<?> & PreparableReloadListener> T addToListener(List<ResourcePojoManager<?>> reloadListeners, T listener) {
        reloadListeners.add(listener);
        return listener;
    }

    private static void onAddClientReloadListenerEvent(IAddClientReloadListenerEvent event) {
        // TODO PlayerAnimatorCompat.init()
        AllAssetsManager.INSTANCE.reloadAndRegister(event);
        // TODO PlayerAnimatorCompat
    }

    /**
     * 物理客户端 {@link McSide#CLIENT} 执行/reload
     * 当能获取到 MinecraftServer (单人游戏) 时一并/reload数据包
     */
    public static void reloadAllPack() {
        try {
            Minecraft.getInstance().reloadResourcePacks().get();
            if (CustomGun.getMinecraftServer() != null) {
                AllDataManager.reloadAllPack();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
