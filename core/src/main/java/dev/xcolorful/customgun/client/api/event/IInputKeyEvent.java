package dev.xcolorful.customgun.client.api.event;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.client.input.KeyEvent;
import org.jetbrains.annotations.ApiStatus;

public interface IInputKeyEvent extends IEvent {

    @ApiStatus.AvailableSince("1.21.10")
    KeyEvent getKeyEvent();

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
