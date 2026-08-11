package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.core.api.event.IEvent;

public interface IRenderFrameEvent extends IEvent {

    /**
     * @return {@code RenderFrameEvent.getPartialTick().getGameTimeDeltaPartialTick(true)}
     */
    float getPartialTick();
}
