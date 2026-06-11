/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.client.init;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import xiao.customgun.core.resource._AllDataManager;

public class ClientModEvent {

    private static final ClientModEvent INSTANCE = new ClientModEvent();

    public static ClientModEvent get() {
        return INSTANCE;
    }

    public void onClientLoggingIn(LocalPlayer player, Connection connection) {
        // TODO 这里是原 client.event.CommonNetworkCacheEvent 的部分，应该在这里集中调用别的地方，而不是直接完成
        if (connection != null && !connection.isMemoryConnection()) {
            _AllDataManager.clearInstance();
        }
    }

    public void onClientLoggingOut(LocalPlayer player, Connection connection) {
    }
}
