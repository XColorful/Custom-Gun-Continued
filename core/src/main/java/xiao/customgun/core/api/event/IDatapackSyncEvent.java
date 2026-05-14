package xiao.customgun.core.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public interface IDatapackSyncEvent extends IEvent {

    PlayerList getPlayerList();

    @Nullable ServerPlayer getPlayer();

    Stream<ServerPlayer> getRelevantPlayers();
}
