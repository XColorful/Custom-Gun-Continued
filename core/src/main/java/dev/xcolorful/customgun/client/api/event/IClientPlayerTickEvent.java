package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.core.api.common.ILogicalSideOnly;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public interface IClientPlayerTickEvent extends IEvent, ILogicalSideOnly {

    @Override
    McLogicalSide getLogicalSide();

    Player getPlayer();
    default LocalPlayer getLocalPlayer() {
        return (LocalPlayer) getPlayer();
    }
}
