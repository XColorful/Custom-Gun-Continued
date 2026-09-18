package dev.xcolorful.customgun.client.compat.oculus;

import net.minecraft.client.renderer.MultiBufferSource;

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
    public static boolean endBatch(MultiBufferSource.BufferSource bufferSource) {
        // mixin注入点
        return false;
    }
}
