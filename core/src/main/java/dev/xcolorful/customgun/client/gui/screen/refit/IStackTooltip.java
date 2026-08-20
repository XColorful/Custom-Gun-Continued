/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.screen.refit;

import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * TODO 这名字放现在看太(随)便了，原版的Components跟组件系统语义重复了，需要修改
 * <ul>
 *     <li>这引发一个思考，不考虑设计的话，直接"函数式"暴力完成需求，用速度优势弥补可维护性？</li>
 *     <li>能对人类制造阅读门槛，即使能用agent处理，也能建立token开销的门槛</li>
 *     <li>好的设计到底有没有意义？或许只有从一开始就能做出好的设计才有意义，对一个烂的设计，最优策略就是接着烂？</li>
 * </ul>
 */
public interface IStackTooltip {

    /**
     * 添加此接口，会调用此渲染文本提示
     *
     * @param consumer 需要渲染文本提示的物品
     */
    void renderTooltip(Consumer<ItemStack> consumer);
}
