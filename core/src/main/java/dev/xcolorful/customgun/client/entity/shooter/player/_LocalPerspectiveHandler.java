package dev.xcolorful.customgun.client.entity.shooter.player;

import dev.xcolorful.customgun.client.api.event.IPrepareClientTickEvent;
import dev.xcolorful.customgun.client.compat.shouldersurfing.ShoulderSurfingCompat;
import dev.xcolorful.customgun.client.config.KeyConfig;
import dev.xcolorful.customgun.core.api.event.*;
import dev.xcolorful.customgun.core.api.event.shooter.ShooterSwitchAimEvent;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

public class _LocalPerspectiveHandler implements ICustomEventHandler {
    private static class _LocalPerspectiveHandlerHolder {
        private static final _LocalPerspectiveHandler INSTANCE = new _LocalPerspectiveHandler();
    }
    public static _LocalPerspectiveHandler get() {
        return _LocalPerspectiveHandlerHolder.INSTANCE;
    }
    protected _LocalPerspectiveHandler() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(CustomEventType eventType, ICustomEvent event) {
        if (eventType == CustomEventType.SHOOTER_SWITCH_AIM_EVENT) {
            onShooterSwitchAim((ShooterSwitchAimEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    private @Nullable CameraType lastPerspectiveBeforeAiming = null;
    /**
     * -1 表示未处于取消瞄准后的状态; 0表示开始计时
     */
    private int ticksAfterCancelAim = -1;
    /**
     * 延迟 2 ticks (50ms ~ 100ms)
     * <ul>
     *     <li>避免(高倍镜)取消瞄准时看远景而无目镜环</li>
     *     <li>顺便去掉一点从奔跑切到开镜时额外的抬枪过渡</li>
     * </ul>
     */
    private final int revertDelay = 2;

    private void onShooterSwitchAim(ShooterSwitchAimEvent event) {
        if (event.getLogicalSide().isServer()) return;
        if (event.isCanceled()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;
        if (localPlayer == null) return;

        if (event.getIsAiming()) {
            // 切换到瞄准状态
            this._onAim(mc);
        } else {
            // 取消瞄准状态
            this._onCancelAim(mc);
        }
    }
    private void _onAim(Minecraft mc) {
        this.lastPerspectiveBeforeAiming = mc.options.getCameraType();

        if (KeyConfig.AIM_SWITCH_TO_FIRST_PERSON.get()) {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }

        this.ticksAfterCancelAim = -1;
    }
    private void _onCancelAim(Minecraft mc) {
        if (this.lastPerspectiveBeforeAiming == null) return;

        this.ticksAfterCancelAim = 0;
    }

    private void revertPerspective() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;
        if (localPlayer == null) return;

        if (this.lastPerspectiveBeforeAiming == null) return;

        if (KeyConfig.REVERT_AIM_PERSPECTIVE.get()) {
            /**
             * 会回到越肩视角，不需要单独 {@link ShoulderSurfingCompat}
             */
            mc.options.setCameraType(this.lastPerspectiveBeforeAiming);
        }

        this.lastPerspectiveBeforeAiming = null;
    }

    private void onPrepareClientTick(IPrepareClientTickEvent event) {
        if (this.ticksAfterCancelAim < 0) return;

        if (++this.ticksAfterCancelAim < this.revertDelay) return;

        this.ticksAfterCancelAim = -1;
        this.revertPerspective();
    }

    public static class Helper implements IEventHandler {
        private static class HelperHolder {
            private static final Helper INSTANCE = new Helper();
        }
        public static Helper get() {
            return HelperHolder.INSTANCE;
        }
        protected Helper() {}
        @Override public String getEventHandlerName() {
            return this.getClass().getName();
        }
        @Override
        public void handleEvent(EventType eventType, IEvent event) {
            if (eventType == EventType.PREPARE_CLIENT_TICK_EVENT) {
                _LocalPerspectiveHandler.get().onPrepareClientTick((IPrepareClientTickEvent) event);
            } else {
                onReceiveWrongEvent(eventType);
            }
        }
    }
}
