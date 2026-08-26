package dev.xcolorful.customgun.client.util;

import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ClientGuiUtils {

    public static @Nullable Screen getCurrentScreen(Minecraft minecraft) {
        return minecraft.gui.screen();
    }
    public static void setCurrentScreen(Minecraft minecraft, Screen screen) {
        minecraft.gui.setScreen(screen);
    }

    public static @Nullable Overlay getOverlay(Minecraft minecraft) {
        return minecraft.gui.overlay();
    }
    public static void setOverlay(Minecraft minecraft, Overlay overlay) {
        minecraft.gui.setOverlay(overlay);
    }

    public static void blitGuiTexture(GuiGraphicsExtractor guiGraphics,
                                      CustomTexture customTexture,
                                      int startX, int startY,
                                      int width, int height,
                                      int uOffset, int vOffset,
                                      int uWidth, int vHeight) {
        Graphics.blitGuiTexture(guiGraphics,
                customTexture.getLocation(),
                startX, startY,
                width, height,
                uOffset, vOffset,
                uWidth, vHeight,
                customTexture.getWidth(), customTexture.getHeight());

    }

    public static class Graphics {

        public static void blitGuiTexture(GuiGraphicsExtractor guiGraphics,
                                          Identifier textureLocation,
                                          int startX, int startY,
                                          int width, int height,
                                          int uOffset, int vOffset,
                                          int uWidth, int vHeight,
                                          int textureWidth, int textureHeight) {
            guiGraphics.blit(
                    // 1.21.4
//                  RenderType::guiTextured,
                    // 1.21.6
                    RenderPipelines.GUI_TEXTURED,
                    textureLocation,
                    startX, startY,
//                  width, height, // [1.20.1, 1.21.4)
                    uOffset, vOffset,
                    width, height, // [1.21.4, )
                    uWidth, vHeight,
                    textureWidth, textureHeight);
        }
        public static void blitCrosshairTexture(GuiGraphics guiGraphics,
                                          ResourceLocation textureLocation,
                                          int startX, int startY,
                                          int width, int height,
                                          int uOffset, int vOffset,
                                          int uWidth, int vHeight,
                                          int textureWidth, int textureHeight) {
            guiGraphics.blit(
                    // 1.21.4
                    RenderType::crosshair,
                    // 1.21.6
//                  RenderPipelines.CROSSHAIR,
                    textureLocation,
                    startX, startY,
//                  width, height, // [1.20.1, 1.21.4)
                    uOffset, vOffset,
                    width, height, // [1.21.4, )
                    uWidth, vHeight,
                    textureWidth, textureHeight);
        }

        public static void drawText(GuiGraphicsExtractor guiGraphics,
                                    Font font,
                                    Component component,
                                    int startX, int startY,
                                    int rgb) {
            guiGraphics.text(font,
                    component,
                    startX, startY,
                    rgb | 0xFF000000);
        }
        public static void drawText(GuiGraphicsExtractor guiGraphics,
                                    Font font,
                                    FormattedCharSequence sequence,
                                    int startX, int startY,
                                    int rgb) {
            guiGraphics.text(font,
                    sequence,
                    startX, startY,
                    rgb | 0xFF000000);
        }

        public static void drawCenteredText(GuiGraphicsExtractor guiGraphics,
                                            Font font,
                                            Component component,
                                            int startX, int startY,
                                            int rgb) {
            guiGraphics.centeredText(
                    font,
                    component,
                    startX, startY,
                    rgb | 0xFF000000);
        }
        public static void drawCenteredText(GuiGraphicsExtractor guiGraphics,
                                            Font font,
                                            FormattedCharSequence sequence,
                                            int startX, int startY,
                                            int rgb) {
            guiGraphics.centeredText(
                    font,
                    sequence,
                    startX, startY,
                    rgb | 0xFF000000);
        }

        public static void drawTooltip(GuiGraphicsExtractor guiGraphics,
                                       Font font,
                                       int startX, int startY,
                                       ItemStack itemStack) {
            guiGraphics.setTooltipForNextFrame(
                    font,
                    itemStack,
                    startX, startY);
        }

        public static void drawItem(GuiGraphicsExtractor guiGraphics,
                                    ItemStack itemStack,
                                    int startX, int startY) {
            guiGraphics.item(
                    itemStack,
                    startX, startY);
        }
    }
}
