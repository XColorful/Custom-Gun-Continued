package dev.xcolorful.customgun.core.api.minecraft;

import dev.xcolorful.customgun.core.api.minecraft.capability.IInventoryCapability;
import dev.xcolorful.customgun.core.api.minecraft.capability.ISyncDataCapabilityProvider;
import dev.xcolorful.customgun.core.entity.sync.SyncDataHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICapabilityProvider {

    @Nullable IInventoryCapability getItemHandler(@Nullable LivingEntity livingEntity, @Nullable Direction facing);

    @NotNull ISyncDataCapabilityProvider createSyncDataCapabilityProvider();
    @Nullable SyncDataHolder getSyncDataHolder(Entity entity, Direction facing);
}
