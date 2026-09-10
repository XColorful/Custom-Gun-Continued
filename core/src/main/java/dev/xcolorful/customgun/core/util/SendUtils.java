/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.core.util;

import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class SendUtils {

    public static <T extends IMessage<?>> void sendMessageToPlayer(@NotNull ServerPlayer player, T message) {
        NetworkHandler.get().sendToPlayer(player, message);
    }

    public static <T extends IMessage<?>> void sendMessageToAllPlayers(T message) {
        NetworkHandler.get().sendToAllPlayers(message);
    }

    /**
     * 将 Message 发给该实体周围的 {@link ServerPlayer}，不包括实体自己
     */
    public static <T extends IMessage<?>> void sendMessageToNearbyPlayers(@NotNull Entity centerEntity, T message) {
        NetworkHandler.get().sendToTrackingEntity(centerEntity, message);
    }
    /**
     * 将 Message 发给该实体周围的 {@link ServerPlayer}，如果实体本身是{@link ServerPlayer}，则也会发送给自己
     */
    public static <T extends IMessage<?>> void sendMessageToNearbyPlayersAndSelf(@NotNull Entity centerEntity, T message) {
        NetworkHandler.get().sendToTrackingEntityAndSelf(centerEntity, message);
    }

    public static <T extends IMessage<?>> void sendMessageToServer(T message) {
        NetworkHandler.get().sendToServer(message);
    }
}
