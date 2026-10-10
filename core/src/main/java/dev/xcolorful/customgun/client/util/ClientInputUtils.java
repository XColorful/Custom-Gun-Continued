package dev.xcolorful.customgun.client.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class ClientInputUtils {

    /**
     * 判断当前是否处于可进行游戏操作的状态
     * @return 当前窗口是否可以接收游戏输入
     */
    public static boolean isGameplayFocused() {
        Minecraft mc = Minecraft.getInstance();
        // 不能是加载界面
        if (ClientGuiUtils.getOverlay(mc) != null) {
            return false;
        }
        // 不能打开任何 GUI
        if (ClientGuiUtils.getCurrentScreen(mc) != null) {
            return false;
        }
        // 当前窗口捕获鼠标操作
        if (!mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        // 选择了当前窗口
        return mc.isWindowActive();
    }

    /**
     * 判断当前是否处于游戏内状态，比{@link #isGameplayFocused()}宽松
     * <br>
     * 不限制: 窗口焦点、鼠标、是否打开GUI
     * @return 当前是否可进行游戏操作
     */
    public static boolean isInGameWorld() {
        Minecraft mc = Minecraft.getInstance();
        // 不能是加载界面
        if (ClientGuiUtils.getOverlay(mc) != null) {
            return false;
        }
        return true;
    }

    public static class Key {

        public static @Nullable Input getInput(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return null;
            // [1.20.1, 1.21.4)
//          return localPlayer.input;
            // [1.21.4, )
            return localPlayer.input.keyPresses;
        }

        public static boolean forward(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.keyPresses.forward();
        }
        public static boolean backward(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.keyPresses.backward();
        }
        public static boolean left(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.keyPresses.left();
        }
        public static boolean right(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.keyPresses.right();
        }
        public static boolean jump(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.keyPresses.jump();
        }
        public static boolean shift(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.keyPresses.shift();
        }
        public static boolean sprint(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.keyPresses.sprint();
        }
        public static boolean moving(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.getMoveVector().length() > 0.01f;
        }
        public static boolean movingForward(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.hasForwardImpulse();
        }
    }

    public static class KeyType {

        public static InputConstants.Type keyboard() {
            // [1.20.1, 26.3)
//            return InputConstants.Type.KEYSYM;

            // [26.3, )
            return InputConstants.Type.KEYBOARD;
        }

        public static InputConstants.Type mouse() {
            // [1.20.1, )
            return InputConstants.Type.MOUSE;
        }
    }

    /**
     * [1.20.1, 26.3) 用 {@code org.lwjgl.glfw}
     * [26.3, ) 用 {@link InputConstants}
     */
    public static class KeyCode {

        // ----26字母----

        public static int _KEY_A() {
            return InputConstants.KEY_A;
        }
        public static int _KEY_B() {
            return InputConstants.KEY_B;
        }
        public static int _KEY_C() {
            return InputConstants.KEY_C;
        }
        public static int _KEY_D() {
            return InputConstants.KEY_D;
        }
        public static int _KEY_E() {
            return InputConstants.KEY_E;
        }
        public static int _KEY_F() {
            return InputConstants.KEY_F;
        }
        public static int _KEY_G() {
            return InputConstants.KEY_G;
        }
        public static int _KEY_H() {
            return InputConstants.KEY_H;
        }
        public static int _KEY_I() {
            return InputConstants.KEY_I;
        }
        public static int _KEY_J() {
            return InputConstants.KEY_J;
        }
        public static int _KEY_K() {
            return InputConstants.KEY_K;
        }
        public static int _KEY_L() {
            return InputConstants.KEY_L;
        }
        public static int _KEY_M() {
            return InputConstants.KEY_M;
        }
        public static int _KEY_N() {
            return InputConstants.KEY_N;
        }
        public static int _KEY_O() {
            return InputConstants.KEY_O;
        }
        public static int _KEY_P() {
            return InputConstants.KEY_P;
        }
        public static int _KEY_Q() {
            return InputConstants.KEY_Q;
        }
        public static int _KEY_R() {
            return InputConstants.KEY_R;
        }
        public static int _KEY_S() {
            return InputConstants.KEY_S;
        }
        public static int _KEY_T() {
            return InputConstants.KEY_T;
        }
        public static int _KEY_U() {
            return InputConstants.KEY_U;
        }
        public static int _KEY_V() {
            return InputConstants.KEY_V;
        }
        public static int _KEY_W() {
            return InputConstants.KEY_W;
        }
        public static int _KEY_X() {
            return InputConstants.KEY_X;
        }
        public static int _KEY_Y() {
            return InputConstants.KEY_Y;
        }
        public static int _KEY_Z() {
            return InputConstants.KEY_Z;
        }

        // ----大键盘数字----

        public static int _KEY_1() {
            return InputConstants.KEY_1;
        }
        public static int _KEY_2() {
            return InputConstants.KEY_2;
        }
        public static int _KEY_3() {
            return InputConstants.KEY_3;
        }
        public static int _KEY_4() {
            return InputConstants.KEY_4;
        }
        public static int _KEY_5() {
            return InputConstants.KEY_5;
        }
        public static int _KEY_6() {
            return InputConstants.KEY_6;
        }
        public static int _KEY_7() {
            return InputConstants.KEY_7;
        }
        public static int _KEY_8() {
            return InputConstants.KEY_8;
        }
        public static int _KEY_9() {
            return InputConstants.KEY_9;
        }
        public static int _KEY_0() {
            return InputConstants.KEY_0;
        }

        // ----特殊按键----

        public static int _KEY_LEFT_ALT() {
            return InputConstants.KEY_LALT;
        }
        public static int _KEY_RIGHT_ALT() {
            return InputConstants.KEY_RALT;
        }
        public static int _KEY_LEFT_CONTROL() {
            return InputConstants.KEY_LCONTROL;
        }
        public static int _KEY_RIGHT_CONTROL() {
            return InputConstants.KEY_RCONTROL;
        }
        public static int _KEY_LEFT_SHIFT() {
            return InputConstants.KEY_LSHIFT;
        }
        public static int _KEY_RIGHT_SHIFT() {
            return InputConstants.KEY_RSHIFT;
        }
        public static int _KEY_TAB() {
            return InputConstants.KEY_TAB;
        }

        // ----鼠标----
        // 默认不使用侧键

        public static int _MOUSE_BUTTON_LEFT() {
            return InputConstants.MOUSE_BUTTON_LEFT;
        }
        public static int _MOUSE_BUTTON_MIDDLE() {
            return InputConstants.MOUSE_BUTTON_MIDDLE;
        }
        public static int _MOUSE_BUTTON_RIGHT() {
            return InputConstants.MOUSE_BUTTON_RIGHT;
        }

        // ----小键盘----
        // 默认没有，比如87键键盘

        // ----F1到F12----
        // F12以上的默认没有

        public static int _KEY_F1() {
            return InputConstants.KEY_F1;
        }
        public static int _KEY_F2() {
            return InputConstants.KEY_F2;
        }
        public static int _KEY_F3() {
            return InputConstants.KEY_F3;
        }
        public static int _KEY_F4() {
            return InputConstants.KEY_F4;
        }
        public static int _KEY_F5() {
            return InputConstants.KEY_F5;
        }
        public static int _KEY_F6() {
            return InputConstants.KEY_F6;
        }
        public static int _KEY_F7() {
            return InputConstants.KEY_F7;
        }
        public static int _KEY_F8() {
            return InputConstants.KEY_F8;
        }
        public static int _KEY_F9() {
            return InputConstants.KEY_F9;
        }
        public static int _KEY_F10() {
            return InputConstants.KEY_F10;
        }
        public static int _KEY_F11() {
            return InputConstants.KEY_F11;
        }
        public static int _KEY_F12() {
            return InputConstants.KEY_F12;
        }
    }

    /**
     * 编译器常量在switch case无法跨版本复用
     * 扩展模组需改用 {@link dev.xcolorful.customgun.client.api.input.InputAction#of}
     */
    @ApiStatus.Internal
    public static class KeyAction {

        public static final int _PRESS = InputConstants.PRESS;

        public static final int _RELEASE = InputConstants.RELEASE;

        public static final int _REPEAT = InputConstants.REPEAT;
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true)
    public static boolean isInGame() {
        return isGameplayFocused();
    }
}
