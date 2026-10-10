package dev.xcolorful.customgun.client.api.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xcolorful.customgun.client.util.ClientInputUtils.KeyAction;

/**
 * <ul>
 *     <li>26.3弃用 {@code org.lwjgl.glfw.GLFW}，改用 {@link InputConstants}</li>
 *     <li>switch case需要编译期常量</li>
 *     <li>从而扩展模组从1.21.11+被迫拆为[1.21.11, 26.3), [26.3, )两个版本</li>
 *     <li>此枚举用来减少一个需要维护的版本</li>
 * </ul>
 */
public enum InputAction {
    PRESS(KeyAction._PRESS),
    RELEASE(KeyAction._RELEASE),
    REPEAT(KeyAction._REPEAT);

    public final int keyCode;
    InputAction(int keyCode) {
        this.keyCode = keyCode;
    }

    public int keyCode() {
        return this.keyCode;
    }

    public static InputAction of(int key) {
        return switch (key) {
            case KeyAction._PRESS -> PRESS;
            case KeyAction._RELEASE -> RELEASE;
            case KeyAction._REPEAT -> REPEAT;
            default -> throw new IllegalArgumentException("Illegal key code: " + key);
        };
    }
}
