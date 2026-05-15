/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.sync.core;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.network.message.handshake.ServerMessageSyncedEntityDataMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyncedEntityData {
    private static SyncedEntityData INSTANCE;

    private final Set<SyncedDataKey<?, ?>> registeredDataKeys = new HashSet<>();
    private final Reference2IntMap<SyncedDataKey<?, ?>> internalIds = new Reference2IntOpenHashMap<>();
    private final Int2ReferenceMap<SyncedDataKey<?, ?>> syncedIdToKey = new Int2ReferenceOpenHashMap<>();


    private SyncedEntityData() {
    }

    public static SyncedEntityData instance() {
        if (INSTANCE == null) {
            INSTANCE = new SyncedEntityData();
        }
        return INSTANCE;
    }

    public <E extends Entity, T> void set(E entity, SyncedDataKey<?, ?> key, T value) {
        // TODO 待移植
    }

    public <E extends Entity, T> T get(E entity, SyncedDataKey<E, T> key) {
        // TODO 待移植
        return null;
    }

    public int getInternalId(SyncedDataKey<?, ?> key) {
        return this.internalIds.getInt(key);
    }

    @Nullable
    public SyncedDataKey<?, ?> getKey(int id) {
        return this.syncedIdToKey.get(id);
    }

    public Set<SyncedDataKey<?, ?>> getKeys() {
        return ImmutableSet.copyOf(this.registeredDataKeys);
    }

    public boolean updateMappings(ServerMessageSyncedEntityDataMapping message) {
        this.syncedIdToKey.clear();

        List<Pair<Identifier, Identifier>> missingKeys = new ArrayList<>();
        // TODO 待移植
        return missingKeys.isEmpty();
    }
}
