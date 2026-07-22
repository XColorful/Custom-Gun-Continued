package xiao.customgun.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class ClientGuiUtils {

    public static @Nullable Screen getCurrentScreen(Minecraft minecraft) {
        return minecraft.screen; // minecraft.gui.screen()
    }
    public static void setCurrentScreen(Minecraft minecraft, Screen screen) {
        minecraft.setScreen(screen); // minecraft.gui.setScreen(screen)
    }
}
