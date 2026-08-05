package dev.xcolorful.customgun.forgeclient.event;

import dev.xcolorful.customgun.client.api.event.IInteractionMappingEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ForgeInteractionMappingEvent extends ForgeEvent implements IInteractionMappingEvent {

    protected InputEvent.InteractionKeyMappingTriggered interactionEvent;

    public ForgeInteractionMappingEvent(Event event) {
        super(event);
        if (event instanceof InputEvent.InteractionKeyMappingTriggered eventIn) {
            this.interactionEvent = eventIn;
        } else {
            throw new RuntimeException("Expected InputEvent.InteractionKeyMappingTriggered but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.INTERACTION_MAPPING_EVENT;
    }

    @Override public boolean isAttack() {
        return interactionEvent.isAttack();
    }
    @Override public boolean isUseItem() {
        return interactionEvent.isUseItem();
    }
    @Override public boolean isPickBlock() {
        return interactionEvent.isPickBlock();
    }

    @Override public KeyMapping getKeyMapping() {
        return interactionEvent.getKeyMapping();
    }

    @Override public InteractionHand getHand() {
        return interactionEvent.getHand();
    }

    @Override public boolean shouldSwingHand() {
        return interactionEvent.shouldSwingHand();
    }
    @Override public void setSwingHand(boolean value) {
        interactionEvent.setSwingHand(value);
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "ForgeInteractionMappingEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
