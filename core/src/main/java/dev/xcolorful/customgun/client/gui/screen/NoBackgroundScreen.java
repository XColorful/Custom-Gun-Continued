package dev.xcolorful.customgun.client.gui.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    @Deprecated(since = "26.1.x")
    public void renderTransparentBackground(
            @NotNull GuiGraphicsExtractor graphics
    ) {
    }
    @ApiStatus.AvailableSince("1.21.1")
    @Deprecated(since = "26.1.x")
    protected void renderBlurredBackground(
            @NotNull GuiGraphicsExtractor graphics
    ) {
    }
    @ApiStatus.AvailableSince("1.21.1")
    @Deprecated(since = "26.1.x")
    protected void renderMenuBackground(
            @NotNull GuiGraphicsExtractor graphics
    ) {
    }
    @ApiStatus.AvailableSince("26.1.x")
    @Override
    public void extractTransparentBackground(
            @NotNull GuiGraphicsExtractor graphics
    ) {
    }
    @ApiStatus.AvailableSince("26.1.x")
    @Override
    protected void extractBlurredBackground(
            @NotNull GuiGraphicsExtractor graphics
    ) {
    }
    @ApiStatus.AvailableSince("26.1.x")
    @Override
    protected void extractMenuBackground(
            @NotNull GuiGraphicsExtractor graphics
    ) {
    }
}
