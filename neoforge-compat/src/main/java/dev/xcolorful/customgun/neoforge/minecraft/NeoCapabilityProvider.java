package dev.xcolorful.customgun.neoforge.minecraft;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.minecraft.ICapabilityProvider;
import dev.xcolorful.customgun.core.api.minecraft.capability.IInventoryCapability;
import dev.xcolorful.customgun.core.api.minecraft.capability.ISyncDataCapabilityProvider;
import dev.xcolorful.customgun.core.entity.sync.SyncDataHolder;
import dev.xcolorful.customgun.neoforge.minecraft.capability.NeoInventoryCapability;
import dev.xcolorful.customgun.neoforge.minecraft.capability.SyncDataCapabilityProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NeoCapabilityProvider implements ICapabilityProvider {
    public static final NeoCapabilityProvider INSTANCE = new NeoCapabilityProvider();

    /**
     * 手动传NeoForge类为旧实现方式，现直接调用 {@link CustomGun#getCapabilityProvider()} 即可
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(forRemoval = false)
    public NeoCapabilityProvider() {
    }

    @Override public @Nullable IInventoryCapability getItemHandler(@Nullable LivingEntity livingEntity, @Nullable Direction facing) {
        return NeoInventoryCapability.fromLivingEntity(livingEntity, facing);
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
