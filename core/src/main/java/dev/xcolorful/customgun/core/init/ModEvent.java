/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.core.init;

import dev.xcolorful.customgun.core.resource._AllDataManager;
import net.minecraft.server.MinecraftServer;

public class ModEvent {

    private static final ModEvent INSTANCE = new ModEvent();
    public static ModEvent get() {
        return INSTANCE;
    }
    private ModEvent() {}

    public void onServerStarting(MinecraftServer server) {
    }

    public void onServerStopping(MinecraftServer server) {
        if (_AllDataManager.getCurrent() != null) _AllDataManager.onServerStopped(server);
    }
}
