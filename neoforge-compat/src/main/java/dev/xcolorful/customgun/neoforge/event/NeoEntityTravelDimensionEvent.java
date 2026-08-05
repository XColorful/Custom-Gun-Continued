package dev.xcolorful.customgun.neoforge.event;

import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEntityTravelDimensionEvent;
import dev.xcolorful.customgun.core.api.minecraft.CommandLevel;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NeoEntityTravelDimensionEvent extends NeoEvent implements IEntityTravelDimensionEvent {

    protected EntityTravelToDimensionEvent entityTravelToDimensionEvent;

    public NeoEntityTravelDimensionEvent(Event event) {
        super(event);
        if (event instanceof EntityTravelToDimensionEvent eventIn) {
            this.entityTravelToDimensionEvent = eventIn;
        } else {
            throw new RuntimeException("Expected EntityTravelToDimensionEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.ENTITY_TRAVEL_DIMENSION_EVENT;
    }

    @Override
    public Entity getEntity() {
        return entityTravelToDimensionEvent.getEntity();
    }

    @Override
    public ResourceKey<Level> getDimension() {
        return entityTravelToDimensionEvent.getDimension();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        @NotNull Entity entity = this.getEntity();
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

    @Override public String getTextName() {
        return this.getEntity().getName().getString();
    }
    @Override public Component getDisplayName() {
        return this.getEntity().getDisplayName();
    }
}
