package dev.xcolorful.customgun.client.api.gui.overlay;

public interface IOverlaySubManager {

    String getManagerName();
    String getOverlayName();

    boolean registerOverlayEvent();
    boolean unregisterOverlayEvent();

    boolean isEnabled();
    void setEnabled(boolean enabled);
}
