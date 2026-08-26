package dev.xcolorful.customgun.core.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface IPlayerStartTrackingEvent extends IEvent {

    Player getEntity();

    Entity getTarget();
}
