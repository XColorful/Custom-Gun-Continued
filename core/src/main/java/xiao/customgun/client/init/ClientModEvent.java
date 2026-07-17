/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.client.init;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import xiao.customgun.client.resource._AssetsInstanceManager;
import xiao.customgun.client.resource.network.SyncDataCache;
import xiao.customgun.core.resource._AllDataManager;

public class ClientModEvent {

    private static final ClientModEvent INSTANCE = new ClientModEvent();

    public static ClientModEvent get() {
        return INSTANCE;
    }

    public void onClientLoggingIn(LocalPlayer player, Connection connection) {
        if (connection != null && !connection.isMemoryConnection()) {
            _AllDataManager.clearInstance();
            SyncDataCache.INSTANCE.clear();
            _AssetsInstanceManager.clear();
        }
    }

    public void onClientLoggingOut(LocalPlayer player, Connection connection) {
    }
}
