/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.neoforge.init.registry;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import xiao.customgun.core.api.init.registry.IRegistryObject;

public class NeoRegistryObject<R, T extends R> implements IRegistryObject<T> {

    private final DeferredHolder<R, T> registryObject;
    private final String id;

    public NeoRegistryObject(DeferredHolder<R, T> deferredHolder, String id) {
        this.registryObject = deferredHolder;
        this.id = id;
    }

    @Override
    public String getId() {
        return registryObject.getId().getPath();
    }

    @Override
    public T get() {
        return registryObject.get();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return registryObject.getId();
    }
}