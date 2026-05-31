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
import xiao.customgun.core.resource.instance.PojoInstance;

public class ModelObject extends PojoInstance<BedrockModel> {

    ModelObject(@NotNull BedrockModel pojo) {
        super(pojo);
    }

    public static @Nullable ModelObject fromPojo(BedrockModel pojo) {
        if (pojo == null) return null;
        ModelObject instance = new ModelObject(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;

        return true;
    }
}
