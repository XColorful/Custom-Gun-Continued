/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.recipe.recipe.ingredient;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;
import xiao.customgun.core.api.resource.data.recipe.recipe.ingredient._IngredientFilterDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _IngredientFilterData extends ResourcePojo<_IngredientFilterData> {

    private ResourceLocation itemFilterLocation;
    private ResourceLocation tagFilterLocation;

    private static final _IngredientFilterData PARSER = new _IngredientFilterData();
    public static _IngredientFilterData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _IngredientFilterData fromJsonReader(JsonReader reader) throws IOException {
        _IngredientFilterData pojo = new _IngredientFilterData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _IngredientFilterDataTag.ITEM_FILTER_LOCATION -> pojo.itemFilterLocation = JsonUtils.readResourceLocation(reader);
                    case _IngredientFilterDataTag.TAG_FILTER_LOCATION -> pojo.tagFilterLocation = JsonUtils.readResourceLocation(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _IngredientFilterData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, _IngredientFilterDataTag.ITEM_FILTER_LOCATION, this.itemFilterLocation);
            JsonUtils.writeResourceLocation(writer, _IngredientFilterDataTag.TAG_FILTER_LOCATION, this.tagFilterLocation);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(this.itemFilterLocation != null || this.tagFilterLocation != null);
    }

    // --------Getter & Setter--------

    public ResourceLocation getItemFilterLocation() {
        return itemFilterLocation;
    }
    public ResourceLocation getTagFilterLocation() {
        return tagFilterLocation;
    }

    public void setItemFilterLocation(ResourceLocation itemFilterLocation) {
        this.itemFilterLocation = itemFilterLocation;
    }
    public void setTagFilterLocation(ResourceLocation tagFilterLocation) {
        this.tagFilterLocation = tagFilterLocation;
    }
}