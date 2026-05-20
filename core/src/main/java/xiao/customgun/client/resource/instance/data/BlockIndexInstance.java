/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.instance.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.resource.instance.ClientPojoInstance;
import xiao.customgun.core.resource.data.index.BlockIndex;

public final class BlockIndexInstance extends ClientPojoInstance<BlockIndex> {

    private BlockIndexInstance(@NotNull BlockIndex pojo) {
        super(pojo);
    }

    public static @Nullable BlockIndexInstance fromPojo(BlockIndex pojo) {
        if (pojo == null) return null;
        BlockIndexInstance instance = new BlockIndexInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;

        return true;
    }
}
