package xiao.customgun.core.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xiao.customgun.core.api.common.ILogicalSideOnly;
import xiao.customgun.core.api.common.McLogicalSide;

public interface IServerPlayerTickEvent extends IEvent, ILogicalSideOnly {

    @Override
    McLogicalSide getLogicalSide();

    Player getPlayer();
    default ServerPlayer getServerPlayer() {
        return (ServerPlayer) getPlayer();
    }
}
