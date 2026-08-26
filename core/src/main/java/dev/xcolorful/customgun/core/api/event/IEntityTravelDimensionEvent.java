package dev.xcolorful.customgun.core.api.event;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface IEntityTravelDimensionEvent extends IEvent {

    Entity getEntity();

    ResourceKey<Level> getDimension();
}
