package xiao.customgun.client.event;

import xiao.customgun.client.sound.SoundPlayManager;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.ICustomEventRegister;
import xiao.customgun.core.event.custom.CoreEventHandlers;

public class ClientEventHandlers {

    public static void registerAll(ICustomEventRegister customEventRegister) {
        CoreEventHandlers.register(customEventRegister, SoundPlayManager.get(), EventType.CLIENT_TICK_EVENT, EventPriority.NORMAL, false);
    }
}
