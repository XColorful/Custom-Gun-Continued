package dev.xcolorful.customgun.core.api.block.victim;

import dev.xcolorful.customgun.core.api.block.IBulletVictimBlock;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public interface IBulletVictimBlockGetter {

    static @Nullable IBulletVictimBlock fromBlock(Block block) {
        return block instanceof IBulletVictimBlock iBulletVictimBlock ? iBulletVictimBlock : null;
    }
}
