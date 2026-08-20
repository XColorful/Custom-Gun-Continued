/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data.data.block;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.api.resource.data.data.block._RecipeGroupDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.ComponentUtils;
import dev.xcolorful.customgun.core.util.JsonUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public final class _RecipeGroupData extends ResourcePojo<_RecipeGroupData> {

    private ResourceLocation groupCategory;
    private String nameLang;

    private static final _RecipeGroupData PARSER = new _RecipeGroupData();
    public static _RecipeGroupData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _RecipeGroupData fromJsonReader(JsonReader reader) throws IOException {
        _RecipeGroupData pojo = new _RecipeGroupData();
        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            switch (key) {
                case _RecipeGroupDataTag.GROUP_CATEGORY -> pojo.groupCategory = JsonUtils.readResourceLocation(reader);
                case _RecipeGroupDataTag.NAME_LANG -> pojo.nameLang = JsonUtils.readString(reader);
                default -> reader.skipValue();
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _RecipeGroupData data) throws IOException {
        if (data != null) data.toJson(writer);
    }

    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, _RecipeGroupDataTag.GROUP_CATEGORY, this.groupCategory);
            JsonUtils.writeString(writer, _RecipeGroupDataTag.NAME_LANG, this.nameLang);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.groupCategory == null | this.nameLang == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public ResourceLocation getGroupCategory() {
        return groupCategory;
    }
    public String getNameLang() {
        return nameLang;
    }

    public void setGroupCategory(ResourceLocation groupCategory) {
        this.groupCategory = groupCategory;
    }
    public void setNameLang(String nameLang) {
        this.nameLang = nameLang;
    }

    // --------Back compatibility--------

    @Override
    public _RecipeGroupData applyBackCompatibility() {
        this.groupCategory = this.groupCategory == null ? ResourceTag.NULL_LOCATION : this.groupCategory;
        this.nameLang = this.nameLang == null ? ComponentUtils.UNKNOWN_TRANSLATABLE_KEY : this.nameLang;
        return this;
    }
}