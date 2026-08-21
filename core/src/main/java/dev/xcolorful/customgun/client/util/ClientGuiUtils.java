package dev.xcolorful.customgun.client.util;

import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ClientGuiUtils {

    public static @Nullable Screen getCurrentScreen(Minecraft minecraft) {
        return minecraft.screen; // minecraft.gui.screen()
    }
    public static void setCurrentScreen(Minecraft minecraft, Screen screen) {
        minecraft.setScreen(screen); // minecraft.gui.setScreen(screen)
    }

    public static @Nullable Overlay getOverlay(Minecraft minecraft) {
        return minecraft.getOverlay(); // minecraft.gui.overlay()
    }
    public static void setOverlay(Minecraft minecraft, Overlay overlay) {
        minecraft.setOverlay(overlay); // minecraft.gui.setOverlay(overlay)
    }

    public static void blitGuiTexture(GuiGraphics guiGraphics,
                                      CustomTexture customTexture,
                                      int startX, int startY,
                                      int endX, int endY,
                                      int uOffset, int vOffset,
                                      int uWidth, int vHeight) {
        Graphics.blitGuiTexture(guiGraphics,
                customTexture.getLocation(),
                startX, startY,
                endX, endY,
                uOffset, vOffset,
                uWidth, vHeight,
                customTexture.getWidth(), customTexture.getHeight());

    }

    public static class Graphics {

        public static void blitGuiTexture(GuiGraphics guiGraphics,
                                          ResourceLocation textureLocation,
                                          int startX, int startY,
                                          int endX, int endY,
                                          int uOffset, int vOffset,
                                          int uWidth, int vHeight,
                                          int textureWidth, int textureHeight) {
            guiGraphics.blit(
                    // 1.21.4
                    RenderType::guiTextured,
                    // 1.21.6
//                  RenderPipelines.GUI_TEXTURED,
                    textureLocation,
                    startX, startY,
                    endX, endY,
                    uOffset, vOffset,
                    uWidth, vHeight,
                    textureWidth, textureHeight);
        }

        public static void drawCenteredText(GuiGraphics guiGraphics,
                                            Font font,
                                            Component component,
                                            int startX, int startY,
                                            int rgb) {
            guiGraphics.drawCenteredString( // centeredText
                    font,
                    component,
                    startX, startY,
                    rgb);
        }

        public static void drawTooltip(GuiGraphics guiGraphics,
                                       Font font,
                                       int startX, int startY,
                                       ItemStack itemStack) {
            guiGraphics.renderTooltip( // setTooltipForNextFrame
                    font,
                    itemStack,
                    startX, startY);
        }

        public static void drawItem(GuiGraphics guiGraphics,
                                    ItemStack itemStack,
                                    int startX, int startY) {
            guiGraphics.renderItem( // item
                    itemStack,
                    startX, startY);
        }
    }
}
