package dev.xcolorful.customgun.forgeclient.event;

import dev.xcolorful.customgun.client.api.event.IPrepareRenderOverlayEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forgeclient.compat.forge.event.PrepareRenderOverlayEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ForgePrepareRenderOverlayEvent extends ForgeEvent implements IPrepareRenderOverlayEvent {

    private final PrepareRenderOverlayEvent renderGuiEvent;

    public ForgePrepareRenderOverlayEvent(Event event) {
        super(event);
        if (event instanceof PrepareRenderOverlayEvent eventIn) {
            this.renderGuiEvent = eventIn;
        } else {
            throw new RuntimeException("Expected PrepareRenderOverlayEvent but received: " + event.getClass().getName());
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
        return renderGuiEvent.getPartialTick().getGameTimeDeltaPartialTick(true);
    }

    @Override
    public ResourceLocation getRegistryLocation() {
        return renderGuiEvent.getRegistryLocation();
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
