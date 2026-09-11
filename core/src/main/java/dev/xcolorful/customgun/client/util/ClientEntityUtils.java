package dev.xcolorful.customgun.client.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class ClientEntityUtils {

    public static AABB getBoundingBoxForCulling(Entity entity) {
        // [1.21.1, 1.21.4)
        return entity.getBoundingBoxForCulling();

        // [1.21.4, )
//        return entity.getBoundingBox();
    }
}
