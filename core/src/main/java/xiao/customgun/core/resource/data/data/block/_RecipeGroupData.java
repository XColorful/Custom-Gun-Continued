/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.block;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import xiao.customgun.core.api.resource.data.data.block._RecipeGroupDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _RecipeGroupData extends ResourcePojo<_RecipeGroupData> {

    private ResourceLocation groupCategory;
    private MutableComponent nameLang;

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
                case _RecipeGroupDataTag.NAME_LANG -> pojo.nameLang = JsonUtils.readTranslatable(reader);
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
            JsonUtils.writeTranslatable(writer, _RecipeGroupDataTag.NAME_LANG, this.nameLang);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public ResourceLocation getGroupCategory() {
        return groupCategory;
    }
    public MutableComponent getNameLang() {
        return nameLang;
    }

    public void setGroupCategory(ResourceLocation groupCategory) {
        this.groupCategory = groupCategory;
    }
    public void setNameLang(MutableComponent nameLang) {
        this.nameLang = nameLang;
    }
}