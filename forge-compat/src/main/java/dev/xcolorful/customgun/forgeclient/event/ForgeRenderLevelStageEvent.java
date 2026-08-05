/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.forgeclient.event;

import dev.xcolorful.customgun.client.api.event.IRenderLevelStageEvent;
import dev.xcolorful.customgun.client.api.event.RenderLevelStage;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ForgeRenderLevelStageEvent extends ForgeEvent implements IRenderLevelStageEvent {

    private final RenderLevelStageEvent typedEvent;
    private final RenderLevelStage stage;

    public ForgeRenderLevelStageEvent(RenderLevelStageEvent event) {
        super(event);
        this.typedEvent = event;
        this.stage = ForgeRenderLevelStage.fromStage(event.getStage());
    }
    @Override public EventType getType() {
        return EventType.RENDER_LEVEL_STAGE_EVENT;
    }

    @Override
    public RenderLevelStage getStage() {
        return this.stage;
    }

    @Override
    public Matrix4f getModelViewMatrix() {
        return this.typedEvent.getPoseStack();
    }

    @Override
    public Vec3 getCamera_getPosition() {
        return this.typedEvent.getCamera().getPosition();
    }

    @Override
    public float getPartialTick() {
//        return this.typedEvent.getPartialTick();
        /*
         * 【修复 Forge 1.21.1 渲染抖动问题】
         * * 异常现象：使用 typedEvent.getPartialTick() 会导致渲染物体随坐标平移而剧烈抖动。
         * 原因分析：Forge 在 1.21.1 的 RenderLevelStageEvent 中传入的是 getRealtimeDeltaTicks()，
         * 该值基于系统真实增量时间，且逻辑上与游戏 Tick 步调不完全同步，甚至会出现回退现象（如 0.28 -> 0.12）。
         * 解决方案：改用 Minecraft 计时器维护的 GameTimeDeltaPartialTick。
         * 该值对应旧版本的 partialTick (deltaTickResidual)，是基于游戏逻辑刻平滑插值的系数，
         * 能保证渲染位置与玩家坐标插值（LERP）逻辑完全同步。
         */
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "ForgeRenderLevelStageEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
