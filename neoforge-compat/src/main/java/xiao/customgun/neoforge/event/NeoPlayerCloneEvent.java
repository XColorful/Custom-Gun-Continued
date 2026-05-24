package xiao.customgun.neoforge.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IPlayerCloneEvent;
import xiao.customgun.core.api.minecraft.CommandLevel;

public class NeoPlayerCloneEvent extends NeoEvent implements IPlayerCloneEvent {

    protected PlayerEvent.Clone playerCloneEvent;

    public NeoPlayerCloneEvent(Event event) {
        super(event);
        if (event instanceof PlayerEvent.Clone eventIn) {
            this.playerCloneEvent = eventIn;
        } else {
            throw new RuntimeException("Expected PlayerEvent.Clone but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.PLAYER_CLONE_EVENT;
    }

    @Override
    public Player getEntity() {
        return playerCloneEvent.getEntity();
    }

    @Override
    public Player getOriginalPlayer() {
        return playerCloneEvent.getOriginal();
    }

    @Override
    public boolean isCausedByDeath() {
        return playerCloneEvent.isWasDeath();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        @NotNull LivingEntity entity = this.getEntity();
        Level level = entity.level();
        if (level != null && level.isClientSide()) return null;
        return new CommandSourceStack(
                source != null ? source : CommandSource.NULL,
                entity.position(),
                entity.getRotationVector(),
                (ServerLevel) level,
                CommandLevel.permission(4),
                this.getTextName(),
                this.getDisplayName(),
                level.getServer(),
                entity
        );
    }

    @Override
    public String getTextName() {
        return this.getEntity().getName().getString();
    }

    @Override
    public Component getDisplayName() {
        return this.getEntity().getDisplayName();
    }
}