package dev.xcolorful.customgun.neoforgeclient.event;

import dev.xcolorful.customgun.client.api.event.IRenderFrameEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.TickEvent;
import org.jetbrains.annotations.Nullable;

public class NeoPrepareRenderFrameEvent extends NeoEvent implements IRenderFrameEvent {

    protected TickEvent.RenderTickEvent renderTickEvent;

    public NeoPrepareRenderFrameEvent(Event event) {
        super(event);
        if (event instanceof TickEvent.RenderTickEvent eventIn) {
            this.renderTickEvent = eventIn;
        } else {
            throw new RuntimeException("Expected RenderTickEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.PREPARE_RENDER_FRAME_EVENT;
    }

    @Override
    public float getPartialTick() {
        return renderTickEvent.renderTickTime;
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoPrepareRenderFrameEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
