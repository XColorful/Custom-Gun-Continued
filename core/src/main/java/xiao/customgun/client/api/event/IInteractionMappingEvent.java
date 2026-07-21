package xiao.customgun.client.api.event;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;

public interface IInteractionMappingEvent {

    boolean isAttack();
    boolean isUseItem();
    boolean isPickBlock();

    KeyMapping getKeyMapping();

    InteractionHand getHand();

    boolean shouldSwingHand();
    void setSwingHand(boolean value);
}
