package xiao.customgun.client.api.event;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import xiao.customgun.core.api.common.ILogicalSideOnly;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.event.IEvent;

public interface IClientPlayerTickEvent extends IEvent, ILogicalSideOnly {

    @Override
    McLogicalSide getLogicalSide();

    Player getPlayer();
    default LocalPlayer getLocalPlayer() {
        return (LocalPlayer) getPlayer();
    }
}
