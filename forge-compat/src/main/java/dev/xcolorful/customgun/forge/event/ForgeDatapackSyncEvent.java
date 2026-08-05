package dev.xcolorful.customgun.forge.event;

import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IDatapackSyncEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class ForgeDatapackSyncEvent extends ForgeEvent implements IDatapackSyncEvent {

    protected OnDatapackSyncEvent onDatapackSyncEvent;

    public ForgeDatapackSyncEvent(Event event) {
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
        if (player != null) return Stream.of(player);
        else return this.onDatapackSyncEvent.getPlayerList().getPlayers().stream();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "ForgeDatapackSyncEvent";
    }

    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}