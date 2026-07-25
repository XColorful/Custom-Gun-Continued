/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.attachment;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.attachment.__ModifierDataTag;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _SimpleModifierData extends __ModifierData<_SimpleModifierData> {

    private static final _SimpleModifierData PARSER = new _SimpleModifierData();
    public static _SimpleModifierData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _SimpleModifierData fromJsonReader(JsonReader reader) throws IOException {
        _SimpleModifierData pojo = new _SimpleModifierData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case __ModifierDataTag.SHARED_BASE_ADD, __ModifierDataTag.SHARED_BASE_ADD_OLD1 -> pojo.setSharedBaseAdd(JsonUtils.readFloat(reader));
                    case __ModifierDataTag.SHARED_PERCENT_ADD, __ModifierDataTag.SHARED_PERCENT_ADD_OLD1 -> pojo.setSharedPercentAdd(JsonUtils.readFloat(reader));
                    case __ModifierDataTag.UNIQUE_MULTIPLIER, __ModifierDataTag.UNIQUE_MULTIPLIER_OLD1 -> pojo.setUniqueMultiplier(JsonUtils.readFloat(reader));
                    case __ModifierDataTag.SCRIPT_FUNCTION, __ModifierDataTag.SCRIPT_FUNCTION_OLD1 -> pojo.setScriptFunction(JsonUtils.readString(reader));
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _SimpleModifierData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, __ModifierDataTag.SHARED_BASE_ADD, this.getSharedBaseAdd());
            JsonUtils.writeFloat(writer, __ModifierDataTag.SHARED_PERCENT_ADD, this.getSharedPercentAdd());
            JsonUtils.writeFloat(writer, __ModifierDataTag.UNIQUE_MULTIPLIER, this.getUniqueMultiplier());
            JsonUtils.writeString(writer, __ModifierDataTag.SCRIPT_FUNCTION, this.getScriptFunction());
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Back compatibility--------
}