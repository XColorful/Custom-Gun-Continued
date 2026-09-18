package dev.xcolorful.customgun.client.gui.overlay;

import dev.xcolorful.customgun.client.api.gui.overlay.OverlayStateAccessor;

public class OverlayState implements OverlayStateAccessor {

    public OverlayState() {}

    // --------Crosshair--------

    private long hitTimestamp = -1;
    private long killTimestamp = -1;
    private long headshotTimestamp = -1;

    @Override public void setHitTimestamp(long timestamp) {
        this.hitTimestamp = timestamp;
    }
    @Override public void setKillTimestamp(long timestamp) {
        this.killTimestamp = timestamp;
    }
    @Override public void setHeadshotTimestamp(long timestamp) {
        this.headshotTimestamp = timestamp;
    }

    @Override public long getHitTimestamp() {
        return this.hitTimestamp;
    }
    @Override public long getKillTimestamp() {
        return this.killTimestamp;
    }
    @Override public long getHeadshotTimestamp() {
        return this.headshotTimestamp;
    }
}
