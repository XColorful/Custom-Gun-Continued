package dev.xcolorful.customgun.neoforgeclient.event;

import dev.xcolorful.customgun.client.api.event.IRenderOverlayEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.RenderGuiOverlayEvent;
import org.jetbrains.annotations.Nullable;

public class NeoRenderOverlayEvent extends NeoEvent implements IRenderOverlayEvent {

    private final RenderGuiOverlayEvent.Post renderGuiEvent;

    public NeoRenderOverlayEvent(Event event) {
        super(event);
        if (event instanceof RenderGuiOverlayEvent.Post eventIn) {
            this.renderGuiEvent = eventIn;
        } else {
            throw new RuntimeException("Expected RenderGuiOverlayEvent.Post but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.RENDER_OVERLAY_EVENT;
    }

    @Override
    public GuiGraphics getGuiGraphics() {
        return renderGuiEvent.getGuiGraphics();
    }

    @Override
    public float getPartialTick() {
        return renderGuiEvent.getPartialTick();
    }

    @Override
    public ResourceLocation getRegistryLocation() {
        return renderGuiEvent.getOverlay().id();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoRenderOverlayEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
