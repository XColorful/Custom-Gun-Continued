package dev.xcolorful.customgun.client.api.input;

import dev.xcolorful.customgun.client.api.event.IInputKeyEvent;
import dev.xcolorful.customgun.client.api.event.IMouseButtonEvent;

/**
 * {@link IInputKeyManager}不检查modifier，{@link IInputKeySubManager}自行决定是否需要modifier隔离
 */
public interface IInputHandler {

    void onKeyInput(IInputKeyManager inputKeyManager, IInputKeyEvent event);
    void onMouseInput(IInputKeyManager inputKeyManager, IMouseButtonEvent event);
}
