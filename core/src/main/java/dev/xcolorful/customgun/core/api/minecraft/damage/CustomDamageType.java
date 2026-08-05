/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.minecraft.damage;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum CustomDamageType implements ResourceTag.RegistryTag {
    BULLET(false, false,
            CustomDamageTypeTag.BULLET, CustomDamageTypeTag.BULLET_OLD1),
    PIERCER(true, false,
            CustomDamageTypeTag.PIERCER, CustomDamageTypeTag.PIERCER_OLD1),
    BREAKER(false, true,
            CustomDamageTypeTag.BREAKER, CustomDamageTypeTag.BREAKER_OLD1),
    OVERRIDER(true, true,
            CustomDamageTypeTag.OVERRIDER, CustomDamageTypeTag.OVERRIDER_OLD1);

    public static class Tag {
        public static final @NotNull ResourceLocation BULLET_DAMAGE = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, CustomDamageTypeTag.BULLET_DAMAGE));
        public static final @NotNull ResourceLocation PIERCE_DAMAGE = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, CustomDamageTypeTag.PIERCE_DAMAGE));
        public static final @NotNull ResourceLocation BYPASS_DAMAGE = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, CustomDamageTypeTag.BYPASS_DAMAGE));
    }

    public final boolean isPierce;
    public final boolean isBypass;
    public final String typeName;
    public final String typeNameOld;
    public final String registryName;
    public final ResourceLocation registryLocation;
    public final ResourceKey<DamageType> resourceKey;
    CustomDamageType(boolean isPierce, boolean isBypass, String typeName, String typeNameOld) {
        this.isPierce = isPierce;
        this.isBypass = isBypass;
        IMcRegistry mcRegistry = CustomGun.getMcRegistry();
        this.typeName = typeName;
        this.typeNameOld = typeNameOld;
        this.registryLocation = mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, this.typeName));
        this.registryName = this.registryLocation.toString();
        this.resourceKey = mcRegistry.createResourceKey(Registries.DAMAGE_TYPE, this.registryLocation);
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getRegistryName() {
        return this.registryName;
    }
    @Override public ResourceLocation getRegistryLocation() {
        return this.registryLocation;
    }

    private static final Map<String, CustomDamageType> DAMAGE_TYPES = new HashMap<>();

    static {
        for (CustomDamageType damageType : CustomDamageType.values()) {
            DAMAGE_TYPES.put(damageType.typeName, damageType);
            DAMAGE_TYPES.put(damageType.typeNameOld, damageType);
            DAMAGE_TYPES.put(damageType.registryName, damageType);
            DAMAGE_TYPES.put(damageType.resourceKey.toString(), damageType);
        }
    }

    private static final CustomDamageType[] VALUES = { // 索引 -> bit mask
            BULLET, // 0 -> 00
            PIERCER, // 1 -> 01
            BREAKER, // 2 -> 10
            OVERRIDER // 3 -> 11
    };
    public static CustomDamageType of(boolean pierce, boolean bypass) {
        int mask = (pierce ? 1 : 0) | (bypass ? 1 << 1 : 0);
        return VALUES[mask];
    }

    public static @Nullable CustomDamageType fromString(String name) {
        return name != null ? DAMAGE_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
