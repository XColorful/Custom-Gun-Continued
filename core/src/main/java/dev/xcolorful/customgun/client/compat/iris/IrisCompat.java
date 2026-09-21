package dev.xcolorful.customgun.client.compat.iris;

/**
 * <ul>
 *     <li>1.20.1为Oculus</li>
 *     <li>1.21.1+为Iris Shaders</li>
 * </ul>
 */
public class IrisCompat {

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
