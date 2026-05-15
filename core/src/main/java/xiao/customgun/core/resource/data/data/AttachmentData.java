/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.item.attachment.MagazineCategory;
import xiao.customgun.core.api.resource.data.data.AttachmentDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class AttachmentData extends ResourcePojo<AttachmentData> {

    private float weight = 0.0F;
    private MagazineCategory magazineCategory;

    private static final AttachmentData PARSER = new AttachmentData();
    public static AttachmentData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected AttachmentData fromJsonReader(JsonReader reader) throws IOException {
        AttachmentData pojo = new AttachmentData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case AttachmentDataTag.WEIGHT -> pojo.weight = JsonUtils.readFloat(reader);
                    case AttachmentDataTag.MAGAZINE_CATEGORY -> pojo.magazineCategory = JsonUtils.readFromString(reader, MagazineCategory::fromString);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, AttachmentData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, AttachmentDataTag.WEIGHT, this.weight);
            JsonUtils.writeToString(writer, AttachmentDataTag.MAGAZINE_CATEGORY, this.magazineCategory);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float getWeight() {
        return weight;
    }
    public MagazineCategory getMagazineCategory() {
        return magazineCategory;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
    public void setMagazineCategory(MagazineCategory magazineCategory) {
        this.magazineCategory = magazineCategory;
    }
}