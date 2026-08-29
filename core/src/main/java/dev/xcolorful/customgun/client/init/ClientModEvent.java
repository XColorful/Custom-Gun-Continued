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
    private ClientModEvent() {}

    private boolean LOGGED_ONCE = false;
    public void onClientLoggingIn(LocalPlayer player, Connection connection) {
        LOGGED_ONCE = true;
        if (connection != null && !connection.isMemoryConnection()) {
            _AllDataManager.clearInstance();
            SyncDataCache.INSTANCE.clear();
            _AssetsInstanceManager.clear();
        }
    }

    /**
     * <ul>
     *     1.21.1+ 资源包加载时无法访问注册表
     *     <li>可用于延迟首次解析，避免抛异常导致资源包加载列表被重置</li>
     *     <li>1.21.1forge 启动时资源包加载失败还会导致指令全都没注册</li>
     * </ul>
     * @return 是否进过存档
     */
    public boolean hasLoggedOnce() {
        return LOGGED_ONCE;
    }

    public void onClientLoggingOut(LocalPlayer player, Connection connection) {
    }

    public void onClientPlayerClone(LocalPlayer oldPlayer, LocalPlayer newPlayer) {
        _LocalPlayerHandler.get().onClientPlayerClone(oldPlayer, newPlayer);
    }
}
