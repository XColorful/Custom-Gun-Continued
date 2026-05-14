/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.gun._RecoilDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.gun.recoil._RecoilEntryData;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.List;

public class _RecoilData extends ResourcePojo<_RecoilData> {

    private List<_RecoilEntryData> pitchRecoils;
    private List<_RecoilEntryData> yawRecoils;

    private static final _RecoilData PARSER = new _RecoilData();
    public static _RecoilData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _RecoilData fromJsonReader(JsonReader reader) throws IOException {
        _RecoilData pojo = new _RecoilData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _RecoilDataTag.PITCH_RECOIL -> pojo.pitchRecoils = JsonUtils.readList(reader, _RecoilEntryData::fromJson);
                    case _RecoilDataTag.YAW_RECOIL -> pojo.yawRecoils = JsonUtils.readList(reader, _RecoilEntryData::fromJson);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _RecoilData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }

    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeList(writer, _RecoilDataTag.PITCH_RECOIL, this.pitchRecoils, _RecoilEntryData::toJson);
            JsonUtils.writeList(writer, _RecoilDataTag.YAW_RECOIL, this.yawRecoils, _RecoilEntryData::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public List<_RecoilEntryData> getPitchRecoils() {
        return pitchRecoils;
    }
    public List<_RecoilEntryData> getYawRecoils() {
        return yawRecoils;
    }

    public void setPitchRecoils(List<_RecoilEntryData> pitchRecoils) {
        this.pitchRecoils = pitchRecoils;
    }
    public void setYawRecoils(List<_RecoilEntryData> yawRecoils) {
        this.yawRecoils = yawRecoils;
    }
}