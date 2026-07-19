package xiao.customgun.client.event.custom;

import xiao.customgun.client.api.event._CustomEventType;
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
    }
}
