/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.info;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;
import xiao.customgun.core.api.resource.assets.info.GunpackInfoTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.List;

public final class GunpackInfo extends ResourcePojo<GunpackInfo> {

    private String gunpackVersion;
    private ResourceLocation nameLocation;
    private ResourceLocation tooltipLocation;
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
        boolean n1 = (this.gunpackVersion == null | this.nameLocation == null | this.authors == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public String getGunpackVersion() {
        return gunpackVersion;
    }
    public ResourceLocation getNameLocation() {
        return nameLocation;
    }
    public ResourceLocation getTooltipLocation() {
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
    public void setNameLocation(ResourceLocation nameLocation) {
        this.nameLocation = nameLocation;
    }
    public void setTooltipLocation(ResourceLocation tooltipLocation) {
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
}