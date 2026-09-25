package dev.xcolorful.customgun.client.compat.shouldersurfing;

import dev.xcolorful.customgun.CustomGun;

import java.lang.reflect.Method;

public class ShoulderSurfingCompat {

    public static final String MOD_ID = "shouldersurfing";

    /**
     * @return 是否显示越肩视角的准心
     */
    public static boolean showCrosshair() {
        // mixin注入点
        if (enableBuiltinCompat) return _showCrosshair();
        return false;
    }

    /**
     * @return 当前是否是越肩视角
     */
    public static boolean isShoulderSurfing() {
        // mixin注入点
        if (enableBuiltinCompat) return _isShoulderSurfing();
        return false;
    }

    public static float getXRot() {
        // mixin注入点
        if (enableBuiltinCompat) return _getXRot();
        return 0;
    }
    public static float getYRot() {
        // mixin注入点
        if (enableBuiltinCompat) return _getYRot();
        return 0;
    }

    public static void setXRot(float xRot) {
        // mixin注入点
        if (enableBuiltinCompat) _setXRot(xRot);
    }
    public static void setYRot(float yRot) {
        // mixin注入点
        if (enableBuiltinCompat) _setYRot(yRot);
    }

    public static boolean enableBuiltinCompat = false;

    // 反射字段
    // 不使用MC原版混淆字段
    private static Object shoulderSurfing;
    private static Object camera; // ShoulderSurfing的Camera是单例
    private static Object shoulderSurfingPerspective;

    private static Method getInstance;
    private static Method currentPerspective;
    private static Method isShoulderSurfing;
    private static Method getXRot;
    private static Method getYRot;
    private static Method setXRot;
    private static Method setYRot;

    public static void init() {
        if (!CustomGun.getMcRegistry().isModLoaded(MOD_ID)) return;

        try {
            Class<?> iShoulderSurfingClass = Class.forName("com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing");

            getInstance = iShoulderSurfingClass.getMethod("getInstance");
            shoulderSurfing = invoke(getInstance, null);

            Class<?> iShoulderSurfingCameraClass = Class.forName("com.github.exopandora.shouldersurfing.api.client.IShoulderSurfingCamera");
            Class<?> perspectiveClass = Class.forName("com.github.exopandora.shouldersurfing.api.client.Perspective");

            isShoulderSurfing = iShoulderSurfingClass.getMethod("isShoulderSurfing");
            Method getCamera = iShoulderSurfingClass.getMethod("getCamera");

            currentPerspective = perspectiveClass.getMethod("current");
            java.lang.reflect.Field shoulderSurfingField = perspectiveClass.getField("SHOULDER_SURFING");
            shoulderSurfingPerspective = shoulderSurfingField.get(null);

            getXRot = iShoulderSurfingCameraClass.getMethod("getXRot");
            getYRot = iShoulderSurfingCameraClass.getMethod("getYRot");
            setXRot = iShoulderSurfingCameraClass.getMethod("setXRot", float.class);
            setYRot = iShoulderSurfingCameraClass.getMethod("setYRot", float.class);

            camera = invoke(getCamera, shoulderSurfing);

            enableBuiltinCompat = true;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize Shoulder Surfing compatibility", e);
        }
    }

    private static boolean _showCrosshair() {
        return invoke(currentPerspective, null) == shoulderSurfingPerspective;
    }

    private static boolean _isShoulderSurfing() {
        return (boolean) invoke(isShoulderSurfing, shoulderSurfing);
    }

    private static float _getXRot() {
        return (float) invoke(getXRot, camera);
    }
    private static float _getYRot() {
        return (float) invoke(getYRot, camera);
    }

    private static void _setXRot(float xRot) {
        invoke(setXRot, camera, xRot);
    }
    private static void _setYRot(float yRot) {
        invoke(setYRot, camera, yRot);
    }

    private static Object invoke(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to invoke Shoulder Surfing API", e);
        }
    }
}
