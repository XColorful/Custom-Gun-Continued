/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.item.gun.AmmoFeedType;
import dev.xcolorful.customgun.core.api.resource.data.data.gun._ReloadDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.gun.reload._ReloadCooldownData;
import dev.xcolorful.customgun.core.resource.data.data.gun.reload._ReloadFeedData;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _ReloadData extends ResourcePojo<_ReloadData> {

    private AmmoFeedType ammoFeedType;
    private boolean freeAmmoFeed = false;
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
                    case _ReloadDataTag.AMMO_FEED_TYPE, _ReloadDataTag.AMMO_FEED_TYPE_OLD1 -> pojo.ammoFeedType = JsonUtils.readFromString(reader, AmmoFeedType::fromString);
                    case _ReloadDataTag.FREE_AMMO_FEED, _ReloadDataTag.FREE_AMMO_FEED_OLD1 -> pojo.freeAmmoFeed = JsonUtils.readBoolean(reader);
                    case _ReloadDataTag.RELOAD_FEED, _ReloadDataTag.RELOAD_FEED_OLD1 -> pojo.reloadFeed = JsonUtils.read(reader, _ReloadFeedData::fromJson);
                    case _ReloadDataTag.RELOAD_COOLDOWN, _ReloadDataTag.RELOAD_COOLDOWN_OLD1 -> pojo.reloadCooldown = JsonUtils.read(reader, _ReloadCooldownData::fromJson);
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
            JsonUtils.writeToString(writer, _ReloadDataTag.AMMO_FEED_TYPE, this.ammoFeedType);
            JsonUtils.writeBoolean(writer, _ReloadDataTag.FREE_AMMO_FEED, this.freeAmmoFeed);
            JsonUtils.write(writer, _ReloadDataTag.RELOAD_FEED, this.reloadFeed, _ReloadFeedData::toJson);
            JsonUtils.write(writer, _ReloadDataTag.RELOAD_COOLDOWN, this.reloadCooldown, _ReloadCooldownData::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.ammoFeedType == null | this.reloadFeed == null | this.reloadCooldown == null);
        if (n1) {
            this.setValid(false);
            return;
        }
        this.reloadFeed.validate();
        this.reloadCooldown.validate();
        boolean v1 = (this.reloadFeed.isValid() & this.reloadCooldown.isValid());
        if (!(v1)) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public AmmoFeedType getAmmoFeedType() {
        return ammoFeedType;
    }
    public boolean getFreeAmmoFeed() {
        return freeAmmoFeed;
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
    public void setFreeAmmoFeed(boolean freeAmmoFeed) {
        this.freeAmmoFeed = freeAmmoFeed;
    }
    public void setReloadFeed(_ReloadFeedData reloadFeed) {
        this.reloadFeed = reloadFeed;
    }
    public void setReloadCooldown(_ReloadCooldownData reloadCooldown) {
        this.reloadCooldown = reloadCooldown;
    }

    // --------Back compatibility--------

    @Override
    public _ReloadData applyBackCompatibility() {
        this.ammoFeedType = this.ammoFeedType == null ? AmmoFeedType.MAGAZINE : this.ammoFeedType;
        this.reloadFeed = this.reloadFeed == null ? new _ReloadFeedData().applyBackCompatibility() : this.reloadFeed.applyBackCompatibility();
        this.reloadCooldown = this.reloadCooldown == null ? new _ReloadCooldownData().applyBackCompatibility() : this.reloadCooldown.applyBackCompatibility();
        return this;
    }
}