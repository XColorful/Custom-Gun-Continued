/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data.recipe.recipe;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.recipe.RecipeResultType;
import dev.xcolorful.customgun.core.api.resource.data.recipe.recipe._TableResultDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.index._DataIndex;
import dev.xcolorful.customgun.core.resource.data.recipe.recipe.result._ResultItemData;
import dev.xcolorful.customgun.core.util.JsonUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public final class _TableResultData extends ResourcePojo<_TableResultData> {

    private RecipeResultType resultType;
    private int resultCount = 1;
    private @Nullable _ResultItemData resultItem;
    private @Nullable ResourceLocation tabGroupLocation;
    /**
     * 同 {@link _DataIndex#getDataLocation()}
     */
    private @Nullable ResourceLocation pojoLocation;

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
                    case _TableResultDataTag.RESULT_TYPE, _TableResultDataTag.RESULT_TYPE_OLD1 -> pojo.resultType = JsonUtils.readFromString(reader, RecipeResultType::fromString);
                    case _TableResultDataTag.RESULT_COUNT, _TableResultDataTag.RESULT_COUNT_OLD1 -> pojo.resultCount = JsonUtils.readInt(reader);
                    case _TableResultDataTag.RESULT_ITEM, _TableResultDataTag.RESULT_ITEM_OLD1 -> pojo.resultItem = JsonUtils.read(reader, _ResultItemData::fromJson);
                    case _TableResultDataTag.TAB_GROUP_LOCATION, _TableResultDataTag.TAB_GROUP_LOCATION_OLD1 -> pojo.tabGroupLocation = JsonUtils.readResourceLocation(reader);
                    case _TableResultDataTag.POJO_LOCATION, _TableResultDataTag.POJO_LOCATION_OLD1 -> pojo.pojoLocation = JsonUtils.readResourceLocation(reader);
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
            JsonUtils.writeToString(writer, _TableResultDataTag.RESULT_TYPE, resultType);
            JsonUtils.writeInt(writer, _TableResultDataTag.RESULT_COUNT, resultCount);
            JsonUtils.write(writer, _TableResultDataTag.RESULT_ITEM, resultItem, _ResultItemData::toJson);
            JsonUtils.writeResourceLocation(writer, _TableResultDataTag.TAB_GROUP_LOCATION, tabGroupLocation);
            JsonUtils.writeResourceLocation(writer, _TableResultDataTag.POJO_LOCATION, pojoLocation);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.resultType == null);
        if (n1) {
            this.setValid(false);
            return;
        }
        if (this.resultItem != null) this.resultItem.validate();
        boolean v1 = (this.resultType != null || this.resultItem.isValid());
        if (!(v1)) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public RecipeResultType getResultType() {
        return resultType;
    }
    public int getResultCount() {
        return resultCount;
    }
    public @Nullable _ResultItemData getResultItem() {
        return resultItem;
    }
    public @Nullable ResourceLocation getTabGroupLocation() {
        return tabGroupLocation;
    }
    public @Nullable ResourceLocation getPojoLocation() {
        return pojoLocation;
    }

    public void setResultType(RecipeResultType resultType) {
        this.resultType = resultType;
    }
    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }
    public void setResultItem(_ResultItemData resultItem) {
        this.resultItem = resultItem;
    }
    public void setTabGroupLocation(ResourceLocation tabGroupLocation) {
        this.tabGroupLocation = tabGroupLocation;
    }
    public void setPojoLocation(ResourceLocation pojoLocation) {
        this.pojoLocation = pojoLocation;
    }

    // --------Back compatibility--------

    @Override
    public _TableResultData applyBackCompatibility() {
        return this;
    }
}