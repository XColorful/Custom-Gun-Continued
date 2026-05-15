/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.recipe.recipe;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;
import xiao.customgun.core.api.recipe.RecipeResultType;
import xiao.customgun.core.api.resource.data.recipe.recipe._TableResultDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.index._DataIndex;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _TableResultData extends ResourcePojo<_TableResultData> {

    private RecipeResultType recipeResultType;
    /**
     * 同 {@link _DataIndex#getDataLocation()}
     */
    private ResourceLocation recipeResultLocation;

    private static final _TableResultData PARSER = new _TableResultData();
    public static _TableResultData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _TableResultData fromJsonReader(JsonReader reader) throws IOException {
        _TableResultData pojo = new _TableResultData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _TableResultDataTag.RECIPE_RESULT_TYPE -> pojo.recipeResultType = JsonUtils.readFromString(reader, RecipeResultType::fromString);
                    case _TableResultDataTag.RECIPE_RESULT_LOCATION -> pojo.recipeResultLocation = JsonUtils.readResourceLocation(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _TableResultData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeToString(writer, _TableResultDataTag.RECIPE_RESULT_TYPE, recipeResultType);
            JsonUtils.writeResourceLocation(writer, _TableResultDataTag.RECIPE_RESULT_LOCATION, recipeResultLocation);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public RecipeResultType getRecipeResultType() {
        return recipeResultType;
    }
    public ResourceLocation getRecipeResultLocation() {
        return recipeResultLocation;
    }

    public void setRecipeResultType(RecipeResultType recipeResultType) {
        this.recipeResultType = recipeResultType;
    }
    public void setRecipeResultLocation(ResourceLocation recipeResultLocation) {
        this.recipeResultLocation = recipeResultLocation;
    }
}