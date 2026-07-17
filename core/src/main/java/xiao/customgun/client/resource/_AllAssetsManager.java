/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.event.IAddClientReloadListenerEvent;
import xiao.customgun.client.api.resource.assets.AssetsFolderType;
import xiao.customgun.client.compat.playeranimator.PlayerAnimator;
import xiao.customgun.client.resource.assets.*;
import xiao.customgun.core.api.common.McSide;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEvent;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource._AllDataManager;
import xiao.customgun.core.resource.ResourceFileManager;
import xiao.customgun.core.resource.ResourcePojoManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Go to {@link ResourceApi}
 */
@ApiStatus.Internal
public class _AllAssetsManager implements IEventHandler {
    public static final _AllAssetsManager INSTANCE = new _AllAssetsManager();

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
    /**
     * ./resourcepacks/{resourcepack}/assets/{namespace}/{@link AssetsFolderType#ANIMATIONS}
     */
    public @Nullable AnimationManager.BedrockAnimationManager bedrockAnimationManager;
    public @Nullable AnimationManager.GltfAnimationManager gltfAnimationManager;
    /**
     * ./resourcepacks/{resourcepack}/assets/{namespace}/{@link AssetsFolderType#DISPLAY}
     */
    public @Nullable DisplayManager.GunDisplayManager gunDisplayManager;
    public @Nullable DisplayManager.AttachmentDisplayManager attachmentDisplayManager;
    public @Nullable DisplayManager.AmmoDisplayManager ammoDisplayManager;
    public @Nullable DisplayManager.BlockDisplayManager blockDisplayManager;
    /**
     * ./resourcepacks/{resourcepack}/assets/{namespace}/{@link AssetsFolderType#MODEL}
     */
    public @Nullable ModelManager.BedrockModelManager bedrockModelManager;
    /**
     * ./resourcepacks/{resourcepack}/assets/{namespace}/{@link AssetsFolderType#PLAYER_ANIMATOR}
     */
    public @Nullable AnimationManager.PlayerAnimationManager playerAnimationManager;
    /**
     * ./resourcepacks/{resourcepack}/assets/{namespace}/{@link AssetsFolderType#SCRIPT}
     */
    public @Nullable ClientScriptManager clientScriptManager;

    private _AllAssetsManager() {
        this.reloadListeners = new ArrayList<>();
    }

    protected void reloadAndRegister(IAddClientReloadListenerEvent event) {
        this.reloadListeners.clear();
        // 注册时按顺序重载
        this.gunDisplayManager = addToListener(this.reloadListeners, new DisplayManager.GunDisplayManager());
        this.attachmentDisplayManager = addToListener(this.reloadListeners, new DisplayManager.AttachmentDisplayManager());
        this.ammoDisplayManager = addToListener(this.reloadListeners, new DisplayManager.AmmoDisplayManager());
        this.blockDisplayManager = addToListener(this.reloadListeners, new DisplayManager.BlockDisplayManager());

        this.bedrockModelManager = addToListener(this.reloadListeners, new ModelManager.BedrockModelManager());
        this.bedrockAnimationManager = addToListener(this.reloadListeners, new AnimationManager.BedrockAnimationManager());
        this.gltfAnimationManager = addToListener(this.reloadListeners, new AnimationManager.GltfAnimationManager());
        this.clientScriptManager = _registerListener(event, new ClientScriptManager());
        this.gunpackInfoManager = addToListener(this.reloadListeners, new GunpackInfoManager());

        this.reloadListeners.forEach((pojoManager) -> event.addListener(pojoManager.getRegistryName(), pojoManager));
        event.addListener(
                CustomGun.getMcRegistry().createResourceLocation(CustomGun.MOD_ID + ":all_assets_manager"),
                (sharedState, backgroundExecutor, barrier, gameExecutor) -> {
                    return barrier.wait("")
                            .thenRunAsync(_AssetsInstanceManager::reload, gameExecutor);
                }
        );
    }
    private <T extends ResourceFileManager<?>> T _registerListener(IAddClientReloadListenerEvent event, T listener) {
        event.addListener(listener.getRegistryName(), listener);
        return listener;
    }
    private <T extends ResourcePojoManager<?> & PreparableReloadListener> T addToListener(List<ResourcePojoManager<?>> reloadListeners, T listener) {
        reloadListeners.add(listener);
        return listener;
    }

    private static void onAddClientReloadListenerEvent(IAddClientReloadListenerEvent event) {
        SoundManager.clearCacheOnReload();

        // TODO PlayerAnimatorCompat.init()
        _AllAssetsManager.INSTANCE.reloadAndRegister(event);
        // TODO PlayerAnimatorCompat
        if (CustomGun.getMcRegistry().isModLoaded(PlayerAnimator.MOD_ID)) {
            INSTANCE.playerAnimationManager = INSTANCE.addToListener(INSTANCE.reloadListeners, new AnimationManager.PlayerAnimationManager());
            event.addListener(INSTANCE.playerAnimationManager.getRegistryName(), INSTANCE.playerAnimationManager);
        }
    }

    /**
     * 服务端指令 /cgc reload (单人游戏时线程转到客户端) 专用
     * 同步等待客户端重载完成，再触发服务端数据包重载以保证时序
     */
    public static void reloadAllPack() {
        try {
            Minecraft.getInstance().reloadResourcePacks().get();
            if (CustomGun.getMinecraftServer() != null) {
                _AllDataManager.reloadAllPack();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 物理客户端 {@link McSide#CLIENT} 客户端指令 /cgc reload_client 专用
     * 不阻塞渲染线程，避免死锁
     */
    public static void reloadClientPack() {
        try {
            Minecraft.getInstance().reloadResourcePacks();
            if (CustomGun.getMinecraftServer() != null) {
                _AllDataManager.reloadAllPack();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
