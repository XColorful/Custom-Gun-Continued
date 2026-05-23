/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.instance.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.index.GunIndex;
import xiao.customgun.core.resource.instance.PojoInstance;

public final class ClientGunIndexInstance extends PojoInstance<GunIndex> {

    private ClientGunIndexInstance(@NotNull GunIndex pojo) {
        super(pojo);
    }

    public static @Nullable ClientGunIndexInstance fromPojo(GunIndex pojo) {
        if (pojo == null) return null;
        ClientGunIndexInstance instance = new ClientGunIndexInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;

        return true;
    }

    // --------便利接口--------

    public @Nullable GunData getGunData() {
        return ResourceApi.getGunData(this.getPojo().getDataLocation());
    }

    public @Nullable GunDisplayInstance getGunDisplayInstance() {
        return ClientResourceApi.getGunDisplayInstance(this.getPojo().getDisplayIndexLocation());
    }
}
