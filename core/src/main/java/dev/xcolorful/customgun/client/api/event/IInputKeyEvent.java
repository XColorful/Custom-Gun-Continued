package dev.xcolorful.customgun.client.api.event;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xcolorful.customgun.client.util.ClientInputUtils;
import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.client.input.KeyEvent;
import org.jetbrains.annotations.ApiStatus;

public interface IInputKeyEvent extends IEvent {

    @ApiStatus.AvailableSince("1.21.10")
    KeyEvent getKeyEvent();

    /**
     * [1.20.1, 26.3) {@code org.lwjgl.glfw.GLFW}
     * [26.3, ) {@link InputConstants}
     */
    int getKey();

    /**
     * @since 26.3 返回 SDL keycode (同{@link ClientInputUtils.KeyCode}封装的{@link InputConstants})
     * @see InputConstants.Key#getKey
     */
    int getScanCode();

    /**
     * @see InputConstants
     */
    int getAction();

    int getModifiers();
}
