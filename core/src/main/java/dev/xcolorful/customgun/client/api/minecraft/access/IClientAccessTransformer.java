package dev.xcolorful.customgun.client.api.minecraft.access;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
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
    RenderSetup
    getState(
            RenderType renderType
    );

    @ApiStatus.AvailableSince("26.2")
    RenderSetup
    new_RenderSetup(
            RenderPipeline renderPipeline,
            RenderSetup renderSetup
    );

    @ApiStatus.AvailableSince("26.3")
    RenderPipeline
    RenderSystem_getPIPELINE_MODIFIERS_apply(
            RenderPipeline renderPipeline
    );
}
