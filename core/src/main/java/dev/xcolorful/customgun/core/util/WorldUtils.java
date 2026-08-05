package dev.xcolorful.customgun.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

public class WorldUtils {

    public static @Nullable Entity getEntityById(ServerLevel serverLevel, int entityId) {
        return serverLevel.getEntity(entityId);
    }
    public static @Nullable LivingEntity getLivingEntityById(ServerLevel serverLevel, int entityId) {
        return serverLevel.getEntity(entityId) instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    public static ChunkPos chunkPos(BlockPos blockPos) {
        return new ChunkPos(blockPos);
    }
}
