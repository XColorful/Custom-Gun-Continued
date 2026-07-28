package xiao.customgun.core.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import xiao.customgun.core.api.common.ILogicalSideOnly;

public interface IEntityJoinLevelEvent extends ILogicalSideOnly {

    Entity getEntity();

    Level getLevel();
    boolean isLoadedFromDisk();
}
