/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.init;

import xiao.customgun.core.entity.sync.core.SyncDataHolder;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class CapabilityRegistry {

    private static final CapabilityRegistry INSTANCE = new CapabilityRegistry();
    public static CapabilityRegistry get() {
        return INSTANCE;
    }
    private CapabilityRegistry() {}

    public record CapabilityDefinition<T>(String name, Class<T> clazz, Supplier<T> factory) {}

    public void registerCapabilities(BiConsumer<String, CapabilityDefinition<?>> registry) {
        CapabilityDefinition<SyncDataHolder> syncData = new CapabilityDefinition<>(
                "sync_data_holder",
                SyncDataHolder.class,
                SyncDataHolder::new
        );

        registry.accept(syncData.name(), syncData);
    }
}
