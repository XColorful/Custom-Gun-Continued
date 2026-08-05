/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.minecraft.input;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.minecraft.input.CustomInputKeyTag;

import java.util.HashMap;
import java.util.Map;

public enum CustomInputKey implements ICustomInputKey {
    // config
    @Deprecated(forRemoval = true) CONFIG(CustomInputKeyTag.CONFIG),
    // player
    INTERACT(CustomInputKeyTag.INTERACT),
    REFIT(CustomInputKeyTag.REFIT),
    // shooter
    AIM(CustomInputKeyTag.AIM),
    INSPECT(CustomInputKeyTag.INSPECT),
    MELEE(CustomInputKeyTag.MELEE),
    PRONE(CustomInputKeyTag.PRONE),
    RELOAD(CustomInputKeyTag.RELOAD),
    SHOOT(CustomInputKeyTag.SHOOT),
    SWITCH_FIRE_MODE(CustomInputKeyTag.SWITCH_FIRE_MODE),
    ZOOM(CustomInputKeyTag.ZOOM);

    public final String tagName;
    public final String categoryName;
    public final String registryName;
    public final Identifier registryLocation;
    public final Component categoryLang;
    CustomInputKey(String name) {
        this(CustomInputKeyTag.PREFIX, name);
    }
    CustomInputKey(String prefix, String name) {
        this.tagName = name;
        this.categoryName = name;
        this.registryLocation = CustomGun.getMcRegistry().createResourceLocation(prefix + name);
        this.registryName = registryLocation.toString();
        this.categoryLang = Component.translatable(this.registryLocation.getPath());
    }
    @Override public String getTagName() {
        return this.tagName;
    }
    @Override public String getCategoryName() {
        return this.categoryName;
    }
    @Override public String getRegistryName() {
        return this.registryName;
    }
    @Override public Identifier getRegistryLocation() {
        return this.registryLocation;
    }

    @Override
    public Component getCategoryLang() {
        return this.categoryLang;
    }

    private static final Map<String, ICustomInputKey> INPUT_KEYS = new HashMap<>();
    @ApiStatus.Internal
    public static void registerInputKey(ICustomInputKey key) {
        INPUT_KEYS.put(key.getTagName(), key);
        INPUT_KEYS.put(key.getCategoryName(), key);
        INPUT_KEYS.put(key.getRegistryName(), key);
    }

    static {
        for (ICustomInputKey key : values()) {
            registerInputKey(key);
        }
    }

    public static @Nullable ICustomInputKey fromString(String name) {
        return name != null ? INPUT_KEYS.get(name) : null;
    }

    @Override
    public String toString() {
        return this.categoryName;
    }
}
