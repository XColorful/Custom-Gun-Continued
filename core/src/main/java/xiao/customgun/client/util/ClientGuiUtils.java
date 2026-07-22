package xiao.customgun.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
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
}
