package xiao.customgun.client.api.event;

import com.mojang.blaze3d.platform.InputConstants;
import xiao.customgun.core.api.event.IEvent;

public interface IInputKeyEvent extends IEvent {

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
