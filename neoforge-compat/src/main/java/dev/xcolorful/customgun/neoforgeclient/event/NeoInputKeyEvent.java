package dev.xcolorful.customgun.neoforgeclient.event;

import dev.xcolorful.customgun.client.api.event.IInputKeyEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.InputEvent;
import org.jetbrains.annotations.Nullable;

public class NeoInputKeyEvent extends NeoEvent implements IInputKeyEvent {

    protected InputEvent.Key inputKeyEvent;

    public NeoInputKeyEvent(Event event) {
        super(event);
        if (event instanceof InputEvent.Key eventIn) {
            this.inputKeyEvent = eventIn;
        } else {
            throw new RuntimeException("Expected InputEvent.Key but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.INPUT_KEY_EVENT;
    }

    @Override
    public KeyEvent getKeyEvent() {
        return inputKeyEvent.getKeyEvent();
    }

    @Override public int getKey() {
        return inputKeyEvent.getKey();
    }

    @Override public int getScanCode() {
        return inputKeyEvent.getScanCode();
    }

    @Override public int getAction() {
        return inputKeyEvent.getAction();
    }

    @Override public int getModifiers() {
        return inputKeyEvent.getModifiers();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoInputKeyEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
