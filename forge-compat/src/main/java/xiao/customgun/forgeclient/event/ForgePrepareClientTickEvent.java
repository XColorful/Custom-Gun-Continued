package xiao.customgun.forgeclient.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.event.IPrepareClientTickEvent;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.forge.event.ForgeEvent;

public class ForgePrepareClientTickEvent extends ForgeEvent implements IPrepareClientTickEvent {

    protected TickEvent.ClientTickEvent.Pre clientTickEvent;

    public ForgePrepareClientTickEvent(Event event) {
        super(event);
        if (event instanceof TickEvent.ClientTickEvent.Pre eventIn) {
            this.clientTickEvent = eventIn;
        } else {
            throw new RuntimeException("Expected ClientTickEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.PREPARE_CLIENT_TICK_EVENT;
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "ForgePrepareClientTickEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
