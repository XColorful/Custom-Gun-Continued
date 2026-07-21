package xiao.customgun.forgeclient.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.event.IMouseButtonEvent;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.forge.event.ForgeEvent;

public class ForgeMouseButtonEvent extends ForgeEvent implements IMouseButtonEvent {

    protected InputEvent.MouseButton mouseButtonEvent;

    public ForgeMouseButtonEvent(Event event) {
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
        return "ForgeMouseButtonEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
