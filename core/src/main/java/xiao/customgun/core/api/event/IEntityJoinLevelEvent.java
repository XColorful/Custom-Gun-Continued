package xiao.customgun.core.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface IEntityJoinLevelEvent {

    Entity getEntity();

    Level getLevel();
    boolean isLoadedFromDisk();
}
