package dev.xcolorful.customgun.forgeclient.event;

import dev.xcolorful.customgun.client.api.event.IRenderOverlayEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forgeclient.compat.forge.event.RenderOverlayEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ForgeRenderOverlayEvent extends ForgeEvent implements IRenderOverlayEvent {

    private final RenderOverlayEvent renderGuiEvent;

    public ForgeRenderOverlayEvent(Event event) {
        super(event);
        if (event instanceof RenderOverlayEvent eventIn) {
            this.renderGuiEvent = eventIn;
        } else {
            throw new RuntimeException("Expected RenderOverlayEvent but received: " + event.getClass().getName());
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
        return "ForgeRenderOverlayEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
