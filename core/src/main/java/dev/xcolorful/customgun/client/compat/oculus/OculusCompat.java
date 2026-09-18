package dev.xcolorful.customgun.client.compat.oculus;

public class OculusCompat {

    public static boolean isRenderShadow() {
        // mixin注入点
        return false;
    }

    public static boolean isUsingRenderPack() {
        // mixin注入点
        return false;
    }

    /**
     * @return 是否接管渲染
     */
    public static boolean endBatch(Object bufferSource) {
        // mixin注入点
        return false;
    }
}
