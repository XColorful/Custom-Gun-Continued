/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model;

import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AttachmentModelObject extends AnimatedModelObject {

    AttachmentModelObject(@NotNull BedrockModel pojo) {
        super(pojo);
    }

    public static @Nullable AttachmentModelObject fromPojo(BedrockModel pojo) {
        if (pojo == null) return null;
        AttachmentModelObject instance = new AttachmentModelObject(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }
}
