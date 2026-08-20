package dev.xcolorful.customgun.client.api.gui.overlay;

public interface IOverlayMainManager extends IOverlaySubManager, IOverlayStateAccess {

    /**
     * @return 是否成功注册
     */
    boolean register(IOverlaySubManager subManager);

    /**
     * @return 是否成功取消注册
     */
    boolean unregister(IOverlaySubManager subManager);
}
