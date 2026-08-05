/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.instance.data;

import dev.xcolorful.customgun.core.resource.data.index.AmmoIndex;
import dev.xcolorful.customgun.core.resource.instance.PojoInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AmmoIndexInstance extends PojoInstance<AmmoIndex> {

    private AmmoIndexInstance(@NotNull AmmoIndex pojo) {
        super(pojo);
    }

    public static @Nullable AmmoIndexInstance fromPojo(AmmoIndex pojo) {
        if (pojo == null) return null;
        AmmoIndexInstance instance = new AmmoIndexInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        return true;
    }
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;
        if (!resetCache()) return false;

        return true;
    }

    // --------Deprecated--------

    @Deprecated public int getSort() {
        return this.getPojo().getSlotSort();
    }
    @Deprecated public int getStackSize() {
        return this.getPojo().getMaxStackSize();
    }
}
