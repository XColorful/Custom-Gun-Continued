package dev.xcolorful.customgun.client.gui.overlay;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.gui.overlay.IOverlayManager;
import dev.xcolorful.customgun.client.api.gui.overlay.IOverlaySubManager;
import dev.xcolorful.customgun.client.api.gui.overlay.OverlayStateAccessor;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.util.ClassUtils;
import org.jetbrains.annotations.Nullable;

public class OverlayManager implements IOverlayManager {
    public static final OverlayManager INSTANCE = new OverlayManager();

    final OverlayState overlayState;
    final ClassUtils.ArrayMap<String, IOverlaySubManager> subManagers = new ClassUtils.ArrayMap<>(IOverlaySubManager::getOverlayName);

    protected OverlayManager() {
        this.overlayState = new OverlayState();
    }

    public static void init(McSide mcSide) {
    }

    // --------IOverlayMainManager--------

    @Override
    public boolean register(IOverlaySubManager subManager) {
        String overlayName = subManager.getOverlayName();
        if (this.subManagers.containsKey(overlayName)) {

            IOverlaySubManager previous = this.subManagers.put(overlayName, subManager);
            CustomGun.LOGGER.debug("OverlayManager: registered overlay sub manager {} for overlay {} (replaced {})", subManager.getManagerName(), overlayName, previous.getManagerName());
        } else {
            this.subManagers.put(overlayName, subManager);
            CustomGun.LOGGER.debug("OverlayManager: registered overlay sub manager {} for overlay {}", subManager.getManagerName(), overlayName);
        }

        // 注册事件
        if (!subManager.registerOverlayEvent()) {
            CustomGun.LOGGER.warn("OverlayManager: Failed to register event of overlay sub manager {}", subManager.getManagerName());
        }

        return true;
    }
    @Override
    public boolean unregister(IOverlaySubManager subManager) {
        String overlayName = subManager.getOverlayName();
        @Nullable IOverlaySubManager previous = this.subManagers.mapGet(overlayName);
        if (previous == null) {
            CustomGun.LOGGER.debug("OverlayManager: overlay sub manager {} not registered for overlay {}", subManager.getManagerName(), overlayName);
            return false;
        } else if (previous != subManager) {
            CustomGun.LOGGER.debug("OverlayManager: overlay sub manager {} not equals to previous ({}) for overlay {}", subManager.getManagerName(), previous.getManagerName(), overlayName);
            return false;
        }

        // 取消注册事件
        if (!previous.unregisterOverlayEvent()) {
            CustomGun.LOGGER.warn("OverlayManager: Failed to unregister event of overlay sub manager {}", subManager.getManagerName());
        }

        this.subManagers.remove(overlayName);
        return true;
    }

    // --------IOverlaySubManager--------

    private boolean isEnabled = false;

    private static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, OverlayManager.class.getSimpleName());
    @Override
    public String getManagerName() {
        return _MANAGER_NAME;
    }

    @Override
    public String getOverlayName() {
        return "";
    }

    @Override
    public boolean registerOverlayEvent() {
        return true;
    }

    @Override
    public boolean unregisterOverlayEvent() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    // --------IOverlayStateAccess--------

    @Override
    public OverlayStateAccessor getState() {
        return this.overlayState;
    }
}
