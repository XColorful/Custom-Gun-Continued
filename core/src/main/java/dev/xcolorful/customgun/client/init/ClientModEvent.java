/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.client.init;

import dev.xcolorful.customgun.client.entity.shooter.player._LocalPlayerHandler;
import dev.xcolorful.customgun.client.resource._AssetsInstanceManager;
import dev.xcolorful.customgun.client.resource.network.SyncDataCache;
import dev.xcolorful.customgun.core.resource._AllDataManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;

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

    public void onClientPlayerClone(LocalPlayer oldPlayer, LocalPlayer newPlayer) {
        _LocalPlayerHandler.get().onClientPlayerClone(oldPlayer, newPlayer);
    }
}
