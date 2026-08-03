package xiao.customgun.core.api.minecraft;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.minecraft.capability.IInventoryCapability;
import xiao.customgun.core.api.minecraft.capability.ISyncDataCapabilityProvider;
import xiao.customgun.core.entity.sync.SyncDataHolder;

public interface ICapabilityProvider {

    @Nullable IInventoryCapability getItemHandler(@Nullable LivingEntity livingEntity, @Nullable Direction facing);

    @NotNull ISyncDataCapabilityProvider createSyncDataCapabilityProvider();
    @Nullable SyncDataHolder getSyncDataHolder(Entity entity, Direction facing);
}
