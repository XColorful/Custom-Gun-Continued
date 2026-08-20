package dev.xcolorful.customgun.neoforgeclient.event;

import dev.xcolorful.customgun.client.api.event.IPrepareRenderGuiEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.jetbrains.annotations.Nullable;

public class NeoPrepareRenderGuiEvent extends NeoEvent implements IPrepareRenderGuiEvent {

    private final RenderGuiEvent.Pre renderGuiEvent;

    public NeoPrepareRenderGuiEvent(Event event) {
        super(event);
        if (event instanceof RenderGuiEvent.Pre eventIn) {
            this.renderGuiEvent = eventIn;
        } else {
            throw new RuntimeException("Expected RenderGuiEvent.Pre but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.PREPARE_RENDER_GUI_EVENT;
    }

    @Override
    public GuiGraphics getGuiGraphics() {
        return renderGuiEvent.getGuiGraphics();
    }

    @Override
    public float getPartialTick() {
        return renderGuiEvent.getPartialTick().getGameTimeDeltaPartialTick(true);
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoPrepareRenderGuiEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
