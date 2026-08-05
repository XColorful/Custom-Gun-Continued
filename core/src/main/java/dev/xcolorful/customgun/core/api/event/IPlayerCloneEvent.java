package dev.xcolorful.customgun.core.api.event;

import net.minecraft.world.entity.player.Player;

public interface IPlayerCloneEvent {

    Player getEntity();

    Player getOriginalPlayer();
    boolean isCausedByDeath();
}
