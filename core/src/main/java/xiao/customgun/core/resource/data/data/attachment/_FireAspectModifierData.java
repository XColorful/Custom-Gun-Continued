/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.attachment;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.attachment._FireAspectModifierDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _FireAspectModifierData extends ResourcePojo<_FireAspectModifierData> {

    private boolean igniteEntity = false;
    private boolean igniteBlock = false;

    private static final _FireAspectModifierData PARSER = new _FireAspectModifierData();
    public static _FireAspectModifierData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _FireAspectModifierData fromJsonReader(JsonReader reader) throws IOException {
        _FireAspectModifierData pojo = new _FireAspectModifierData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _FireAspectModifierDataTag.IGNITE_ENTITY, _FireAspectModifierDataTag.IGNITE_ENTITY_OLD1 -> pojo.igniteEntity = JsonUtils.readBoolean(reader);
                    case _FireAspectModifierDataTag.IGNITE_BLOCK, _FireAspectModifierDataTag.IGNITE_BLOCK_OLD1 -> pojo.igniteBlock = JsonUtils.readBoolean(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _FireAspectModifierData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeBoolean(writer, _FireAspectModifierDataTag.IGNITE_ENTITY, this.igniteEntity);
            JsonUtils.writeBoolean(writer, _FireAspectModifierDataTag.IGNITE_BLOCK, this.igniteBlock);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public boolean getIgniteEntity() {
        return igniteEntity;
    }
    public boolean getIgniteBlock() {
        return igniteBlock;
    }

    public void setIgniteEntity(boolean igniteEntity) {
        this.igniteEntity = igniteEntity;
    }
    public void setIgniteBlock(boolean igniteBlock) {
        this.igniteBlock = igniteBlock;
    }

    // --------Back compatibility--------
}