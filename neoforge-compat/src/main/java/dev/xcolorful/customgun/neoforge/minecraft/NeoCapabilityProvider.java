package dev.xcolorful.customgun.neoforge.minecraft;

import dev.xcolorful.customgun.core.api.minecraft.ICapabilityProvider;
import dev.xcolorful.customgun.core.api.minecraft.capability.IInventoryCapability;
import dev.xcolorful.customgun.core.api.minecraft.capability.ISyncDataCapabilityProvider;
import dev.xcolorful.customgun.core.entity.sync.SyncDataHolder;
import dev.xcolorful.customgun.neoforge.init.NeoCapabilityRegistry;
import dev.xcolorful.customgun.neoforge.minecraft.capability.NeoInventoryCapability;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NeoCapabilityProvider implements ICapabilityProvider {

    @Override public @Nullable IInventoryCapability getItemHandler(LivingEntity livingEntity, @Nullable Direction facing) {
        return NeoInventoryCapability.fromLivingEntity(livingEntity, facing);
    }

    @SuppressWarnings("all")
    @Deprecated
    @Override
    public @NotNull ISyncDataCapabilityProvider createSyncDataCapabilityProvider() {
        return null;
    }
    @Override
    public @Nullable SyncDataHolder getSyncDataHolder(Entity entity, Direction facing) {
        return entity.getData(NeoCapabilityRegistry.SYNC_DATA_HOLDER);
    }
}
