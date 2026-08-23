/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.shooter;

import dev.xcolorful.customgun.client.api.animation.statemachine.AnimStateMachine;
import dev.xcolorful.customgun.client.api.event.IRenderHandEvent;
import dev.xcolorful.customgun.client.api.item.IAnimateGeoItem;
import dev.xcolorful.customgun.client.api.renderer.KeepingItemRenderer;
import dev.xcolorful.customgun.client.api.renderer.item.IAnimateGeoItemRenderer;
import dev.xcolorful.customgun.client.compat.oculus.OculusCompat;
import dev.xcolorful.customgun.client.renderer.item.AnimateGeoItemRenderer;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEvent;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 原模组把逻辑移到了前置模组，从而绑架了移植
 * <ul>
 *     <li>一个跨版本的模组不能引入跨版本能力更弱的模组作为依赖</li>
 * </ul>
 */
public class FirstPersonRender implements IEventHandler {
    private static class FirstPersonRenderHolder {
        private static final FirstPersonRender INSTANCE = new FirstPersonRender();
    }
    public static FirstPersonRender get() {
        return FirstPersonRenderHolder.INSTANCE;
    }
    protected FirstPersonRender() {}

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        if (eventType == EventType.RENDER_HAND_EVENT) {
            onRenderHand((IRenderHandEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    private AnimStateMachine<?> lastStateMachine = null;

    private void onRenderHand(IRenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (event.getHand() == InteractionHand.OFF_HAND) {
            ItemStack gunItem = KeepingItemRenderer.cgc$getRenderer().cgc$getCurrentItem();
            if (IGunGetter.fromItemStack(gunItem) != null) {
                event.setCanceled(true);
            }
            return;
        }

        // 事件给的是被延长渲染修改过后的物品，不是玩家实际手持的
        ItemStack gunItem = event.getItemStack();

        /**
         * 原模组移除了对{@link IGun}的限制
         * <br>
         * 若{@link AnimateGeoItemRenderer}作为API给扩展模组用，应该让扩展模组自己监听
         */
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        // 获取 TransformType
        ItemDisplayContext transformType = event.getHand() == InteractionHand.MAIN_HAND ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        // 渲染相关内容整理到物品的IClientItemExtensions了，这个接口有待进一步抽象
        @Nullable IAnimateGeoItemRenderer<?, ?> renderer = IAnimateGeoItem.cgc$getCustomRenderer(gunItem);
        if (renderer == null) return;

        // 如果旧的状态机已经不再使用且未正常退出，使其静默退出
        AnimStateMachine<?> stateMachine = renderer.getStateMachine(gunItem);
        if (stateMachine != this.lastStateMachine) {
            if (this.lastStateMachine != null && this.lastStateMachine.isInitialized()) {
                this.lastStateMachine.exit();
            }

            this.lastStateMachine = stateMachine;
        }

        // 物品处于后台时，阻止状态机初始化
        if (!iGun.switchItemNeedReset(player.getMainHandItem(), gunItem) && renderer.needReInit(gunItem)) {
            renderer.tryInit(gunItem, player, event.getPartialTick());
        }

        // 防止内存泄漏
        OculusCompat.endBatch(mc.renderBuffers().bufferSource());

        renderer.renderFirstPerson(event.getPoseStack(),
                event,
                transformType,
                event.getPackedLight(),
                event.getPartialTick(),
                player, gunItem);
        event.setCanceled(true);
    }
}
