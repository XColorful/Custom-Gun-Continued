package dev.xcolorful.customgun.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xcolorful.customgun.client.api.event.IInputKeyEvent;
import dev.xcolorful.customgun.client.api.event.IMouseButtonEvent;
import dev.xcolorful.customgun.client.api.input.IInputKeySubManager;
import dev.xcolorful.customgun.client.api.input.IKeyMapping;
import dev.xcolorful.customgun.client.api.input.InputAction;
import dev.xcolorful.customgun.client.util.ClientInputUtils;

public class _InputEventHandler {

    protected static void onKeyInput(InputKeyManager inputKeyManager, IInputKeyEvent event) {
        var keyEvent = event.getKeyEvent();

        for (IInputKeySubManager subManager : inputKeyManager.getSubManagersInternal()) {
            IKeyMapping keyMapping = subManager.getKeyMapping();
            if (keyMapping.matches(keyEvent)) { // 按键匹配
                if (!_isModifierActive(keyMapping)) continue; // modifier键

                subManager.onKeyInput(inputKeyManager, event);
            } else if (InputAction.of(event.getAction()) == InputAction.RELEASE) {
                // modifier先松开时补发RELEASE, 对齐原版KeyMappingLookup的释放处理
                InputConstants.Key releaseKey = ClientInputUtils.KeyType.keyboard()
                        .getOrCreate(keyEvent.key()); // getOrCreate有缓存
                if (keyMapping.getKeyModifier().matches(releaseKey)) {
                    subManager.onKeyInput(inputKeyManager, event);
                }
            }
        }
    }

    protected static void onMouseInput(InputKeyManager inputKeyManager, IMouseButtonEvent event) {
        var mouseButtonEvent = event.getMouseButtonEvent();

        for (IInputKeySubManager subManager : inputKeyManager.getSubManagersInternal()) {
            IKeyMapping keyMapping = subManager.getKeyMapping();
            if (keyMapping.matchesMouse(mouseButtonEvent)) { // 鼠标按键匹配
                if (!_isModifierActive(keyMapping)) continue; // modifier键

                subManager.onMouseInput(inputKeyManager, event);
            }
        }
    }

    private static boolean _isModifierActive(IKeyMapping keyMapping) {
        return keyMapping.getKeyModifier().isActive(keyMapping.getKeyConflictContext());
    }
}
