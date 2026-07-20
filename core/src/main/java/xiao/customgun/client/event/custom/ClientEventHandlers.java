package xiao.customgun.client.event.custom;

import xiao.customgun.client.api.event._CustomEventType;
import xiao.customgun.client.input.config.ConfigKey;
import xiao.customgun.client.input.player.InteractKey;
import xiao.customgun.client.input.player.RefitKey;
import xiao.customgun.client.input.shooter.*;
import xiao.customgun.client.resource._AllAssetsManager;
import xiao.customgun.client.sound.SoundPlayManager;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.ICustomEventRegister;
import xiao.customgun.core.event.custom.CoreEventHandlers;

public class ClientEventHandlers {

    public static void registerAll(ICustomEventRegister customEventRegister) {
        // 优先注入客户端事件类
        _CustomEventType.mixinClientEventClass();

        CoreEventHandlers.register(customEventRegister, SoundPlayManager.get(), EventType.CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, _AllAssetsManager.INSTANCE, EventType.ADD_CLIENT_RELOAD_LISTENER_EVENT, EventPriority.NORMAL, false);

        // ----input----
        CoreEventHandlers.register(customEventRegister, ConfigKey.get(), EventType.INPUT_KEY_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, InteractKey.get(), EventType.INPUT_KEY_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, InteractKey.get(), EventType.MOUSE_BUTTON_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, RefitKey.get(), EventType.INPUT_KEY_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, AimKey.get(), EventType.MOUSE_BUTTON_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, AimKey.get(), EventType.PREPARE_CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, AimKey.get(), EventType.CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, InspectKey.get(), EventType.INPUT_KEY_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, MeleeKey.get(), EventType.INPUT_KEY_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, MeleeKey.get(), EventType.MOUSE_BUTTON_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, ProneKey.get(), EventType.INPUT_KEY_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, ReloadKey.get(), EventType.INPUT_KEY_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, ReloadKey.get(), EventType.PREPARE_CLIENT_PLAYER_TICK_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, ShootKey.get(), EventType.CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, SwitchFireModeKey.get(), EventType.INPUT_KEY_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, SwitchFireModeKey.get(), EventType.MOUSE_BUTTON_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, ZoomKey.get(), EventType.INPUT_KEY_EVENT, EventPriority.NORMAL, false);
        CoreEventHandlers.register(customEventRegister, ZoomKey.get(), EventType.MOUSE_BUTTON_EVENT, EventPriority.NORMAL, false);
    }
}
