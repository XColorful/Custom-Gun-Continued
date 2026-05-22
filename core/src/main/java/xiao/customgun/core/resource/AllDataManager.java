/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.event.*;
import xiao.customgun.core.api.resource.INetworkCacheReloadListener;
import xiao.customgun.core.api.resource.data.DataFolderType;
import xiao.customgun.core.init.registry.ModRecipe;
import xiao.customgun.core.network.message.ServerMessageSyncGunPack;
import xiao.customgun.core.recipe.TableRecipe;
import xiao.customgun.core.resource.data.*;
import xiao.customgun.core.resource.network.SyncDataType;
import xiao.customgun.core.util.SendUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AllDataManager implements IEventHandler {
    protected static final AllDataManager INSTANCE = new AllDataManager();
    private static volatile AllDataManager CURRENT;
    /**
     * 仅单人游戏/专用服务端可用
     */
    public static @Nullable AllDataManager getCurrent() {
        return CURRENT;
    }
    /**
     * 线程安全
     */
    public static void clearInstance() {
        if (AllDataManager.CURRENT == null) return;
        AllDataManager.CURRENT = null;
    }

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case ADD_SERVER_RELOAD_LISTENER_EVENT -> onAddServerReloadListenerEvent((IAddServerReloadListenerEvent) event);
            case TAGS_UPDATED_EVENT-> onTagsUpdateEvent((ITagsUpdatedEvent) event);
            case DATAPACK_SYNC_EVENT -> onDatapackSyncEvent((IDatapackSyncEvent) event);
            default -> onReceiveWrongEvent(eventType);
        }
    }

    private final List<ResourcePojoManager<?>> reloadListeners;
    private final List<INetworkCacheReloadListener> networkCacheListeners;

    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#GUNPACK_META}
     */
    public @Nullable GunpackMetaManager gunpackMetaManager;

    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#DATA}
     */
    public @Nullable DataManager.GunDataManager gunDataManager;
    public @Nullable DataManager.AttachmentDataManager attachmentDataManager;
    public @Nullable DataManager.BlockDataManager blockDataManager;
    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#INDEX}
     */
    public @Nullable IndexManager.GunIndexManager gunIndexManager;
    public @Nullable IndexManager.AttachmentIndexManager attachmentIndexManager;
    public @Nullable IndexManager.AmmoIndexManager ammoIndexManager;
    public @Nullable IndexManager.BlockIndexManager blockIndexManager;
    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#MOD_TAG}
     */
    public @Nullable ModTagManager.AttachmentTagDataManager attachmentTagManager;
    public @Nullable ModTagManager.GunAttachmentDataManager gunAttachmentDataManager;
    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#RECIPE}
     * @deprecated 注册了原版recipe解析，所以不是独立注册的数据包目录
     * 注册相同目录会连同其他recipe重复读
     */
    @Deprecated public final @Nullable RecipeDataManager recipeDataManager = null;
    public RecipeManager recipeManager;
    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#RECIPE_FILTER}
     */
    public RecipeFilterDataManager recipeFilterDataManager;
    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#SCRIPT}
     */
    public final ScriptManager scriptManager = new ScriptManager();

    private AllDataManager() {
        this.reloadListeners = new ArrayList<>();
        this.networkCacheListeners = new ArrayList<>();
    }

    protected void reloadAndRegister(IAddServerReloadListenerEvent event) {
        this.reloadListeners.clear();
        // 注册时按顺序重载
        this.gunpackMetaManager = addToListener(this.reloadListeners, new GunpackMetaManager());
        this.gunDataManager = addToListener(this.reloadListeners, new DataManager.GunDataManager());
        this.attachmentDataManager = addToListener(this.reloadListeners, new DataManager.AttachmentDataManager());
        this.blockDataManager = addToListener(this.reloadListeners, new DataManager.BlockDataManager());
        this.attachmentTagManager = addToListener(this.reloadListeners, new ModTagManager.AttachmentTagDataManager());
        this.gunAttachmentDataManager = addToListener(this.reloadListeners, new ModTagManager.GunAttachmentDataManager());
//        this.recipeDataManager = addToListener(this.reloadListeners, new RecipeDataManager());
        this.recipeFilterDataManager = addToListener(this.reloadListeners, new RecipeFilterDataManager());
        // index依赖data放在后面
        this.ammoIndexManager = addToListener(this.reloadListeners, new IndexManager.AmmoIndexManager());
        this.gunIndexManager = addToListener(this.reloadListeners, new IndexManager.GunIndexManager());
        this.attachmentIndexManager = addToListener(this.reloadListeners, new IndexManager.AttachmentIndexManager());
        this.blockIndexManager = addToListener(this.reloadListeners, new IndexManager.BlockIndexManager());

        this.reloadListeners.forEach((pojoManager) -> event.addListener(pojoManager.getRegistryName(), pojoManager));
        this.networkCacheListeners.clear();
        this.networkCacheListeners.addAll(this.reloadListeners.stream()
                .filter(INetworkCacheReloadListener.class::isInstance)
                .map(INetworkCacheReloadListener.class::cast)
                .toList()
        );
        event.addListener(
                CustomGun.getMcRegistry().createResourceLocation(CustomGun.MOD_ID + ":all_data_manager"),
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

    public Map<SyncDataType, Map<ResourceLocation, String>> getNetworkCache() {
        ImmutableMap.Builder<SyncDataType, Map<ResourceLocation, String>> builder = ImmutableMap.builder();
        for (INetworkCacheReloadListener listener : this.networkCacheListeners) {
            builder.put(listener.getSyncDataType(), listener.getNetworkCache());
        }
        return builder.build();
    }

    private static void onAddServerReloadListenerEvent(IAddServerReloadListenerEvent event) {
        AllDataManager.INSTANCE.reloadAndRegister(event);
        AllDataManager.CURRENT = AllDataManager.INSTANCE;
        AllDataManager.INSTANCE.recipeManager = event.getServerResources().getRecipeManager();
    }

    /**
     * {@link ITagsUpdatedEvent} 会在 {@link #reloadAndRegister} PojoManager {@link ResourcePojoManager#apply} 全结束之后触发
     */
    private static void onTagsUpdateEvent(ITagsUpdatedEvent event) {
        if (event.getUpdateCause() != ITagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) return;

        var _this = getCurrent();
        if (_this != null && _this.recipeManager != null) {
            List<TableRecipe> tableRecipes = _this.recipeManager.getAllRecipesFor(ModRecipe.TACZ_TABLE_RECIPE_CRAFTING.get());
            for (TableRecipe tableRecipe : tableRecipes) {
                tableRecipe.init();
            }
        }

        // 需要等ResourcePojoManager#apply全结束才执行
        DataInstanceManager.reload();
    }

    private static void onDatapackSyncEvent(IDatapackSyncEvent event) {
        if (getCurrent() == null) return;

        ServerMessageSyncGunPack message = new ServerMessageSyncGunPack(getCurrent().getNetworkCache());
        event.getRelevantPlayers().forEach(player -> SendUtils.sendMessageToPlayer(player, message));
    }

    public static void onServerStopped(MinecraftServer server) {
        AllDataManager.clearInstance();
    }

    public static void reloadAllPack() {
        MinecraftServer server = CustomGun.getMinecraftServer();
        if (server == null) return;

        PackRepository packRepository = server.getPackRepository();
        packRepository.reload();

        Collection<String> collection = packRepository.getSelectedIds();
        server.reloadResources(collection);
    }

    // --------RegisterInit--------

    @ApiStatus.Internal
    public static IEventHandler _getInternal() {
        return AllDataManager.INSTANCE;
    }
}
