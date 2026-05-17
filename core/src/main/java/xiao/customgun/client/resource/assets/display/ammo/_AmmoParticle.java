/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display.ammo;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.resource.ResourcePojo;

import java.io.IOException;

public final class _AmmoParticle extends ResourcePojo<_AmmoParticle> {

    private static final _AmmoParticle PARSER = new _AmmoParticle();
    public static _AmmoParticle fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _AmmoParticle fromJsonReader(JsonReader reader) throws IOException {
        _AmmoParticle pojo = new _AmmoParticle();
        reader.beginObject(); {
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _AmmoParticle pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------
}