package dev.xcolorful.customgun.neoforgeclient.event;

import dev.xcolorful.customgun.client.api.event.IPrepareRenderFrameEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.jetbrains.annotations.Nullable;

public class NeoPrepareRenderFrameEvent extends NeoEvent implements IPrepareRenderFrameEvent {

    protected RenderFrameEvent.Pre renderTickEvent;

    public NeoPrepareRenderFrameEvent(Event event) {
        super(event);
        if (event instanceof RenderFrameEvent.Pre eventIn) {
            this.renderTickEvent = eventIn;
        } else {
            throw new RuntimeException("Expected RenderFrameEvent.Pre but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.PREPARE_RENDER_FRAME_EVENT;
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
        return "NeoPrepareRenderFrameEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
