package dev.xcolorful.customgun.core.util;

import net.minecraft.server.MinecraftServer;

public class ServerUtils {

    public static long[] getTickTimesNanos(MinecraftServer server) {
        // [1.20.1, 1.20,4)
//        return server.tickTimes;
        // 1.20.4
        return server.tickTimesNanos;
        // [1.21.1, )
//        return server.getTickTimesNanos();
    }
}
