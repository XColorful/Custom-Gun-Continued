package xiao.customgun.neoforgeclient.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.event.IClientPlayerTickEvent;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.neoforge.common.McSideHelper;
import xiao.customgun.neoforge.event.NeoEvent;

public class NeoClientPlayerTickEvent extends NeoEvent implements IClientPlayerTickEvent {

    protected TickEvent.PlayerTickEvent playerTickEvent;

    public NeoClientPlayerTickEvent(Event event) {
        super(event);
        if (event instanceof TickEvent.PlayerTickEvent eventIn) {
            this.playerTickEvent = eventIn;
        } else {
            throw new RuntimeException("Expected PlayerTickEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.CLIENT_PLAYER_TICK_EVENT;
    }

    @Override
    public McLogicalSide getLogicalSide() {
        return McSideHelper.convert(playerTickEvent.side);
    }

    @Override
    public Player getPlayer() {
        return playerTickEvent.player;
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
