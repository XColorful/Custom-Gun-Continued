/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.minecraft.particle;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;

public enum CustomParticleType implements ResourceTag.RegistryTag {
    BULLET_HOLE(CustomParticleTypeTag.BULLET_HOLE);

    public final String typeName;
    public final String registryName;
    public final Identifier registryLocation;
    CustomParticleType(String name) {
        this.typeName = name;
        this.registryName = String.format("%s:%s", CustomGun.MOD_ID, this.typeName);
        this.registryLocation = CustomGun.getMcRegistry().createResourceLocation(this.registryName);
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getRegistryName() {
        return this.registryName;
    }
    @Override public Identifier getRegistryLocation() {
        return this.registryLocation;
    }

    private static final Map<String, CustomParticleType> PARTICLE_TYPES = new HashMap<>();

    static {
        for (CustomParticleType type : values()) {
            PARTICLE_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable CustomParticleType fromString(String name) {
        return name != null ? PARTICLE_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
