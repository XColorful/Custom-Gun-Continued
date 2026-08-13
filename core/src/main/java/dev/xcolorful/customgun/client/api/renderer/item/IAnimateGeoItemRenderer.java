/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.renderer.item;

import dev.xcolorful.customgun.client.animation.statemachine.ItemAnimStateContext;
import dev.xcolorful.customgun.client.animation.statemachine.LuaAnimStateMachine;
import dev.xcolorful.customgun.client.model.AnimatedModelObject;
import dev.xcolorful.customgun.client.renderer.item.AnimateGeoItemRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 用于对{@link AnimateGeoItemRenderer}被子类重载的方法做一个分类
 */
public interface IAnimateGeoItemRenderer<M extends AnimatedModelObject, CTX extends ItemAnimStateContext> extends IBlockEntityWithoutLevelRenderer,
        IAnimateGeoItemRendererState, IAnimateGeoItemRendererOperator {

    CTX initContext(ItemStack pojoItem, Player player, float partialTick);

    void updateContext(CTX context, ItemStack pojoItem, Player player, float partialTick);

    // --------Getter--------

    @Nullable M getModel(ItemStack pojoItem);
    @Nullable LuaAnimStateMachine<CTX> getStateMachine(ItemStack itemStack);
    Identifier getTextureLocation(ItemStack itemStack);
    /**
     * 计算并返回切出动画的时长，单位ms
     * @return 保持时间
     */
    long getPutAwayTime(ItemStack stack);

    // --------Setter--------

    void setModel(M modelObject);
}
