/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.instance.data;

import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.index.GunIndex;
import dev.xcolorful.customgun.core.resource.instance.PojoInstance;
import dev.xcolorful.customgun.core.util.ComponentUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ClientGunIndexInstance extends PojoInstance<GunIndex> {

    private @Nullable GunData gunDataCache;
    private @Nullable GunDisplayInstance gunDisplayInstanceCache;

    private ClientGunIndexInstance(@NotNull GunIndex pojo) {
        super(pojo);
    }

    public static @Nullable ClientGunIndexInstance fromPojo(GunIndex pojo) {
        if (pojo == null) return null;
        ClientGunIndexInstance instance = new ClientGunIndexInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        Object ignored = this.getGunData();
        ignored = this.getGunDisplayInstance();
        return true;
    }
    @Override protected boolean isPojoValid() {
        if (!super.isPojoValid()) return false;

        return true;
    }

    // --------Getter--------

    public @Nullable GunData getGunData() {
        if (this.gunDataCache == null) {
            GunData gunData = ResourceApi.getGunData(this.getPojo().getDataLocation());
            if (gunData != null && gunData.isValid()) this.gunDataCache = gunData;
        }
        return this.gunDataCache;
    }

    public @Nullable GunDisplayInstance getGunDisplayInstance() {
        if (this.gunDisplayInstanceCache == null) this.gunDisplayInstanceCache = ClientResourceApi.getGunDisplayInstance(this.getPojo().getDisplayIndexLocation());
        return this.gunDisplayInstanceCache;
    }

    // --------Deprecated--------

    @Deprecated public String getName() {
        return this.getPojo().getNameLang();
    }
    @Deprecated public String getType() {
        return this.getPojo().getGunCategory().toString();
    }
    @Deprecated public String getItemType() {
        return this.getPojo().getItemType().toString();
    }
    @Deprecated public @Nullable GunDisplayInstance getDefaultDisplay() {
        return this.getGunDisplayInstance();
    }
}
