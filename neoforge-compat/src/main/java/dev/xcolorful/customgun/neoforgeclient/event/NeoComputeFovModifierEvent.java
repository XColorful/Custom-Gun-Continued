package dev.xcolorful.customgun.neoforgeclient.event;

import dev.xcolorful.customgun.client.api.event.IComputeFovModifierEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import org.jetbrains.annotations.Nullable;

public class NeoComputeFovModifierEvent extends NeoEvent implements IComputeFovModifierEvent {

    protected ComputeFovModifierEvent computeFovModifierEvent;

    public NeoComputeFovModifierEvent(Event event) {
        super(event);
        if (event instanceof ComputeFovModifierEvent eventIn) {
            this.computeFovModifierEvent = eventIn;
        } else {
            throw new RuntimeException("Expected ComputeFovModifierEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.COMPUTE_FOV_MODIFIER_EVENT;
    }

    @Override public Player getPlayer() {
        return computeFovModifierEvent.getPlayer();
    }

    @Override public float getFovModifier() {
        return computeFovModifierEvent.getFovModifier();
    }

    @Override public float getFovScale() {
        return Minecraft.getInstance().options.fovEffectScale().get().floatValue();
    }

    @Override public float getNewFovModifier() {
        return computeFovModifierEvent.getNewFovModifier();
    }

    @Override public void setNewFovModifier(float newFovModifier) {
        computeFovModifierEvent.setNewFovModifier(newFovModifier);
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoComputeFovModifierEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
