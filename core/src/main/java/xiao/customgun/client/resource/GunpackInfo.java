/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.assets.GunpackInfoTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.List;

public final class GunpackInfo extends ResourcePojo<GunpackInfo> {

    private String version;
    private String name;
    private String description;
    private String license;
    private List<String> authors;
    private String date;
    private String url;

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
                    case GunpackInfoTag.VERSION -> pojo.version = JsonUtils.readString(reader);
                    case GunpackInfoTag.NAME -> pojo.name = JsonUtils.readString(reader);
                    case GunpackInfoTag.DESC -> pojo.description = JsonUtils.readString(reader);
                    case GunpackInfoTag.LICENSE -> pojo.license = JsonUtils.readString(reader);
                    case GunpackInfoTag.AUTHORS -> pojo.authors = JsonUtils.readStringList(reader);
                    case GunpackInfoTag.DATE -> pojo.date = JsonUtils.readString(reader);
                    case GunpackInfoTag.URL -> pojo.url = JsonUtils.readString(reader);
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
            JsonUtils.writeString(writer, GunpackInfoTag.VERSION, this.version);
            JsonUtils.writeString(writer, GunpackInfoTag.NAME, this.name);
            JsonUtils.writeString(writer, GunpackInfoTag.DESC, this.description);
            JsonUtils.writeString(writer, GunpackInfoTag.LICENSE, this.license);
            JsonUtils.writeStringList(writer, GunpackInfoTag.AUTHORS, this.authors);
            JsonUtils.writeString(writer, GunpackInfoTag.DATE, this.date);
            JsonUtils.writeString(writer, GunpackInfoTag.URL, this.url);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public String getVersion() {
        return version;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
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
    public String getUrl() {
        return url;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
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
    public void setUrl(String url) {
        this.url = url;
    }
}