/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.recipe.recipe;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.recipe.recipe._TableIngredientDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.recipe.recipe.ingredient._IngredientFilterData;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _TableIngredientData extends ResourcePojo<_TableIngredientData> {

    private _IngredientFilterData ingredientFilter;
    private int ingredientCount = 1;

    private static final _TableIngredientData PARSER = new _TableIngredientData();
    public static _TableIngredientData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _TableIngredientData fromJsonReader(JsonReader reader) throws IOException {
        _TableIngredientData pojo = new _TableIngredientData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _TableIngredientDataTag.INGREDIENT_FILTER_DATA, _TableIngredientDataTag.INGREDIENT_FILTER_DATA_OLD1 -> pojo.ingredientFilter = JsonUtils.read(reader, _IngredientFilterData::fromJson);
                    case _TableIngredientDataTag.INGREDIENT_COUNT, _TableIngredientDataTag.INGREDIENT_COUNT_OLD1 -> pojo.ingredientCount = JsonUtils.readInt(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _TableIngredientData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.write(writer, _TableIngredientDataTag.INGREDIENT_FILTER_DATA, this.ingredientFilter, _IngredientFilterData::toJson);
            JsonUtils.writeInt(writer, _TableIngredientDataTag.INGREDIENT_COUNT, this.ingredientCount);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(this.ingredientFilter != null && this.ingredientCount > 0);
    }

    // --------Getter & Setter--------

    public _IngredientFilterData getIngredientFilter() {
        return ingredientFilter;
    }
    public int getIngredientCount() {
        return ingredientCount;
    }

    public void setIngredientFilter(_IngredientFilterData ingredientFilter) {
        this.ingredientFilter = ingredientFilter;
    }
    public void setIngredientCount(int ingredientCount) {
        this.ingredientCount = ingredientCount;
    }
}