package dev.xcolorful.customgun.forgeclient.event;

import dev.xcolorful.customgun.client.api.event.IInputKeyEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ForgeInputKeyEvent extends ForgeEvent implements IInputKeyEvent {

    protected InputEvent.Key inputKeyEvent;

    public ForgeInputKeyEvent(Event event) {
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
        return new KeyEvent(inputKeyEvent.getKey(), inputKeyEvent.getScanCode(), inputKeyEvent.getModifiers());
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
        return "ForgeInputKeyEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
