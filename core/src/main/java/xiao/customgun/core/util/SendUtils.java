/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.core.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.network.NetworkHandler;

public class SendUtils {

    public static <T extends IMessage<?>> void sendMessageToPlayer(@NotNull ServerPlayer player, T message) {
        NetworkHandler.get().sendToPlayer(player, message);
    }

    public static <T extends IMessage<?>> void sendMessageToAllPlayers(T message) {
        NetworkHandler.get().sendToAllPlayers(message);
    }

    public static <T extends IMessage<?>> void sendMessageToTrackingEntity(@NotNull Entity centerEntity, T message) {
        NetworkHandler.get().sendToTrackingEntity(centerEntity, message);
    }
    public static <T extends IMessage<?>> void sendMessageToTrackingEntityAndSelf(@NotNull Entity centerEntity, T message) {
        NetworkHandler.get().sendToTrackingEntityAndSelf(centerEntity, message);
    }

    public static <T extends IMessage<?>> void sendMessageToServer(T message) {
        NetworkHandler.get().sendToServer(message);
    }
}
