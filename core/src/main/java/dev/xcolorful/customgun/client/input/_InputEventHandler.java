package dev.xcolorful.customgun.client.input;

import dev.xcolorful.customgun.client.api.event.IInputKeyEvent;
import dev.xcolorful.customgun.client.api.event.IMouseButtonEvent;
import dev.xcolorful.customgun.client.api.input.IInputKeySubManager;

public class _InputEventHandler {

    protected static void onKeyInput(InputKeyManager inputKeyManager, IInputKeyEvent event) {
        var keyEvent = event.getKeyEvent();

        for (IInputKeySubManager subManager : inputKeyManager.getSubManagersInternal()) {
            if (subManager.getKeyMapping().matches(keyEvent)) {
                subManager.onKeyInput(inputKeyManager, event);
            }
        }
    }

    protected static void onMouseInput(InputKeyManager inputKeyManager, IMouseButtonEvent event) {
        var mouseButtonEvent = event.getMouseButtonEvent();

        for (IInputKeySubManager subManager : inputKeyManager.getSubManagersInternal()) {
            if (subManager.getKeyMapping().matchesMouse(mouseButtonEvent)) {
                subManager.onMouseInput(inputKeyManager, event);
            }
        }
    }
}
