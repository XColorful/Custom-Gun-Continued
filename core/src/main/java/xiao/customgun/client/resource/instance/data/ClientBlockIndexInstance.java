/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.instance.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.resource.data.index.BlockIndex;
import xiao.customgun.core.resource.instance.PojoInstance;

public final class ClientBlockIndexInstance extends PojoInstance<BlockIndex> {

    private ClientBlockIndexInstance(@NotNull BlockIndex pojo) {
        super(pojo);
    }

    public static @Nullable ClientBlockIndexInstance fromPojo(BlockIndex pojo) {
        if (pojo == null) return null;
        ClientBlockIndexInstance instance = new ClientBlockIndexInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;

        return true;
    }
}
