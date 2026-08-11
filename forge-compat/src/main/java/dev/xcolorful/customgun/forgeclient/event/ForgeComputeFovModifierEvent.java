package dev.xcolorful.customgun.forgeclient.event;

import dev.xcolorful.customgun.client.api.event.IComputeFovModifierEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ForgeComputeFovModifierEvent extends ForgeEvent implements IComputeFovModifierEvent {

    protected ComputeFovModifierEvent computeFovModifierEvent;

    public ForgeComputeFovModifierEvent(Event event) {
        super(event);
        if (event instanceof ComputeFovModifierEvent eventIn) {
            this.computeFovModifierEvent = eventIn;
        } else {
            throw new RuntimeException("Expected ComputeFovModifierEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.COMPUTE_CAMERA_ANGLES_EVENT;
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
        return "ForgeComputeFovModifierEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
