package dev.xcolorful.customgun.client.gui.overlay.gunhud;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.event.*;
import dev.xcolorful.customgun.core.api.event.gun.GunFireEvent;
import dev.xcolorful.customgun.core.api.event.shooter.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;

/**
 * <ul>
 *     在以下情况通知刷新枪械 HUD
 *     <li>切枪 (Shooter draw)</li>
 *     <li>射击 (Shooter shoot)</li>
 *     <li>枪械开火 (Gun fire)</li>
 * </ul>
 */
@ApiStatus.Internal
public class _GunHudTrigger implements ICustomEventHandler {
    protected static final _GunHudTrigger INSTANCE = new _GunHudTrigger();

    protected  _GunHudTrigger() {}

    protected void register(ICustomEventRegister eventRegister) {
        eventRegister.register(INSTANCE, CustomEventType.SHOOTER_DRAW_EVENT, EventPriority.LOWEST, true);
        eventRegister.register(INSTANCE, CustomEventType.SHOOTER_FIRE_EVENT, EventPriority.LOWEST, true);
        eventRegister.register(INSTANCE, CustomEventType.GUN_FIRE_EVENT, EventPriority.LOWEST, false);
        eventRegister.register(INSTANCE, CustomEventType.SHOOTER_SWITCH_FIRE_MODE_EVENT, EventPriority.LOWEST, false);
        eventRegister.register(INSTANCE, CustomEventType.SHOOTER_RELOAD_EVENT, EventPriority.LOWEST, false);
        eventRegister.register(INSTANCE, CustomEventType.SHOOTER_RELOAD_FEED_EVENT, EventPriority.LOWEST, false);
    }
    protected void unregister(ICustomEventRegister eventRegister) {
        eventRegister.unregister(INSTANCE, CustomEventType.SHOOTER_DRAW_EVENT, EventPriority.LOWEST, true);
        eventRegister.unregister(INSTANCE, CustomEventType.SHOOTER_FIRE_EVENT, EventPriority.LOWEST, true);
        eventRegister.unregister(INSTANCE, CustomEventType.GUN_FIRE_EVENT, EventPriority.LOWEST, false);
        eventRegister.unregister(INSTANCE, CustomEventType.SHOOTER_SWITCH_FIRE_MODE_EVENT, EventPriority.LOWEST, false);
        eventRegister.unregister(INSTANCE, CustomEventType.SHOOTER_RELOAD_EVENT, EventPriority.LOWEST, false);
        eventRegister.unregister(INSTANCE, CustomEventType.SHOOTER_RELOAD_FEED_EVENT, EventPriority.LOWEST, false);
    }

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override public void handleEvent(CustomEventType eventType, ICustomEvent event) {
        switch (eventType) {
            case SHOOTER_DRAW_EVENT -> {
                onLivingShooter((ShooterDrawEvent) event);
            }
            case SHOOTER_FIRE_EVENT -> {
                onLivingShooter((ShooterFireEvent) event);
            }
            case GUN_FIRE_EVENT -> {
                onGunFire((GunFireEvent) event);
            }
            case SHOOTER_SWITCH_FIRE_MODE_EVENT -> {
                onLivingShooter((ShooterSwitchFireModeEvent) event);
            }
            case SHOOTER_RELOAD_EVENT -> {
                onLivingShooter((ShooterReloadEvent) event);
            }
            case SHOOTER_RELOAD_FEED_EVENT -> {
                onLivingShooter((ShooterReloadFeedEvent) event);
            }
            default -> {
                onReceiveWrongEvent(eventType);
            }
        }
    }

    private void onLivingShooter(LivingShooterEvent event) {
        if (event.getLogicalSide().isServer()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;

        if (localPlayer == null || !localPlayer.equals(event.getLivingShooter())) return;

        DefaultGunHud.INSTANCE.setForceRefresh();
    }
    private void onGunFire(GunFireEvent event) {
        if (event.getLogicalSide().isServer()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;

        if (localPlayer == null || !localPlayer.equals(event.getLivingShooter())) return;

        DefaultGunHud.INSTANCE.setForceRefresh();
    }
}
