package dev.xcolorful.customgun.forge.common;

import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.common.McSide;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.LogicalSide;

public class McSideHelper {

    public static McSide convert(Dist dist) {
        return dist.isClient() ? McSide.CLIENT : McSide.DEDICATED_SERVER;
    }
    public static Dist convert(McSide mcSide) {
        return mcSide.isClientSide() ? Dist.CLIENT : Dist.DEDICATED_SERVER;
    }
    public static McLogicalSide convert(LogicalSide logicalSide) {
        return logicalSide.isClient() ? McLogicalSide.CLIENT : McLogicalSide.SERVER;
    }
    public static LogicalSide convert(McLogicalSide mcLogicalSide) {
        return mcLogicalSide.isClient() ? LogicalSide.CLIENT : LogicalSide.SERVER;
    }
}
