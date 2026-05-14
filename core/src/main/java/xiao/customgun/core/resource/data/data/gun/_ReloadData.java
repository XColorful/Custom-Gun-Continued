/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.item.gun.AmmoFeedType;
import xiao.customgun.core.api.resource.data.data.gun._ReloadDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.gun.reload._ReloadCooldownData;
import xiao.customgun.core.resource.data.data.gun.reload._ReloadFeedData;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _ReloadData extends ResourcePojo<_ReloadData> {

    private AmmoFeedType ammoFeedType = AmmoFeedType.MAGAZINE;
    private boolean infiniteAmmo = false;
    private _ReloadFeedData reloadFeed;
    private _ReloadCooldownData reloadCooldown;

    private static final _ReloadData PARSER = new _ReloadData();
    public static _ReloadData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _ReloadData fromJsonReader(JsonReader reader) throws IOException {
        _ReloadData pojo = new _ReloadData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _ReloadDataTag.AMMO_FEED_TYPE -> pojo.ammoFeedType = AmmoFeedType.fromString(JsonUtils.readString(reader));
                    case _ReloadDataTag.INFINITE_AMMO -> pojo.infiniteAmmo = JsonUtils.readBoolean(reader);
                    case _ReloadDataTag.RELOAD_FEED -> pojo.reloadFeed = JsonUtils.read(reader, _ReloadFeedData::fromJson);
                    case _ReloadDataTag.RELOAD_COOLDOWN -> pojo.reloadCooldown = JsonUtils.read(reader, _ReloadCooldownData::fromJson);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _ReloadData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeString(writer, _ReloadDataTag.AMMO_FEED_TYPE, this.ammoFeedType.toString());
            JsonUtils.writeBoolean(writer, _ReloadDataTag.INFINITE_AMMO, this.infiniteAmmo);
            JsonUtils.write(writer, _ReloadDataTag.RELOAD_FEED, this.reloadFeed, _ReloadFeedData::toJson);
            JsonUtils.write(writer, _ReloadDataTag.RELOAD_COOLDOWN, this.reloadCooldown, _ReloadCooldownData::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public AmmoFeedType getAmmoFeedType() {
        return ammoFeedType;
    }
    public boolean isInfiniteAmmo() {
        return infiniteAmmo;
    }
    public _ReloadFeedData getReloadFeed() {
        return reloadFeed;
    }
    public _ReloadCooldownData getReloadCooldown() {
        return reloadCooldown;
    }

    public void setAmmoFeedType(AmmoFeedType ammoFeedType) {
        this.ammoFeedType = ammoFeedType;
    }
    public void setInfiniteAmmo(boolean infiniteAmmo) {
        this.infiniteAmmo = infiniteAmmo;
    }
    public void setReloadFeed(_ReloadFeedData reloadFeed) {
        this.reloadFeed = reloadFeed;
    }
    public void setReloadCooldown(_ReloadCooldownData reloadCooldown) {
        this.reloadCooldown = reloadCooldown;
    }
}