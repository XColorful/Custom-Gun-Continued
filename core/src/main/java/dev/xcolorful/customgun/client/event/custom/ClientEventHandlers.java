package dev.xcolorful.customgun.client.event.custom;

import dev.xcolorful.customgun.client.api.event._CustomEventType;
import dev.xcolorful.customgun.client.entity.shooter.player._LocalAnimHandler;
import dev.xcolorful.customgun.client.entity.shooter.player._LocalMessageHandler;
import dev.xcolorful.customgun.client.gui.tooltip.PojoLocationTooltip;
import dev.xcolorful.customgun.client.input.shooter.AimKey;
import dev.xcolorful.customgun.client.input.shooter.ReloadKey;
import dev.xcolorful.customgun.client.input.shooter.ShootKey;
import dev.xcolorful.customgun.client.renderer.item.gun.GunCameraHelper;
import dev.xcolorful.customgun.client.renderer.item.gun.GunRendererAddon;
import dev.xcolorful.customgun.client.renderer.victim.GunHurtBobTweak;
import dev.xcolorful.customgun.client.resource._AllAssetsManager;
import dev.xcolorful.customgun.client.sound.SoundPlayManager;
import dev.xcolorful.customgun.core.api.event.CustomEventType;
import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.ICustomEventRegister;
import dev.xcolorful.customgun.core.event.custom.CoreEventHandlers;

public class ClientEventHandlers {

    public static void registerAll(ICustomEventRegister customEventRegister) {
        // 优先注入客户端事件类
        _CustomEventType.mixinClientEventClass();

        CoreEventHandlers.register(customEventRegister, SoundPlayManager.get(), EventType.CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, _AllAssetsManager.INSTANCE, EventType.ADD_CLIENT_RELOAD_LISTENER_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, GunCameraHelper.get(), EventType.COMPUTE_CAMERA_ANGLES_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, GunCameraHelper.get(), EventType.COMPUTE_FOV_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, GunCameraHelper.get(), EventType.COMPUTE_FOV_MODIFIER_EVENT, EventPriority.LOW, false);
        CoreEventHandlers.register(customEventRegister, _LocalAnimHandler.get(), EventType.CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, _LocalAnimHandler.get(), EventType.RENDER_FRAME_EVENT, EventPriority.NORMAL, false);

        // ----input----
        CoreEventHandlers.register(customEventRegister, AimKey.get(), EventType.PREPARE_CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, AimKey.get(), EventType.CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, ReloadKey.get(), EventType.PREPARE_CLIENT_PLAYER_TICK_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, ShootKey.get(), EventType.CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, PojoLocationTooltip.get(), EventType.ITEM_TOOLTIP_EVENT, EventPriority.NORMAL, false);

        // ----custom event type----
        CoreEventHandlers.register(customEventRegister, _LocalMessageHandler.get(), CustomEventType.SWAP_ITEM_WITH_OFFHAND_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, GunHurtBobTweak.get(), CustomEventType.PROJECTILE_HIT_ENTITY_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, GunRendererAddon.get(), CustomEventType.GUN_FIRE_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, GunRendererAddon.get(), CustomEventType.ITEM_IN_HAND_BOB_VIEW_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, GunCameraHelper.Addon.get(), CustomEventType.GUN_FIRE_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, GunCameraHelper.Addon.get(), CustomEventType.BEFORE_RENDER_HAND_EVENT, EventPriority.NORMAL, false);
    }
}
