/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.client.init;

import dev.xcolorful.customgun.client.gui.overlay.sub.DefaultCrosshair;

public class ClientSetup {

    private static final ClientSetup INSTANCE = new ClientSetup();
    public static ClientSetup get() {
        return INSTANCE;
    }
    private ClientSetup() {}

    public void onClientSetup() {
        DefaultCrosshair.init();
    }
}
