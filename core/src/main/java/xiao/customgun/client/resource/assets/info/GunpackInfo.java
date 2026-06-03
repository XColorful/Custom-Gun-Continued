/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.info;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.Identifier;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.api.resource.assets.info.GunpackInfoTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class GunpackInfo extends ResourcePojo<GunpackInfo> {

    private String gunpackVersion;
    private Identifier nameLocation;
    private Identifier tooltipLocation;
    private String license;
    private List<String> authors;
    private String date;
    private String gunpackUrl;

    private static final GunpackInfo PARSER = new GunpackInfo();
    public static GunpackInfo fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected GunpackInfo fromJsonReader(JsonReader reader) throws IOException {
        GunpackInfo pojo = new GunpackInfo();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case GunpackInfoTag.GUNPACK_VERSION, GunpackInfoTag.GUNPACK_VERSION_OLD1 -> pojo.gunpackVersion = JsonUtils.readString(reader);
                    case GunpackInfoTag.NAME_LOCATION, GunpackInfoTag.NAME_LOCATION_OLD1 -> pojo.nameLocation = JsonUtils.readResourceLocation(reader);
                    case GunpackInfoTag.TOOLTIP_LOCATION, GunpackInfoTag.TOOLTIP_LOCATION_OLD1 -> pojo.tooltipLocation = JsonUtils.readResourceLocation(reader);
                    case GunpackInfoTag.LICENSE -> pojo.license = JsonUtils.readString(reader);
                    case GunpackInfoTag.AUTHORS -> pojo.authors = JsonUtils.readStringList(reader);
                    case GunpackInfoTag.DATE -> pojo.date = JsonUtils.readString(reader);
                    case GunpackInfoTag.GUNPACK_URL, GunpackInfoTag.GUNPACK_URL_OLD1 -> pojo.gunpackUrl = JsonUtils.readString(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, GunpackInfo pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeString(writer, GunpackInfoTag.GUNPACK_VERSION, this.gunpackVersion);
            JsonUtils.writeResourceLocation(writer, GunpackInfoTag.NAME_LOCATION, this.nameLocation);
            JsonUtils.writeResourceLocation(writer, GunpackInfoTag.TOOLTIP_LOCATION, this.tooltipLocation);
            JsonUtils.writeString(writer, GunpackInfoTag.LICENSE, this.license);
            JsonUtils.writeStringList(writer, GunpackInfoTag.AUTHORS, this.authors);
            JsonUtils.writeString(writer, GunpackInfoTag.DATE, this.date);
            JsonUtils.writeString(writer, GunpackInfoTag.GUNPACK_URL, this.gunpackUrl);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.gunpackVersion == null | this.nameLocation == null | this.tooltipLocation == null | this.license == null);
        boolean n2 = (this.authors == null | this.date == null | this.gunpackUrl == null);
        if (n1 | n2) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public String getGunpackVersion() {
        return gunpackVersion;
    }
    public Identifier getNameLocation() {
        return nameLocation;
    }
    public Identifier getTooltipLocation() {
        return tooltipLocation;
    }
    public String getLicense() {
        return license;
    }
    public List<String> getAuthors() {
        return authors;
    }
    public String getDate() {
        return date;
    }
    public String getGunpackUrl() {
        return gunpackUrl;
    }

    public void setGunpackVersion(String gunpackVersion) {
        this.gunpackVersion = gunpackVersion;
    }
    public void setNameLocation(Identifier nameLocation) {
        this.nameLocation = nameLocation;
    }
    public void setTooltipLocation(Identifier tooltipLocation) {
        this.tooltipLocation = tooltipLocation;
    }
    public void setLicense(String license) {
        this.license = license;
    }
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setGunpackUrl(String gunpackUrl) {
        this.gunpackUrl = gunpackUrl;
    }

    // --------Back compatibility--------

    @Override
    public GunpackInfo applyBackCompatibility() {
        this.gunpackVersion = this.gunpackVersion == null ? "" : this.gunpackVersion;
        this.nameLocation = this.nameLocation == null ? ResourceTag.NULL_LOCATION : this.nameLocation;
        this.tooltipLocation = this.tooltipLocation == null ? ResourceTag.NULL_LOCATION : this.tooltipLocation;
        this.license = this.license == null ? "" : this.license;
        this.authors = this.authors == null ? new ArrayList<>() : this.authors;
        this.date = this.date == null ? "" : this.date;
        this.gunpackUrl = this.gunpackUrl == null ? "" : this.gunpackUrl;
        return this;
    }

    // --------Back compatibility--------
}