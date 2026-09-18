package dev.xcolorful.customgun.core.api.event;

import dev.xcolorful.customgun.CustomGun;

public interface ICustomEventHandler extends IHandler<CustomEventType, ICustomEvent> {

    @Override
    default void onReceiveWrongEvent(CustomEventType customEventType) {
        CustomGun.LOGGER.warn("{} received wrong custom event type: {}", getEventHandlerName(), customEventType);
    }
}
