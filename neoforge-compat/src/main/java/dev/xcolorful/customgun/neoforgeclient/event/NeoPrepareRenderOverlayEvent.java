package dev.xcolorful.customgun.neoforgeclient.event;

import dev.xcolorful.customgun.client.api.event.IPrepareRenderOverlayEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import org.jetbrains.annotations.Nullable;

public class NeoPrepareRenderOverlayEvent extends NeoEvent implements IPrepareRenderOverlayEvent {

    private final RenderGuiLayerEvent.Pre renderGuiEvent;

    public NeoPrepareRenderOverlayEvent(Event event) {
        super(event);
        if (event instanceof RenderGuiLayerEvent.Pre eventIn) {
            this.renderGuiEvent = eventIn;
        } else {
            throw new RuntimeException("Expected RenderGuiLayerEvent.Pre but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.PREPARE_RENDER_OVERLAY_EVENT;
    }

    @Override
    public GuiGraphicsExtractor getGuiGraphics() {
        return renderGuiEvent.getGuiGraphics();
    }

    @Override
    public float getPartialTick() {
        return renderGuiEvent.getPartialTick().getGameTimeDeltaPartialTick(true);
    }

    @Override
    public Identifier getRegistryLocation() {
        return renderGuiEvent.getName();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoPrepareRenderOverlayEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
