package dev.xcolorful.customgun.forgeclient.event;

import dev.xcolorful.customgun.client.api.event.IRenderFrameEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ForgeRenderFrameEvent extends ForgeEvent implements IRenderFrameEvent {

    protected TickEvent.RenderTickEvent renderTickEvent;

    public ForgeRenderFrameEvent(Event event) {
        super(event);
        if (event instanceof TickEvent.RenderTickEvent eventIn) {
            this.renderTickEvent = eventIn;
        } else {
            throw new RuntimeException("Expected RenderTickEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.RENDER_FRAME_EVENT;
    }

    @Override
    public float getPartialTick() {
        return renderTickEvent.getTimer().getGameTimeDeltaPartialTick(false);
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "ForgeRenderFrameEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
