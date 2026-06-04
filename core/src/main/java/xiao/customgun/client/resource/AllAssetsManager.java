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
import xiao.customgun.client.compat.playeranimator.PlayerAnimator;
import xiao.customgun.core.api.common.McSide;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEvent;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.core.resource.AllDataManager;
import xiao.customgun.core.resource.ResourceFileManager;
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

    private AllAssetsManager() {
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
                (barrier, resourceManager, preparationProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
                    return barrier
                            .wait(Void.TYPE)
                            .thenRunAsync(AssetsInstanceManager::reload, gameExecutor);
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
        AllAssetsManager.INSTANCE.reloadAndRegister(event);
        // TODO PlayerAnimatorCompat
        if (CustomGun.getMcRegistry().isModLoaded(PlayerAnimator.MOD_ID)) {
            INSTANCE.playerAnimationManager = INSTANCE.addToListener(INSTANCE.reloadListeners, new AnimationManager.PlayerAnimationManager());
            event.addListener(INSTANCE.playerAnimationManager.getRegistryName(), INSTANCE.playerAnimationManager);
        }
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
