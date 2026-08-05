package dev.xcolorful.customgun.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Vec3Utils {

    /**
     * 格式: "x,y,z"
     */
    public static @Nullable Vec3 fromString(@Nullable String value) {
        if (value == null || value.isEmpty()) return null;

        try {
            String[] split = value.split(",");
            if (split.length != 3) return null;

            double x = Double.parseDouble(split[0]);
            double y = Double.parseDouble(split[1]);
            double z = Double.parseDouble(split[2]);

            return new Vec3(x, y, z);
        } catch (Exception e) {
            return null;
        }
    }
    public static @Nullable String toString(@Nullable Vec3 value) {
        if (value == null) return null;
        return value.x + "," + value.y + "," + value.z;
    }

    public static Vec3 getCenter(BlockPos blockPos) {
        return Vec3.atCenterOf(blockPos);
    }
}
