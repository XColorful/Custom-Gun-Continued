/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.tags;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import xiao.customgun.CustomGun;

public enum InteractKeyType {
    BLOCK(Registries.BLOCK),
    ENTITY(Registries.ENTITY_TYPE);

    public final ResourceKey<? extends Registry<?>> tagKey;
    public final TagKey<?> whitelist;
    public final TagKey<?> blacklist;
    <T> InteractKeyType(ResourceKey<? extends Registry<T>> registryKey) {
        this.tagKey = registryKey;
        this.whitelist = TagKey.create(registryKey,
                CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s/%s",
                        CustomGun.MOD_ID,
                        InteractKeyTag.INTERACT_KEY,
                        InteractKeyTag.WHITELIST)));
        this.blacklist = TagKey.create(registryKey,
                CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s/%s",
                        CustomGun.MOD_ID,
                        InteractKeyTag.INTERACT_KEY,
                        InteractKeyTag.BLACKLIST)));
    }

    @SuppressWarnings("unchecked")
    public <T> TagKey<T> getWhitelist() {
        return (TagKey<T>) whitelist;
    }
    @SuppressWarnings("unchecked")
    public <T> TagKey<T> getBlacklist() {
        return (TagKey<T>) blacklist;
    }
}
