package xiao.customgun.core.api.block.victim;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.block.IBulletVictimBlock;

public interface IBulletVictimBlockGetter {

    static @Nullable IBulletVictimBlock fromBlock(Block block) {
        return block instanceof IBulletVictimBlock iBulletVictimBlock ? iBulletVictimBlock : null;
    }
}
