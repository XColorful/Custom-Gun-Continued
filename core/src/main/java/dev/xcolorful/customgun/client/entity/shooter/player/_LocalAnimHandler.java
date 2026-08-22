/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.entity.shooter.player;

import dev.xcolorful.customgun.client.animation.statemachine.GunAnimStateContext;
import dev.xcolorful.customgun.client.animation.statemachine.LuaAnimStateMachine;
import dev.xcolorful.customgun.client.api.animation.statemachine.GunAnimationState;
import dev.xcolorful.customgun.client.api.event.IClientTickEvent;
import dev.xcolorful.customgun.client.api.event.IPrepareClientTickEvent;
import dev.xcolorful.customgun.client.api.event.IRenderFrameEvent;
import dev.xcolorful.customgun.client.api.item.IAnimateGeoItem;
import dev.xcolorful.customgun.client.api.renderer.item.IAnimateGeoItemRenderer;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.renderer.item.AnimateGeoItemRenderer;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.util.ClientInputUtils;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEvent;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class _LocalAnimHandler implements IEventHandler {
    private static class _LocalAnimHandlerHolder {
        private static final _LocalAnimHandler INSTANCE = new _LocalAnimHandler();
    }
    public static _LocalAnimHandler get() {
        return _LocalAnimHandlerHolder.INSTANCE;
    }
    protected _LocalAnimHandler() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case CLIENT_TICK_EVENT -> {
                onClientTick((IClientTickEvent) event);
            }
            case RENDER_FRAME_EVENT -> {
                onRenderFrame((IRenderFrameEvent) event);
            }
            default -> {
                onReceiveWrongEvent(eventType);
            }
        }
    }

    private void onClientTick(IClientTickEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack gunItem = player.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return;

        LuaAnimStateMachine<GunAnimStateContext> animStateMachine = gunDisplayInstance.getAnimStateMachine();
        this._tickAnimState(player, animStateMachine);
    }

    /**
     * 注：原模组没有检查phase，即在{@link IClientTickEvent}和{@link IPrepareClientTickEvent}都执行了tick
     */
    private void _tickAnimState(LocalPlayer player,
                                LuaAnimStateMachine<GunAnimStateContext> animStateMachine) {
        // 群组服切世界导致的特殊 BUG 处理，正常情况不会遇到此问题
        if (ClientInputUtils.Key.getInput(player) == null) {
            animStateMachine.trigger(GunAnimationState.INPUT_IDLE.getConstantName());
            return;
        }

        boolean isSneaking = player.isMovingSlowly();
        if (isSneaking) {
            // 压脚步
            animStateMachine.trigger(GunAnimationState.INPUT_IDLE.getConstantName()); // 可以增加类型
        } else if (
                ClientInputUtils.Key.moving(player) // 任意方向移动
//                || ClientInputUtils.Key.movingForward(player) // 仅判断是否有向前，会漏掉向后移动（如鬼跳）
        ) {
            // 移动
            animStateMachine.trigger(player.isSprinting()
                    ? GunAnimationState.INPUT_RUN.getConstantName() // 冲刺
                    : GunAnimationState.INPUT_WALK.getConstantName()); // 走路
        } else {
            // 待机
            animStateMachine.trigger(GunAnimationState.INPUT_IDLE.getConstantName());
        }
    }

    private void onRenderFrame(IRenderFrameEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (mc.options.getCameraType().isFirstPerson()) {
            return;
        }

        ItemStack gunItem = player.getMainHandItem();

        this._tickAnimRender(event, player, gunItem);
    }
    /**
     * 原模组移除了对{@link IGun}的限制
     * <br>
     * 若{@link AnimateGeoItemRenderer}作为API给扩展模组用，应该让扩展模组自己监听
     */
    private void _tickAnimRender(IRenderFrameEvent event,
                                 LocalPlayer player,
                                 ItemStack gunItem) {
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        // 渲染相关内容整理到物品的IClientItemExtensions了，这个接口有待进一步抽象
        @Nullable IAnimateGeoItemRenderer<?, ?> renderer = IAnimateGeoItem.cgc$getCustomRenderer(gunItem);
        if (renderer == null) return;

        // 如果物品不一样了，先尝试初始化状态机
        if (renderer.needReInit(gunItem)) {
            renderer.tryInit(gunItem, player, event.getPartialTick());
        }

        renderer.visualUpdate(gunItem);
    }
}
