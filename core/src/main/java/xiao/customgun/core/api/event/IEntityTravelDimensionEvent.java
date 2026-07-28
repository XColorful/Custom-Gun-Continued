package xiao.customgun.core.api.event;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface IEntityTravelDimensionEvent {

    Entity getEntity();

    ResourceKey<Level> getDimension();
}
