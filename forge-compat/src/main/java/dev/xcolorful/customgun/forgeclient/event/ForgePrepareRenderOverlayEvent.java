package dev.xcolorful.customgun.forgeclient.event;

import dev.xcolorful.customgun.client.api.event.IPrepareRenderOverlayEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ForgePrepareRenderOverlayEvent extends ForgeEvent implements IPrepareRenderOverlayEvent {

    private final RenderGuiOverlayEvent.Pre renderGuiEvent;

    public ForgePrepareRenderOverlayEvent(Event event) {
        super(event);
        if (event instanceof RenderGuiOverlayEvent.Pre eventIn) {
            this.renderGuiEvent = eventIn;
        } else {
            throw new RuntimeException("Expected RenderGuiOverlayEvent.Pre but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.PREPARE_RENDER_OVERLAY_EVENT;
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
        return "ForgePrepareRenderOverlayEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
