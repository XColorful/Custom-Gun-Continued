package xiao.customgun.forge.minecraft;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.minecraft.ICapabilityProvider;
import xiao.customgun.core.api.minecraft.capability.IInventoryCapability;
import xiao.customgun.core.api.minecraft.capability.ISyncDataCapabilityProvider;
import xiao.customgun.core.entity.sync.SyncDataHolder;
import xiao.customgun.forge.minecraft.capability.ForgeInventoryCapability;
import xiao.customgun.forge.minecraft.capability.SyncDataCapabilityProvider;

public class ForgeCapabilityProvider implements ICapabilityProvider {

    @Override public @Nullable IInventoryCapability getItemHandler(LivingEntity livingEntity, @Nullable Direction facing) {
        return ForgeInventoryCapability.fromLivingEntity(livingEntity, facing);
    }

    @Override
    public @NotNull ISyncDataCapabilityProvider createSyncDataCapabilityProvider() {
        return new SyncDataCapabilityProvider();
    }
    @Override
    public @Nullable SyncDataHolder getSyncDataHolder(Entity entity, Direction facing) {
        return entity.getCapability(SyncDataCapabilityProvider.CAPABILITY, facing).orElse(null);
    }
}
