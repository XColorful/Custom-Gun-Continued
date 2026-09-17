/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.renderer;

import dev.xcolorful.customgun.client.mixin.renderer.FirstPersonHandsAndItemsMixin;
import dev.xcolorful.customgun.client.mixin.renderer.ItemInHandRendererMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * 用来在收物品时，让其保持一段时间渲染的接口
 */
public interface KeepingItemRenderer {

    /**
     * 物品保持渲染的时间
     *
     * @param itemStack 保持的物品
     * @param timeMs    时间，单位毫秒
     */
    void cgc$keep(ItemStack itemStack, long timeMs);

    /**
     * 获取当前主手正在渲染的物品
     */
    ItemStack cgc$getCurrentItem();

    static KeepingItemRenderer cgc$getRenderer() {
        // [1.20.1, 26.3)
        Minecraft mc = Minecraft.getInstance();
        ItemInHandRenderer itemInHandRenderer = mc.getEntityRenderDispatcher().getItemInHandRenderer();
        return cgc$fromItemInHandRenderer(itemInHandRenderer);

        // [26.3, )
//        return cgc$fromLocalPlayer(Minecraft.getInstance().player);
    }

    // --------Internal--------

    /**
     * ItemInHandRenderer 通过 Mixin 的方式实现了此接口 {@link ItemInHandRendererMixin}
     * @return 返回 ItemInHandRenderer 实例
     */
    static KeepingItemRenderer cgc$fromItemInHandRenderer(ItemInHandRenderer itemInHandRenderer) {
        return (KeepingItemRenderer) itemInHandRenderer;
    }

    /**
     * <ul>
     *     <li>26.3 把 26.2 的 {@code net.minecraft.client.renderer.ItemInHandRenderer}（全局单例）拆成了「玩家持有的状态」{@code FirstPersonHandsAndItems} + 「渲染器」FirstPersonHandsAndItemsRenderer</li>
     *     <li>持物品与抬手高度这些字段都在前者上，所以这里改由它实现本接口</li>
     *     <li>{@link FirstPersonHandsAndItemsMixin} 通过 Mixin 的方式实现了此接口</li>
     * </ul>
     * @return 返回 FirstPersonHandsAndItems 实例
     */
    @ApiStatus.AvailableSince("26.3")
    static KeepingItemRenderer cgc$fromLocalPlayer(@Nullable LocalPlayer localPlayer) {
        if (localPlayer == null) return EMPTY;
        return (KeepingItemRenderer) null; // player.firstPersonHandsAndItems();
    }
    @ApiStatus.AvailableSince("26.3")
    KeepingItemRenderer EMPTY = new KeepingItemRenderer() {
        @Override
        public void cgc$keep(ItemStack itemStack, long timeMs) {
        }

        @Override
        public ItemStack cgc$getCurrentItem() {
            return null;
        }
    };
}
