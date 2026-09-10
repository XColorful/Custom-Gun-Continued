package dev.xcolorful.customgun.client.api.minecraft.access;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface IClientAccessTransformer {

    void // 返回值
    startUseItem( // 函数名
            Minecraft minecraft // 类名
    ); // 参数列表

    @Nullable Component
    getOverlayMessageString(
            Gui gui
    );

    void
    ensureHasSentCarriedItem(
            MultiPlayerGameMode gameMode
    );

    @ApiStatus.AvailableSince("26.2")
    Object // RenderSetup
    getState(
            RenderType renderType
    );

    @ApiStatus.AvailableSince("26.2")
    Object // RenderSetup
    new_RenderSetup(
            RenderPipeline renderPipeline,
            Object renderSetup
    );
}
