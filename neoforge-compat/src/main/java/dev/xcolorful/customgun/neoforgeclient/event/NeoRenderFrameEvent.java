package dev.xcolorful.customgun.neoforgeclient.event;

import dev.xcolorful.customgun.client.api.event.IRenderFrameEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.jetbrains.annotations.Nullable;

public class NeoRenderFrameEvent extends NeoEvent implements IRenderFrameEvent {

    protected RenderFrameEvent.Post renderTickEvent;

    public NeoRenderFrameEvent(Event event) {
        super(event);
        if (event instanceof RenderFrameEvent.Post eventIn) {
            this.renderTickEvent = eventIn;
        } else {
            throw new RuntimeException("Expected RenderFrameEvent.Post but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.RENDER_FRAME_EVENT;
    }

    @Override
    public float getPartialTick() {
        return renderTickEvent.getPartialTick().getGameTimeDeltaPartialTick(false);
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoRenderFrameEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
