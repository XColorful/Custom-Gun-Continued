package xiao.customgun.forge.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEntityJoinLevelEvent;
import xiao.customgun.core.api.minecraft.CommandLevel;

public class ForgeEntityJoinLevelEvent extends ForgeEvent implements IEntityJoinLevelEvent {

    protected EntityJoinLevelEvent entityJoinLevelEvent;

    public ForgeEntityJoinLevelEvent(Event event) {
        super(event);
        if (event instanceof EntityJoinLevelEvent eventIn) {
            this.entityJoinLevelEvent = eventIn;
        } else {
            throw new RuntimeException("Expected EntityJoinLevelEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.ENTITY_JOIN_LEVEL_EVENT;
    }

    @Override
    public Entity getEntity() {
        return entityJoinLevelEvent.getEntity();
    }

    @Override
    public Level getLevel() {
        return entityJoinLevelEvent.getLevel();
    }

    @Override
    public boolean isLoadedFromDisk() {
        return entityJoinLevelEvent.loadedFromDisk();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        @NotNull Entity entity = this.getEntity();
        Level level = this.getLevel();
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

    @Override public String getTextName() {
        return this.getEntity().getName().getString();
    }
    @Override public Component getDisplayName() {
        return this.getEntity().getDisplayName();
    }
}