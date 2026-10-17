package dev.xcolorful.customgun.client.entity.shooter.player;

import dev.xcolorful.customgun.client.api.entity.LocalShooterProperty;
import dev.xcolorful.customgun.client.api.event.ClientDelayedEvent;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.entity.sync.SyncDataHolder;
import dev.xcolorful.customgun.core.entity.sync.SyncedEntityData;
import dev.xcolorful.customgun.core.util.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.HashMap;
import java.util.function.Consumer;

public class _LocalPlayerHandler {
    private static class _LocalPlayerHandlerHolder {
        private static final _LocalPlayerHandler INSTANCE = new _LocalPlayerHandler();
    }
    public static _LocalPlayerHandler get() {
        return _LocalPlayerHandlerHolder.INSTANCE;
    }
    protected _LocalPlayerHandler() {}

    private long currentTimeMillis = 0;
    public long getCurrentTimeMillis() {
        return this.currentTimeMillis;
    }

    public void onClientPlayerClone(LocalPlayer oldPlayer, LocalPlayer newPlayer) {
        final long currentTimeMillis = System.currentTimeMillis();
        this.currentTimeMillis = currentTimeMillis;

        /*
        服务端只在实体重新加入世界时全量同步 SyncedEntityData
        如果只有客户端重建了玩家实体（例如换皮肤模组/插件发送的 respawn 包），服务端不会重新同步，新实体会退回默认值，导致冷却/换弹/瞄准等同步状态丢失
        因此把旧实体的数据复制过来；死亡重生、跨维度时服务端随后仍会全量同步覆盖
         */
        {
            EntityUtils.Forge.reviveCaps(oldPlayer);

            SyncDataHolder oldHolder = SyncedEntityData.instance().getSyncDataHolder(oldPlayer);
            SyncDataHolder newHolder = SyncedEntityData.instance().getSyncDataHolder(newPlayer);
            if (oldHolder != null && newHolder != null) {
                newHolder.syncData = new HashMap<>(oldHolder.syncData);
            }
            if (false) LocalShooterProperty.clientBaseTimestamp = LocalShooterProperty.clientBaseTimestamp; // 如果改成实例字段，可以使此处强制编译不通过

            EntityUtils.Forge.invalidateCaps(oldPlayer);
        }

        Consumer<LocalPlayer> delayedTask = localPlayer -> {
            LocalPlayer currentPlayer = Minecraft.getInstance().player;
            if (currentPlayer != null && currentPlayer == localPlayer) {
                if (currentTimeMillis == _LocalPlayerHandler.get().getCurrentTimeMillis()) {
                    ILivingShooterGetter.cgc$fromLivingEntity(localPlayer).cgc$initLivingShooter();
                }
            }
        };
        // 事件触发时玩家背包未同步，延迟10tick再刷新缓存
        new ClientDelayedEvent<>(delayedTask, newPlayer, 10, "_LocalPlayerHandler onClientPlayerClone");
    }
}
