package xiao.customgun.client.input;

import net.minecraft.client.KeyMapping;
import xiao.customgun.client.api.event.IInputKeyEvent;
import xiao.customgun.client.api.event.IMouseButtonEvent;
import xiao.customgun.client.api.input.IInputKeySubManager;

public class _InputEventHandler {

    protected static void onKeyInput(InputKeyManager inputKeyManager, IInputKeyEvent event) {
        int key = event.getKey();
        int scanCode = event.getScanCode();
//        int action = event.getAction();
//        int modifier = event.getModifiers();

        for (IInputKeySubManager subManager : inputKeyManager.getSubManagersInternal()) {
            KeyMapping keyMapping = subManager.getKeyMapping().get();
            if (keyMapping.matches(key, scanCode)) {
                subManager.onKeyInput(inputKeyManager, event);
            }
        }
    }

    protected static void onMouseInput(InputKeyManager inputKeyManager, IMouseButtonEvent event) {
        int button = event.getButton();
//        int action = event.getAction();
//        int modifier = event.getModifiers();

        for (IInputKeySubManager subManager : inputKeyManager.getSubManagersInternal()) {
            KeyMapping keyMapping = subManager.getKeyMapping().get();
            if (keyMapping.matchesMouse(button)) {
                subManager.onMouseInput(inputKeyManager, event);
            }
        }
    }
}
