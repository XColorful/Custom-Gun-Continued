package dev.xcolorful.customgun.core.api.minecraft.input;

import dev.xcolorful.customgun.CustomGun;
import org.jetbrains.annotations.ApiStatus;

public class CustomInputCategoryTag {

    // 1.21.10+会强行加上"key.category"前缀，并跟上RL的namespace
    public static final String PREFIX = ""; @ApiStatus.AvailableSince("1.21.10") public static final String PREFIX_OLD1 = "key.category." + CustomGun.MOD_ID + ".";

    @Deprecated(forRemoval = true) public static final String CONFIG = "config";
    public static final String PLAYER = "player";
    public static final String SHOOTER = "shooter";

    private CustomInputCategoryTag() {}
}
