package xiao.customgun.core.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface IPlayerStartTrackingEvent {

    Player getEntity();

    Entity getTarget();
}
