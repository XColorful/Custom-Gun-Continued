/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.client.init;

import dev.xcolorful.customgun.client.gui.overlay.sub.DefaultCrosshair;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import net.minecraft.client.Minecraft;

public class ClientSetup {

    private static final ClientSetup INSTANCE = new ClientSetup();
    public static ClientSetup get() {
        return INSTANCE;
    }
    private ClientSetup() {}

    public void onClientSetup() {
        ClientRenderHelper.ensureMainRenderTargetStencil(Minecraft.getInstance()); // 没什么用，但可能增加一些兼容性

        DefaultCrosshair.init();
    }
}
