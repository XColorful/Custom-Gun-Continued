package xiao.customgun.neoforge.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IDatapackSyncEvent;

import java.util.stream.Stream;

public class NeoDatapackSyncEvent extends NeoEvent implements IDatapackSyncEvent {

    protected OnDatapackSyncEvent onDatapackSyncEvent;

    public NeoDatapackSyncEvent(Event event) {
        super(event);
        if (event instanceof OnDatapackSyncEvent eventIn) {
            this.onDatapackSyncEvent = eventIn;
        } else {
            throw new RuntimeException("Expected OnDatapackSyncEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.DATAPACK_SYNC_EVENT;
    }

    @Override
    public PlayerList getPlayerList() {
        return this.onDatapackSyncEvent.getPlayerList();
    }

    @Override
    public @Nullable ServerPlayer getPlayer() {
        return this.onDatapackSyncEvent.getPlayer();
    }

    @Override
    public Stream<ServerPlayer> getRelevantPlayers() {
        ServerPlayer player = this.onDatapackSyncEvent.getPlayer();
        if (player != null) {
            return Stream.of(player);
        } else {
            return this.onDatapackSyncEvent.getPlayerList().getPlayers().stream();
        }
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoDatapackSyncEvent";
    }

    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}