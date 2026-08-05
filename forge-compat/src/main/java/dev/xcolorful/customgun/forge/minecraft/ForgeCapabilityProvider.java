package dev.xcolorful.customgun.forge.minecraft;

import dev.xcolorful.customgun.core.api.minecraft.ICapabilityProvider;
import dev.xcolorful.customgun.core.api.minecraft.capability.IInventoryCapability;
import dev.xcolorful.customgun.core.api.minecraft.capability.ISyncDataCapabilityProvider;
import dev.xcolorful.customgun.core.entity.sync.SyncDataHolder;
import dev.xcolorful.customgun.forge.minecraft.capability.ForgeInventoryCapability;
import dev.xcolorful.customgun.forge.minecraft.capability.SyncDataCapabilityProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgeCapabilityProvider implements ICapabilityProvider {

    @Override public @Nullable IInventoryCapability getItemHandler(@Nullable LivingEntity livingEntity, @Nullable Direction facing) {
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
