package dev.xcolorful.customgun.client.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

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
            return localPlayer.input;
            // [1.21.4, )
//          return localPlayer.input.keyPresses;
        }

        public static boolean forward(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.up; // localPlayer.input.keyPresses.forward();
        }
        public static boolean backward(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.down; // localPlayer.input.keyPresses.backward();
        }
        public static boolean left(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.left; // localPlayer.input.keyPresses.left();
        }
        public static boolean right(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.right; // localPlayer.input.keyPresses.right();
        }
        public static boolean jump(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.jumping; // localPlayer.input.keyPresses.jump();
        }
        public static boolean shift(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.shiftKeyDown; // localPlayer.input.keyPresses.shift();
        }
        public static boolean sprint(@Nullable LocalPlayer localPlayer) {
            if (localPlayer == null) return false;
            return localPlayer.input.hasForwardImpulse(); // localPlayer.input.keyPresses.sprint();
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
            return InputConstants.Type.KEYSYM;

            // [26.3, )
//            return InputConstants.Type.KEYBOARD;
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
            return GLFW.GLFW_KEY_A; // InputConstants.KEY_A;
        }
        public static int _KEY_B() {
            return GLFW.GLFW_KEY_B; // InputConstants.KEY_B;
        }
        public static int _KEY_C() {
            return GLFW.GLFW_KEY_C; // InputConstants.KEY_C;
        }
        public static int _KEY_D() {
            return GLFW.GLFW_KEY_D; // InputConstants.KEY_D;
        }
        public static int _KEY_E() {
            return GLFW.GLFW_KEY_E; // InputConstants.KEY_E;
        }
        public static int _KEY_F() {
            return GLFW.GLFW_KEY_F; // InputConstants.KEY_F;
        }
        public static int _KEY_G() {
            return GLFW.GLFW_KEY_G; // InputConstants.KEY_G;
        }
        public static int _KEY_H() {
            return GLFW.GLFW_KEY_H; // InputConstants.KEY_H;
        }
        public static int _KEY_I() {
            return GLFW.GLFW_KEY_I; // InputConstants.KEY_I;
        }
        public static int _KEY_J() {
            return GLFW.GLFW_KEY_J; // InputConstants.KEY_J;
        }
        public static int _KEY_K() {
            return GLFW.GLFW_KEY_K; // InputConstants.KEY_K;
        }
        public static int _KEY_L() {
            return GLFW.GLFW_KEY_L; // InputConstants.KEY_L;
        }
        public static int _KEY_M() {
            return GLFW.GLFW_KEY_M; // InputConstants.KEY_M;
        }
        public static int _KEY_N() {
            return GLFW.GLFW_KEY_N; // InputConstants.KEY_N;
        }
        public static int _KEY_O() {
            return GLFW.GLFW_KEY_O; // InputConstants.KEY_O;
        }
        public static int _KEY_P() {
            return GLFW.GLFW_KEY_P; // InputConstants.KEY_P;
        }
        public static int _KEY_Q() {
            return GLFW.GLFW_KEY_Q; // InputConstants.KEY_Q;
        }
        public static int _KEY_R() {
            return GLFW.GLFW_KEY_R; // InputConstants.KEY_R;
        }
        public static int _KEY_S() {
            return GLFW.GLFW_KEY_S; // InputConstants.KEY_S;
        }
        public static int _KEY_T() {
            return GLFW.GLFW_KEY_T; // InputConstants.KEY_T;
        }
        public static int _KEY_U() {
            return GLFW.GLFW_KEY_U; // InputConstants.KEY_U;
        }
        public static int _KEY_V() {
            return GLFW.GLFW_KEY_V; // InputConstants.KEY_V;
        }
        public static int _KEY_W() {
            return GLFW.GLFW_KEY_W; // InputConstants.KEY_W;
        }
        public static int _KEY_X() {
            return GLFW.GLFW_KEY_X; // InputConstants.KEY_X;
        }
        public static int _KEY_Y() {
            return GLFW.GLFW_KEY_Y; // InputConstants.KEY_Y;
        }
        public static int _KEY_Z() {
            return GLFW.GLFW_KEY_Z; // InputConstants.KEY_Z;
        }

        // ----大键盘数字----

        public static int _KEY_1() {
            return GLFW.GLFW_KEY_1; // InputConstants.KEY_1;
        }
        public static int _KEY_2() {
            return GLFW.GLFW_KEY_2; // InputConstants.KEY_2;
        }
        public static int _KEY_3() {
            return GLFW.GLFW_KEY_3; // InputConstants.KEY_3;
        }
        public static int _KEY_4() {
            return GLFW.GLFW_KEY_4; // InputConstants.KEY_4;
        }
        public static int _KEY_5() {
            return GLFW.GLFW_KEY_5; // InputConstants.KEY_5;
        }
        public static int _KEY_6() {
            return GLFW.GLFW_KEY_6; // InputConstants.KEY_6;
        }
        public static int _KEY_7() {
            return GLFW.GLFW_KEY_7; // InputConstants.KEY_7;
        }
        public static int _KEY_8() {
            return GLFW.GLFW_KEY_8; // InputConstants.KEY_8;
        }
        public static int _KEY_9() {
            return GLFW.GLFW_KEY_9; // InputConstants.KEY_9;
        }
        public static int _KEY_0() {
            return GLFW.GLFW_KEY_0; // InputConstants.KEY_0;
        }

        // ----特殊按键----

        public static int _KEY_LEFT_ALT() {
            return GLFW.GLFW_KEY_LEFT_ALT; // InputConstants.KEY_LALT;
        }
        public static int _KEY_RIGHT_ALT() {
            return GLFW.GLFW_KEY_RIGHT_ALT; // InputConstants.KEY_RALT;
        }
        public static int _KEY_LEFT_CONTROL() {
            return GLFW.GLFW_KEY_LEFT_CONTROL; // InputConstants.KEY_LCONTROL;
        }
        public static int _KEY_RIGHT_CONTROL() {
            return GLFW.GLFW_KEY_RIGHT_CONTROL; // InputConstants.KEY_RCONTROL;
        }
        public static int _KEY_LEFT_SHIFT() {
            return GLFW.GLFW_KEY_LEFT_SHIFT; // InputConstants.KEY_LSHIFT;
        }
        public static int _KEY_RIGHT_SHIFT() {
            return GLFW.GLFW_KEY_RIGHT_SHIFT; // InputConstants.KEY_RSHIFT;
        }
        public static int _KEY_TAB() {
            return GLFW.GLFW_KEY_TAB; // InputConstants.KEY_TAB;
        }

        // ----鼠标----
        // 默认不使用侧键

        public static int _MOUSE_BUTTON_LEFT() {
            return GLFW.GLFW_MOUSE_BUTTON_LEFT; // InputConstants.MOUSE_BUTTON_LEFT;
        }
        public static int _MOUSE_BUTTON_MIDDLE() {
            return GLFW.GLFW_MOUSE_BUTTON_MIDDLE; // InputConstants.MOUSE_BUTTON_MIDDLE;
        }
        public static int _MOUSE_BUTTON_RIGHT() {
            return GLFW.GLFW_MOUSE_BUTTON_RIGHT; // InputConstants.MOUSE_BUTTON_RIGHT;
        }

        // ----小键盘----
        // 默认没有，比如87键键盘

        // ----F1到F12----
        // F12以上的默认没有

        public static int _KEY_F1() {
            return GLFW.GLFW_KEY_F1; // InputConstants.KEY_F1;
        }
        public static int _KEY_F2() {
            return GLFW.GLFW_KEY_F2; // InputConstants.KEY_F2;
        }
        public static int _KEY_F3() {
            return GLFW.GLFW_KEY_F3; // InputConstants.KEY_F3;
        }
        public static int _KEY_F4() {
            return GLFW.GLFW_KEY_F4; // InputConstants.KEY_F4;
        }
        public static int _KEY_F5() {
            return GLFW.GLFW_KEY_F5; // InputConstants.KEY_F5;
        }
        public static int _KEY_F6() {
            return GLFW.GLFW_KEY_F6; // InputConstants.KEY_F6;
        }
        public static int _KEY_F7() {
            return GLFW.GLFW_KEY_F7; // InputConstants.KEY_F7;
        }
        public static int _KEY_F8() {
            return GLFW.GLFW_KEY_F8; // InputConstants.KEY_F8;
        }
        public static int _KEY_F9() {
            return GLFW.GLFW_KEY_F9; // InputConstants.KEY_F9;
        }
        public static int _KEY_F10() {
            return GLFW.GLFW_KEY_F10; // InputConstants.KEY_F10;
        }
        public static int _KEY_F11() {
            return GLFW.GLFW_KEY_F11; // InputConstants.KEY_F11;
        }
        public static int _KEY_F12() {
            return GLFW.GLFW_KEY_F12; // InputConstants.KEY_F12;
        }
    }

    /**
     * 编译器常量在switch case无法跨版本复用
     * 扩展模组需改用 {@link dev.xcolorful.customgun.client.api.input.InputAction#of}
     */
    @ApiStatus.Internal
    public static class KeyAction {

        public static final int _PRESS = GLFW.GLFW_PRESS; // InputConstants.PRESS;

        public static final int _RELEASE = GLFW.GLFW_RELEASE; // InputConstants.RELEASE;

        public static final int _REPEAT = GLFW.GLFW_REPEAT; // InputConstants.REPEAT;
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true)
    public static boolean isInGame() {
        return isGameplayFocused();
    }
}
