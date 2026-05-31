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

public final class AmmoModelObject extends ModelObject {

    AmmoModelObject(@NotNull BedrockModel pojo) {
        super(pojo);
    }

    public static @Nullable AmmoModelObject fromPojo(BedrockModel pojo) {
        if (pojo == null) return null;
        AmmoModelObject instance = new AmmoModelObject(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }
}
