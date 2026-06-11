/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.core.init;

import net.minecraft.server.MinecraftServer;
import xiao.customgun.core.resource._AllDataManager;

public class ModEvent {

    private static final ModEvent INSTANCE = new ModEvent();

    public static ModEvent get() {
        return INSTANCE;
    }

    public void onServerStarting(MinecraftServer server) {
    }

    public void onServerStopping(MinecraftServer server) {
        if (_AllDataManager.getCurrent() != null) _AllDataManager.onServerStopped(server);
    }
}
