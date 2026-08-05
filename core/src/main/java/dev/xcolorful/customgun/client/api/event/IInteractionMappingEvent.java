package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;

public interface IInteractionMappingEvent extends IEvent {

    boolean isAttack();
    boolean isUseItem();
    boolean isPickBlock();

    KeyMapping getKeyMapping();

    InteractionHand getHand();

    boolean shouldSwingHand();
    void setSwingHand(boolean value);
}
