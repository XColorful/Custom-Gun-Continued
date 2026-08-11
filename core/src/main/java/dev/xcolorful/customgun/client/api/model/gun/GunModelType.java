/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.model.gun;

import dev.xcolorful.customgun.client.model.GunModelObject;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.core.api.model.gun.GunModelTypeTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum GunModelType implements IGunModelType {
    DEFAULT(GunModelTypeTag.DEFAULT, GunModelObject::fromPojo);

    public final String typeName;
    public final Function<BedrockModel, ? extends GunModelObject> constructor;
    GunModelType(String name, Function<BedrockModel, ? extends @Nullable GunModelObject> constructor) {
        this.typeName = name;
        this.constructor = constructor;
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    @Override
    public @Nullable GunModelObject create(BedrockModel pojo) {
        return constructor.apply(pojo);
    }

    private static final Map<String, IGunModelType> MODEL_TYPES = new HashMap<>();
    @ApiStatus.Internal
    public static void registerGunModelType(IGunModelType gunModelType) {
        MODEL_TYPES.put(gunModelType.getName(), gunModelType);
    }

    static {
        for (GunModelType type : values()) {
            registerGunModelType(type);
        }
    }

    public static @Nullable IGunModelType fromString(String name) {
        return name != null ? MODEL_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
