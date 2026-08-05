package dev.xcolorful.customgun.neoforgeclient.event;

import dev.xcolorful.customgun.client.api.event.IMouseButtonEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.InputEvent;
import org.jetbrains.annotations.Nullable;

public class NeoMouseButtonEvent extends NeoEvent implements IMouseButtonEvent {

    protected InputEvent.MouseButton mouseButtonEvent;

    public NeoMouseButtonEvent(Event event) {
        super(event);
        if (event instanceof InputEvent.MouseButton eventIn) {
            this.mouseButtonEvent = eventIn;
        } else {
            throw new RuntimeException("Expected MouseButton but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.MOUSE_BUTTON_EVENT;
    }

    @Override
    public MouseButtonInfo getMouseButtonInfo() {
        return mouseButtonEvent.getMouseButtonInfo();
    }

    @Override public int getButton() {
        return mouseButtonEvent.getButton();
    }

    @Override public int getAction() {
        return mouseButtonEvent.getAction();
    }

    @Override public int getModifiers() {
        return mouseButtonEvent.getModifiers();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoMouseButtonEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
