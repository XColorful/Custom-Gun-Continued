package xiao.customgun.client.api.event;

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


    /**
     * 1.21.10移除, 请使用 var mouseButtonInfo
     */
    record MouseButtonInfo(int button, int modifiers) {}
    /**
     * 1.21.10移除，请使用 var mouseButtonEvent
     */
    record MouseButtonEvent(double x, double y, MouseButtonInfo buttonInfo) {}
}
