package dev.xcolorful.customgun.core.api.event.projectile;

import dev.xcolorful.customgun.core.api.block.IBulletVictimBlock;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public interface IBulletVictimBlockEvent {

    @Nullable IBulletVictimBlock getIBulletVictimBlock();
    Block getBulletVictimBlock();
}
