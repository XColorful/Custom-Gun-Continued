package xiao.customgun.client.api.event;

import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.core.api.event.IEvent;

/*
预留给滚轮切换倍镜缩放 (瞄准时拦截滚轮)
 */
public interface IMouseScrollingEvent extends IEvent {

    @ApiStatus.AvailableSince("1.20.2") double getScrollDeltaX();
    double getScrollDeltaY();

    boolean isLeftDown();
    boolean isMiddleDown();
    boolean isRightDown();

    double getMouseX();
    double getMouseY();
}
