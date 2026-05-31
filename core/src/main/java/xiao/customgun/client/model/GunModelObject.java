/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.resource.assets.model.BedrockModel;

public final class GunModelObject extends AnimatedModelObject {

    GunModelObject(@NotNull BedrockModel pojo) {
        super(pojo);
    }

    public static @Nullable GunModelObject fromPojo(BedrockModel pojo) {
        if (pojo == null) return null;
        GunModelObject instance = new GunModelObject(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }
}
