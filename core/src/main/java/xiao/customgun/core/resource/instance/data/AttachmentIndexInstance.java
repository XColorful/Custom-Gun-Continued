/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.instance.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.resource.data.index.AttachmentIndex;
import xiao.customgun.core.resource.instance.PojoInstance;

public class AttachmentIndexInstance extends PojoInstance<AttachmentIndex> {

    private AttachmentIndexInstance(@NotNull AttachmentIndex pojo) {
        super(pojo);
    }

    public static @Nullable AttachmentIndexInstance fromPojo(AttachmentIndex pojo) {
        if (pojo == null) return null;
        AttachmentIndexInstance instance = new AttachmentIndexInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;

        return true;
    }
}