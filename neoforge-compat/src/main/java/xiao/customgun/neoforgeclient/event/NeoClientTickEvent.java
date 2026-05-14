package xiao.customgun.neoforgeclient.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.TickEvent;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.event.IClientTickEvent;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.neoforge.event.NeoEvent;

public class NeoClientTickEvent extends NeoEvent implements IClientTickEvent {

    protected TickEvent.ClientTickEvent clientTickEvent;

    public NeoClientTickEvent(Event event) {
        super(event);
        if (event instanceof TickEvent.ClientTickEvent eventIn) {
            this.clientTickEvent = eventIn;
        } else {
            throw new RuntimeException("Expected ClientTickEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.CLIENT_TICK_EVENT;
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoClientTickEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}