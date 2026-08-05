/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;

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

    /**
     * ItemInHandRenderer 通过 Mixin 的方式实现了此接口
     * @return 返回 ItemInHandRenderer 实例
     */
    static KeepingItemRenderer cgc$fromItemInHandRenderer(ItemInHandRenderer itemInHandRenderer) {
        return (KeepingItemRenderer) itemInHandRenderer;
    }
    static KeepingItemRenderer cgc$getRenderer(){
        return cgc$fromItemInHandRenderer(Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer());
    }
}
