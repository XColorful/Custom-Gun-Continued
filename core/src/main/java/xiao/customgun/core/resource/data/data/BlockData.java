/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;
import xiao.customgun.core.api.resource.data.data.BlockDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.block._RecipeGroupData;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.List;

public final class BlockData extends ResourcePojo<BlockData> {

    private ResourceLocation recipeFilterLocation;
    private List<_RecipeGroupData> recipeGroupList;

    private static final BlockData PARSER = new BlockData();
    public static BlockData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected BlockData fromJsonReader(JsonReader reader) throws IOException {
        BlockData pojo = new BlockData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case BlockDataTag.RECIPE_FILTER_LOCATION -> pojo.recipeFilterLocation = JsonUtils.readResourceLocation(reader);
                    case BlockDataTag.RECIPE_GROUP_LIST -> pojo.recipeGroupList = JsonUtils.readList(reader, _RecipeGroupData::fromJson);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, BlockData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, BlockDataTag.RECIPE_FILTER_LOCATION, this.recipeFilterLocation);
            JsonUtils.writeList(writer, BlockDataTag.RECIPE_GROUP_LIST, this.recipeGroupList, _RecipeGroupData::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public ResourceLocation getRecipeFilterLocation() {
        return recipeFilterLocation;
    }
    public List<_RecipeGroupData> getRecipeGroupList() {
        return recipeGroupList;
    }

    public void setRecipeFilterLocation(ResourceLocation recipeFilterLocation) {
        this.recipeFilterLocation = recipeFilterLocation;
    }
    public void setRecipeGroupList(List<_RecipeGroupData> recipeGroupList) {
        this.recipeGroupList = recipeGroupList;
    }
}