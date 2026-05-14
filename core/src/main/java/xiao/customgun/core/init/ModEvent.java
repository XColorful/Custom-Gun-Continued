/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.core.init;

import net.minecraft.server.MinecraftServer;
import xiao.customgun.core.resource.AllDataManager;

public class ModEvent {

    private static final ModEvent INSTANCE = new ModEvent();

    public static ModEvent get() {
        return INSTANCE;
    }

    public void onServerStarting(MinecraftServer server) {
    }

    public void onServerStopping(MinecraftServer server) {
        if (AllDataManager.getCurrent() != null) AllDataManager.onServerStopped(server);
    }
}
