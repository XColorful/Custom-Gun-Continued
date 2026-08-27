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

    @FunctionalInterface
    public interface MainRenderTargetConfig {
        void enableStencil();
    }
    public void onConfigureMainRenderTarget(MainRenderTargetConfig config) {
        /*
        可以让第一次用模板的时候不会出现一帧黑屏
        也可能增加一些对其他模组兼容性
         */
        config.enableStencil();
    }
}
