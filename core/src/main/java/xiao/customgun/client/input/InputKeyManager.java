package xiao.customgun.client.input;

import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.event.IInputKeyEvent;
import xiao.customgun.client.api.event.IMouseButtonEvent;
import xiao.customgun.client.api.input.IInputKeyManager;
import xiao.customgun.client.api.input.IInputKeySubManager;
import xiao.customgun.core.api.common.McSide;
import xiao.customgun.core.api.event.*;
import xiao.customgun.core.util.ClassUtils;

import java.util.List;

public class InputKeyManager implements IInputKeyManager, IEventHandler {
    public static final InputKeyManager INSTANCE = new InputKeyManager();

    private final ClassUtils.ArraySet<IInputKeySubManager> subManagers; // 改成 KeyMapping -> IInputKeySubManager 的ArrayMap是可选优化

    protected InputKeyManager() {
        this.subManagers = new ClassUtils.ArraySet<>();
    }

    public static void init(McSide mcSide) {
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, InputKeyManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    @Override
    public boolean registerEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        customEventRegister.register(this, EventType.INPUT_KEY_EVENT, EventPriority.NORMAL, true); // subManager手动检查事件取消
        customEventRegister.register(this, EventType.MOUSE_BUTTON_EVENT, EventPriority.NORMAL, true);
        return true;
    }
    @Override
    public boolean unregisterEventHandler() {
        ICustomEventRegister customEventRegister = CustomGun.getEventRegister();
        customEventRegister.unregister(this, EventType.INPUT_KEY_EVENT, EventPriority.NORMAL, true);
        customEventRegister.unregister(this, EventType.MOUSE_BUTTON_EVENT, EventPriority.NORMAL, true);
        return true;
    }
    @Override
    public String getEventHandlerName() {
        return _MANAGER_NAME;
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case INPUT_KEY_EVENT -> this.onKeyInput(this, (IInputKeyEvent) event);
            case MOUSE_BUTTON_EVENT -> this.onMouseInput(this, (IMouseButtonEvent) event);
            default -> onReceiveWrongEvent(eventType);
        }
    }

    // --------IInputKeyMainManager--------

    @Override
    public boolean registerSubManager(IInputKeySubManager subManager) {
        if (!this.subManagers.add(subManager)) {
            CustomGun.LOGGER.debug("{} already registered", subManager.getManagerName());
            return false;
        }
        if (subManager.registerEventHandler()) {
            CustomGun.LOGGER.debug("Registered new InputKeySubManager {}", subManager.getManagerName());
            return true;
        } else {
            CustomGun.LOGGER.warn("Failed to register InputKeySubManager {}", subManager.getManagerName());
            return false;
        }
    }
    @Override
    public boolean unregisterSubManager(IInputKeySubManager subManager) {
        if (!this.subManagers.remove(subManager)) {
            CustomGun.LOGGER.debug("{} is not registered", subManager.getManagerName());
            return false;
        }
        if (subManager.unregisterEventHandler()) {
            CustomGun.LOGGER.debug("Unregistered InputKeySubManager {}", subManager.getManagerName());
            return true;
        } else {
            CustomGun.LOGGER.warn("Failed to unregister InputKeySubManager {} event handler", subManager.getManagerName());
            return false;
        }
    }

    @Override
    public List<IInputKeySubManager> getSubManagers() {
        return this.subManagers.asList();
    }
    @ApiStatus.Internal
    public ClassUtils.ArraySet<IInputKeySubManager> getSubManagersInternal() {
        return this.subManagers;
    }

    // --------IInputHandler--------

    @Override
    public void onKeyInput(IInputKeyManager inputKeyManager, IInputKeyEvent event) {
        _InputEventHandler.onKeyInput(this, event);
    }
    @Override
    public void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event) {
        _InputEventHandler.onMouseInput(this, event);
    }
}
