package xiao.customgun.client.api.event;

import org.jetbrains.annotations.ApiStatus;

/*
预留给滚轮切换倍镜缩放 (瞄准时拦截滚轮)
 */
public interface IMouseScrollingEvent {

    @ApiStatus.AvailableSince("1.20.2") double getScrollDeltaX();
    double getScrollDeltaY();

    boolean isLeftDown();
    boolean isMiddleDown();
    boolean isRightDown();

    double getMouseX();
    double getMouseY();
}
