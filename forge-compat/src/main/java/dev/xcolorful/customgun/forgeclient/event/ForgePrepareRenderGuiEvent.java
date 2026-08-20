package dev.xcolorful.customgun.forgeclient.event;

import dev.xcolorful.customgun.client.api.event.IPrepareRenderGuiEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forgeclient.compat.forge.event.PrepareRenderGuiEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ForgePrepareRenderGuiEvent extends ForgeEvent implements IPrepareRenderGuiEvent {

    private final PrepareRenderGuiEvent renderGuiEvent;

    public ForgePrepareRenderGuiEvent(Event event) {
        super(event);
        if (event instanceof PrepareRenderGuiEvent eventIn) {
            this.renderGuiEvent = eventIn;
        } else {
            throw new RuntimeException("Expected PrepareRenderGuiEvent but received: " + event.getClass().getName());
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
        return "ForgePrepareRenderGuiEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
