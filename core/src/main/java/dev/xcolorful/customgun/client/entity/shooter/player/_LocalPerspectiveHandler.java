package dev.xcolorful.customgun.client.entity.shooter.player;

import dev.xcolorful.customgun.client.compat.shouldersurfing.ShoulderSurfingCompat;
import dev.xcolorful.customgun.client.config.KeyConfig;
import dev.xcolorful.customgun.core.api.event.CustomEventType;
import dev.xcolorful.customgun.core.api.event.ICustomEvent;
import dev.xcolorful.customgun.core.api.event.ICustomEventHandler;
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
    }
    private void _onCancelAim(Minecraft mc) {
        if (this.lastPerspectiveBeforeAiming == null) return;

        if (KeyConfig.REVERT_AIM_PERSPECTIVE.get()) {
            /**
             * 会回到越肩视角，不需要单独 {@link ShoulderSurfingCompat}
             */
            mc.options.setCameraType(this.lastPerspectiveBeforeAiming);
        }

        this.lastPerspectiveBeforeAiming = null;
    }
}
