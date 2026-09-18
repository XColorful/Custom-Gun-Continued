package dev.xcolorful.customgun.client.network.message.sync;

import dev.xcolorful.customgun.client.util.ClientWorldUtils;
import dev.xcolorful.customgun.core.entity.sync.SyncedEntityData;
import dev.xcolorful.customgun.core.network.message.sync.S2CMessageUpdateEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class _S2CMessageUpdateEntityData {

    public static void onHandle(S2CMessageUpdateEntityData message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = ClientWorldUtils.getEntityById(level, message.entityId());
        if (entity == null) {
            return;
        }
        SyncedEntityData instance = SyncedEntityData.instance();
        message.entries().forEach(entry -> instance.set(entity, entry.getKey(), entry.getValue()));
    }
}
