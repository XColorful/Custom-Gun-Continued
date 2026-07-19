package xiao.customgun.core.api.event.projectile;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.block.IBulletVictimBlock;

public interface IBulletVictimBlockEvent {

    @Nullable IBulletVictimBlock getIBulletVictimBlock();
    Block getBulletVictimBlock();
}
