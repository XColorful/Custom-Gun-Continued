/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.meta;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.meta.GunpackMetaTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class GunpackMeta extends ResourcePojo<GunpackMeta> {

    private String namespace;
    private Map<String, String> dependencies; // modid -> versionRange

    private static final GunpackMeta PARSER = new GunpackMeta();
    public static GunpackMeta fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected GunpackMeta fromJsonReader(JsonReader reader) throws IOException {
        GunpackMeta pojo = new GunpackMeta();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case GunpackMetaTag.NAMESPACE -> pojo.namespace = JsonUtils.readString(reader);
                    case GunpackMetaTag.DEPENDENCIES -> pojo.dependencies = JsonUtils.readString2StringMap(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, GunpackMeta pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeString(writer, GunpackMetaTag.NAMESPACE, this.namespace);
            JsonUtils.writeString2StringMap(writer, GunpackMetaTag.DEPENDENCIES, this.dependencies);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.namespace == null | this.dependencies == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public String getNamespace() {
        return namespace;
    }
    public Map<String, String> getDependencies() {
        return dependencies;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    public void setDependencies(Map<String, String> dependencies) {
        this.dependencies = dependencies;
    }

    // --------Back compatibility--------

    @Override
    public GunpackMeta applyBackCompatibility() {
        this.namespace = this.namespace == null ? "" : this.namespace;
        this.dependencies = this.dependencies == null ? new HashMap<>() : this.dependencies;
        return this;
    }
}
