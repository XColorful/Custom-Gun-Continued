package dev.xcolorful.customgun.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public abstract class NoBackgroundScreen extends Screen {

    protected NoBackgroundScreen(Component component) {
        super(component);
    }

    // --------移除虚化背景--------

    @ApiStatus.AvailableSince("1.20.2")
    @Override
    public void renderTransparentBackground(
            @NotNull GuiGraphics graphics
    ) {
    }
    @ApiStatus.AvailableSince("1.21.1")
    @Override
    protected void renderBlurredBackground(
            float partickTick
    ) {
    }
    @ApiStatus.AvailableSince("1.21.1")
    @Override
    protected void renderMenuBackground(
            @NotNull GuiGraphics graphics
    ) {
    }
    @ApiStatus.AvailableSince("26.1.x")
//    @Override
    public void extractTransparentBackground(
            @NotNull GuiGraphics graphics
    ) {
    }
    @ApiStatus.AvailableSince("26.1.x")
//    @Override
    protected void extractBlurredBackground(
            @NotNull GuiGraphics graphics
    ) {
    }
    @ApiStatus.AvailableSince("26.1.x")
//    @Override
    protected void extractMenuBackground(
            @NotNull GuiGraphics graphics
    ) {
    }
}
