package xiao.customgun.client.api.event;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.core.api.event.IEvent;

public interface IMouseButtonEvent extends IEvent {

    @ApiStatus.AvailableSince("1.21.10")
    MouseButtonInfo getMouseButtonInfo();
    default MouseButtonEvent getMouseButtonEvent() {
        return new MouseButtonEvent(0, 0, this.getMouseButtonInfo());
    }

    int getButton();

    int getAction();

    int getModifiers();
}
