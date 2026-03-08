package xiao.customgun;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import xiao.customgun.api.common.McSide;

public class CustomGun {
    public static final String MOD_ID = "customgun";
    public static final String MOD_NAME_SHORT = "cgun";
    public static final Logger LOGGER = LogUtils.getLogger();

    protected static boolean initialized;
    protected static McSide mcSide = McSide.CLIENT;

    public static void init(McSide mcSide) {
        if (initialized) return;

        initialized = true;
    }

    public static McSide getMcSide() {
        return mcSide;
    }
}
