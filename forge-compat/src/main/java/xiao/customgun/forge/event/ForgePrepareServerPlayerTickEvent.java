package xiao.customgun.forge.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IServerPlayerTickEvent;
import xiao.customgun.forge.common.McSideHelper;

public class ForgePrepareServerPlayerTickEvent extends ForgeEvent implements IServerPlayerTickEvent {

    protected TickEvent.PlayerTickEvent.Pre playerTickEvent;

    public ForgePrepareServerPlayerTickEvent(Event event) {
        super(event);
        if (event instanceof TickEvent.PlayerTickEvent.Pre eventIn) {
            this.playerTickEvent = eventIn;
        } else {
            throw new RuntimeException("Expected PlayerTickEvent.Pre but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.PREPARE_SERVER_PLAYER_TICK_EVENT;
    }

    @Override
    public McLogicalSide getLogicalSide() {
        return McSideHelper.convert(playerTickEvent.side);
    }

    @Override
    public Player getPlayer() {
        return playerTickEvent.player;
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "ForgePrepareServerPlayerTickEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
