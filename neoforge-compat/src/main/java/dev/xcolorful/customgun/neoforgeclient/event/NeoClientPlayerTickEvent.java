package dev.xcolorful.customgun.neoforgeclient.event;

import dev.xcolorful.customgun.client.api.event.IClientPlayerTickEvent;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;

public class NeoClientPlayerTickEvent extends NeoEvent implements IClientPlayerTickEvent {

    protected PlayerTickEvent.Post playerTickEvent;

    public NeoClientPlayerTickEvent(Event event) {
        super(event);
        if (event instanceof PlayerTickEvent.Post eventIn) {
            this.playerTickEvent = eventIn;
        } else {
            throw new RuntimeException("Expected PlayerTickEvent.Post but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.CLIENT_PLAYER_TICK_EVENT;
    }

    @Override
    public McLogicalSide getLogicalSide() {
        return McLogicalSide.CLIENT;
    }

    @Override
    public Player getPlayer() {
        return playerTickEvent.getEntity();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoClientPlayerTickEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
