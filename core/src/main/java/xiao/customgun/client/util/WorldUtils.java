package xiao.customgun.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class WorldUtils {

    public static @Nullable Entity getEntityById(ClientLevel clientLevel, int entityId) {
        return clientLevel.getEntity(entityId);
    }
    public static @Nullable LivingEntity getLivingEntityById(ClientLevel clientLevel, int entityId) {
        return getEntityById(clientLevel, entityId) instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    public static boolean isLocalPlayer(Entity entity) {
        return entity == Minecraft.getInstance().player;
    }
}
