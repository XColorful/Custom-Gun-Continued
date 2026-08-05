package dev.xcolorful.customgun.core.api.event;

import dev.xcolorful.customgun.core.api.common.ILogicalSideOnly;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface IEntityJoinLevelEvent extends ILogicalSideOnly {

    Entity getEntity();

    Level getLevel();
    boolean isLoadedFromDisk();
}
