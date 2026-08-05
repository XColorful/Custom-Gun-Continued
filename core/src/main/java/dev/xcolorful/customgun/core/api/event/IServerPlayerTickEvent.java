package dev.xcolorful.customgun.core.api.event;

import dev.xcolorful.customgun.core.api.common.ILogicalSideOnly;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface IServerPlayerTickEvent extends IEvent, ILogicalSideOnly {

    @Override
    McLogicalSide getLogicalSide();

    Player getPlayer();
    default ServerPlayer getServerPlayer() {
        return (ServerPlayer) getPlayer();
    }
}
