/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry.bone.cube;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry.bone.cube.uv._FaceUv;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry.bone.cube._UvTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class _Uv extends ResourcePojo<_Uv> {

    private float @Nullable [] uv;
    private _FaceUv north;
    private _FaceUv south;
    private _FaceUv east;
    private _FaceUv west;
    private _FaceUv up;
    private _FaceUv down;

    private static final _Uv PARSER = new _Uv();
    public static _Uv fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _Uv fromJsonReader(JsonReader reader) throws IOException {
        _Uv pojo = new _Uv();
        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            pojo.uv = JsonUtils.readFloatArray(reader);
            return pojo;
        }

        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _UvTag.NORTH -> pojo.north = JsonUtils.read(reader, _FaceUv::fromJson);
                    case _UvTag.SOUTH -> pojo.south = JsonUtils.read(reader, _FaceUv::fromJson);
                    case _UvTag.EAST -> pojo.east = JsonUtils.read(reader, _FaceUv::fromJson);
                    case _UvTag.WEST -> pojo.west = JsonUtils.read(reader, _FaceUv::fromJson);
                    case _UvTag.UP -> pojo.up = JsonUtils.read(reader, _FaceUv::fromJson);
                    case _UvTag.DOWN -> pojo.down = JsonUtils.read(reader, _FaceUv::fromJson);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _Uv pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        if (this.uv != null) {
            writer.beginArray(); {
                for (float f : this.uv) writer.value(f);
            }
            writer.endArray();
            return;
        }

        writer.beginObject(); {
            JsonUtils.write(writer, _UvTag.NORTH, this.north, _FaceUv::toJson);
            JsonUtils.write(writer, _UvTag.SOUTH, this.south, _FaceUv::toJson);
            JsonUtils.write(writer, _UvTag.EAST, this.east, _FaceUv::toJson);
            JsonUtils.write(writer, _UvTag.WEST, this.west, _FaceUv::toJson);
            JsonUtils.write(writer, _UvTag.UP, this.up, _FaceUv::toJson);
            JsonUtils.write(writer, _UvTag.DOWN, this.down, _FaceUv::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float @Nullable [] getUv() {
        return uv;
    }
    public _FaceUv getNorth() {
        return north;
    }
    public _FaceUv getSouth() {
        return south;
    }
    public _FaceUv getEast() {
        return east;
    }
    public _FaceUv getWest() {
        return west;
    }
    public _FaceUv getUp() {
        return up;
    }
    public _FaceUv getDown() {
        return down;
    }

    public void setUv(float[] uv) {
        this.uv = uv;
    }
    public void setNorth(_FaceUv north) {
        this.north = north;
    }
    public void setSouth(_FaceUv south) {
        this.south = south;
    }
    public void setEast(_FaceUv east) {
        this.east = east;
    }
    public void setWest(_FaceUv west) {
        this.west = west;
    }
    public void setUp(_FaceUv up) {
        this.up = up;
    }
    public void setDown(_FaceUv down) {
        this.down = down;
    }

    // --------Special--------

    public _FaceUv getFaceUv(Direction direction) {
        _FaceUv faceUv = switch (direction) {
            case NORTH -> this.north;
            case SOUTH -> this.south;
            case EAST -> this.east;
            case WEST -> this.west;
            case UP -> this.up;
            case DOWN -> this.down;
        };
        return faceUv != null ? faceUv : _FaceUv.EMPTY;
    }
}