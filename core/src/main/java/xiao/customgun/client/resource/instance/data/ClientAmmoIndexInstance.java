/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.instance.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.resource.data.index.AmmoIndex;
import xiao.customgun.core.resource.instance.PojoInstance;

public final class ClientAmmoIndexInstance extends PojoInstance<AmmoIndex> {

    private ClientAmmoIndexInstance(@NotNull AmmoIndex pojo) {
        super(pojo);
    }

    public static @Nullable ClientAmmoIndexInstance fromPojo(AmmoIndex pojo) {
        if (pojo == null) return null;
        ClientAmmoIndexInstance instance = new ClientAmmoIndexInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;

        return true;
    }
}
