package xiao.customgun.neoforge.minecraft;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.minecraft.ICapabilityProvider;
import xiao.customgun.core.api.minecraft.capability.IInventoryCapability;
import xiao.customgun.core.api.minecraft.capability.ISyncDataCapabilityProvider;
import xiao.customgun.core.entity.sync.SyncDataHolder;
import xiao.customgun.neoforge.init.NeoCapabilityRegistry;
import xiao.customgun.neoforge.minecraft.capability.NeoInventoryCapability;

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
