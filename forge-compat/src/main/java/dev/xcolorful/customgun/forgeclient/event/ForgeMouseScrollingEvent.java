package dev.xcolorful.customgun.forgeclient.event;

import dev.xcolorful.customgun.client.api.event.IMouseScrollingEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ForgeMouseScrollingEvent extends ForgeEvent implements IMouseScrollingEvent {

    protected InputEvent.MouseScrollingEvent scrollingEvent;

    public ForgeMouseScrollingEvent(Event event) {
        super(event);
        if (event instanceof InputEvent.MouseScrollingEvent eventIn) {
            this.scrollingEvent = eventIn;
        } else {
            throw new RuntimeException("Expected MouseScrollingEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.MOUSE_SCROLLING_EVENT;
    }

    @Override public double getScrollDeltaX() {
        return scrollingEvent.getDeltaX();
    }
    @Override public double getScrollDeltaY() {
        return scrollingEvent.getDeltaY();
    }

    @Override public boolean isLeftDown() {
        return scrollingEvent.isLeftDown();
    }
    @Override public boolean isMiddleDown() {
        return scrollingEvent.isMiddleDown();
    }
    @Override public boolean isRightDown() {
        return scrollingEvent.isRightDown();
    }

    @Override public double getMouseX() {
        return scrollingEvent.getMouseX();
    }
    @Override public double getMouseY() {
        return scrollingEvent.getMouseY();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "ForgeMouseScrollingEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
