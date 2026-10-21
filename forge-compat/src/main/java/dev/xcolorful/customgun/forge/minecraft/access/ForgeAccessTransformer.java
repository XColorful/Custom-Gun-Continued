package dev.xcolorful.customgun.forge.minecraft.access;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.minecraft.access.ICoreAccessTransformer;

public class ForgeAccessTransformer implements ICoreAccessTransformer {
    public static final ForgeAccessTransformer INSTANCE = new ForgeAccessTransformer();

    /**
     * 手动传Forge类为旧实现方式，现直接调用 {@link CustomGun#getAccessTransformer()} 即可
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(forRemoval = false)
    public ForgeAccessTransformer() {
    }
}
