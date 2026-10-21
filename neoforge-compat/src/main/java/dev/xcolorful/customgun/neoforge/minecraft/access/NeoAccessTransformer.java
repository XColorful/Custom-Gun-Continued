package dev.xcolorful.customgun.neoforge.minecraft.access;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.minecraft.access.ICoreAccessTransformer;

public class NeoAccessTransformer implements ICoreAccessTransformer {
    public static final NeoAccessTransformer INSTANCE = new NeoAccessTransformer();

    /**
     * 手动传NeoForge类为旧实现方式，现直接调用 {@link CustomGun#getAccessTransformer()} 即可
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(forRemoval = false)
    public NeoAccessTransformer() {
    }
}
