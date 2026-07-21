package xiao.customgun.client.api.event;

import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.core.api.event.IEvent;

public interface IInputKeyEvent extends IEvent {

    @ApiStatus.AvailableSince("1.21.10")
    KeyEvent getKeyEvent();
    /**
     * 1.21.10移除, 请使用 var keyEvent
     */
    record KeyEvent(int key, int scancode, int modifiers) {}

    /**
     * @see org.lwjgl.glfw.GLFW
     */
    int getKey();

    /**
     * @see InputConstants.Key#getKey
     */
    int getScanCode();

    /**
     * @see InputConstants
     */
    int getAction();

    int getModifiers();
}
