package dev.xcolorful.customgun.forge.event;

import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IPlayerCloneEvent;
import dev.xcolorful.customgun.core.api.minecraft.CommandLevel;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgePlayerCloneEvent extends ForgeEvent implements IPlayerCloneEvent {

    protected PlayerEvent.Clone playerCloneEvent;

    public ForgePlayerCloneEvent(Event event) {
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