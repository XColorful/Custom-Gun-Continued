package xiao.customgun.neoforgeclient.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.event.IMouseScrollingEvent;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.neoforge.event.NeoEvent;

public class NeoMouseScrollingEvent extends NeoEvent implements IMouseScrollingEvent {

    protected InputEvent.MouseScrollingEvent scrollingEvent;

    public NeoMouseScrollingEvent(Event event) {
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
        return scrollingEvent.getScrollDeltaX();
    }
    @Override public double getScrollDeltaY() {
        return scrollingEvent.getScrollDeltaY();
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
        return "NeoMouseScrollingEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
