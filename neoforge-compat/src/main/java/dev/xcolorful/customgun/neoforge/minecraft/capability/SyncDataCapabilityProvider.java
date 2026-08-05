/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.neoforge.minecraft.capability;

import dev.xcolorful.customgun.core.api.minecraft.capability.ISyncDataCapabilityProvider;
import dev.xcolorful.customgun.core.entity.sync.*;
import dev.xcolorful.customgun.neoforge.CustomGunNeoforge;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.capabilities.CapabilityToken;
import net.neoforged.neoforge.common.capabilities.ICapabilitySerializable;
import net.neoforged.neoforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 原 DataHolderCapabilityProvider
 */
@Deprecated(forRemoval = true, since = "1.20.4")
public class SyncDataCapabilityProvider implements ISyncDataCapabilityProvider, ICapabilitySerializable<ListTag> {
    public static final Capability<SyncDataHolder> CAPABILITY = CapabilityManager.get(new CapabilityToken<SyncDataHolder>() {});

    private final SyncDataHolder holder = new SyncDataHolder();
    private final LazyOptional<SyncDataHolder> optional = LazyOptional.of(() -> this.holder);

    public SyncDataCapabilityProvider() {}

    @Override
    public void invalidate() {
        this.optional.invalidate();
    }

    @Override
    public ListTag serializeNBT() {
        ListTag list = new ListTag();
        this.holder.syncData.forEach((key, entry) -> {
            if (key.save()) {
                CompoundTag keyTag = new CompoundTag();
                keyTag.putString("ClassKey", key.classKey().id().toString());
                keyTag.putString("DataKey", key.id().toString());
                keyTag.put("Value", entry.writeValue());
                list.add(keyTag);
            }
        });
        return list;
    }

    @Override
    public void deserializeNBT(ListTag listTag) {
        this.holder.syncData.clear();
        listTag.forEach(entryTag -> {
            CompoundTag keyTag = (CompoundTag) entryTag;
            var classKey = CustomGunNeoforge.mcRegistry.createResourceLocation(keyTag.getString("ClassKey"));
            var dataKey = CustomGunNeoforge.mcRegistry.createResourceLocation(keyTag.getString("DataKey"));
            Tag value = keyTag.get("Value");
            SyncedClassKey<?> syncedClassKey = SyncedEntityData.instance().getClassKey(classKey);
            if (syncedClassKey == null) {
                return;
            }
            SyncedDataKey<?, ?> syncedDataKey = SyncedEntityData.instance().getKey(syncedClassKey, dataKey);
            if (syncedDataKey == null || !syncedDataKey.save()) {
                return;
            }
            DataEntry<?, ?> entry = new DataEntry<>(syncedDataKey);
            entry.readValue(value);
            this.holder.syncData.put(syncedDataKey, entry);
        });
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CAPABILITY.orEmpty(cap, this.optional);
    }
}
