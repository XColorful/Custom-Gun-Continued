package dev.xcolorful.customgun.client.network.message.player;

import dev.xcolorful.customgun.client.util.ClientGuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class _S2CMessagePlayerCraft {

    public static void updateScreen(int containerId) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && player.containerMenu.containerId == containerId) {
            updateScreen(player, ClientGuiUtils.getCurrentScreen(mc));
        }
    }

    public static void updateScreen(LocalPlayer localPlayer, Screen screen) {
        // mixin注入点
    }
}