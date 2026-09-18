package dev.xcolorful.customgun.client.compat.optifine;

public class OptifineCompat {

    /**
     * @return 是否接管了操作
     */
    public static boolean onEnableItemEntityStencilTest() {
        // mixin注入点
        return false;
    }
}
