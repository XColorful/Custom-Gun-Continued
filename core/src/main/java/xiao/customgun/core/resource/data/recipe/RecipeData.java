/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.recipe;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.core.api.minecraft.recipe.CustomRecipeType;
import xiao.customgun.core.api.resource.data.recipe.RecipeDataTag;
import xiao.customgun.core.init.registry.ModRecipe;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.recipe.recipe._TableIngredientData;
import xiao.customgun.core.resource.data.recipe.recipe._TableResultData;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.List;

public final class RecipeData extends ResourcePojo<RecipeData> {

    /**
     * 原版根据这个字段选择 {@link ModRecipe} 中注册的序列化方式, 注册id为 {@link CustomRecipeType}
     * 只在 toJson 的时候需要用
     */
    @ApiStatus.Internal
    private String recipeRegistryType;
    private List<_TableIngredientData> tableIngredients;
    private _TableResultData tableResult;

    private static final RecipeData PARSER = new RecipeData();
    public static RecipeData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected RecipeData fromJsonReader(JsonReader reader) throws IOException {
        RecipeData pojo = new RecipeData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case RecipeDataTag.RECIPE_REGISTRY_TYPE -> pojo.recipeRegistryType = JsonUtils.readString(reader);
                    case RecipeDataTag.TABLE_INGREDIENTS, RecipeDataTag.TABLE_INGREDIENTS_OLD1 -> pojo.tableIngredients = JsonUtils.readList(reader, _TableIngredientData::fromJson);
                    case RecipeDataTag.TABLE_RESULT, RecipeDataTag.TABLE_RESULT_OLD1 -> pojo.tableResult = JsonUtils.read(reader, _TableResultData::fromJson);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, RecipeData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }

    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeToString(writer, RecipeDataTag.RECIPE_REGISTRY_TYPE, recipeRegistryType);
            JsonUtils.writeList(writer, RecipeDataTag.TABLE_INGREDIENTS, tableIngredients, _TableIngredientData::toJson);
            JsonUtils.write(writer, RecipeDataTag.TABLE_RESULT, tableResult, _TableResultData::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    @ApiStatus.Internal
    public String getRecipeRegistryType() {
        return recipeRegistryType;
    }
    public List<_TableIngredientData> getTableIngredients() {
        return tableIngredients;
    }
    public _TableResultData getTableResult() {
        return tableResult;
    }

    /**
     * 仅对手动 toJson 有效
     * 目前想到的用途就是自动检测并更正配置
     */
    @ApiStatus.Internal
    public void setRecipeRegistryType(String recipeRegistryType) {
        this.recipeRegistryType = recipeRegistryType;
    }
    public void setTableIngredients(List<_TableIngredientData> tableIngredients) {
        this.tableIngredients = tableIngredients;
    }
    public void setTableResult(_TableResultData tableResult) {
        this.tableResult = tableResult;
    }
}