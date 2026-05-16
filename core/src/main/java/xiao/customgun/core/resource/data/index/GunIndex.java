/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.index;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.item.gun.GunCategory;
import xiao.customgun.core.api.resource.data.index.GunIndexTag;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class GunIndex extends _DataIndex<GunIndex> {

    private GunCategory gunCategory;

    /**
     * ItemStack类型
     */
    private String itemType;

    private static final GunIndex PARSER = new GunIndex();
    public static GunIndex fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected GunIndex fromJsonReader(JsonReader reader) throws IOException {
        GunIndex pojo = new GunIndex();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case GunIndexTag.NAME_LANG, GunIndexTag.NAME_LANG_OLD1 -> pojo.setNameLang(JsonUtils.readString(reader));
                    case GunIndexTag.TOOLTIP_LANG, GunIndexTag.TOOLTIP_LANG_OLD1 -> pojo.setTooltipLang(JsonUtils.readString(reader));
                    case GunIndexTag.DATA_LOCATION, GunIndexTag.DATA_LOCATION_OLD1 -> pojo.setDataLocation(JsonUtils.readResourceLocation(reader));
                    case GunIndexTag.DISPLAY_INDEX_LOCATION, GunIndexTag.DISPLAY_INDEX_LOCATION_OLD1 -> pojo.setDisplayIndexLocation(JsonUtils.readResourceLocation(reader));
                    case GunIndexTag.SLOT_SORT, GunIndexTag.SLOT_SORT_OLD1 -> pojo.setSlotSort(JsonUtils.readInt(reader));

                    case GunIndexTag.GUN_CATEGORY, GunIndexTag.GUN_CATEGORY_OLD1 -> pojo.gunCategory = JsonUtils.readFromString(reader, GunCategory::fromString);
                    case GunIndexTag.ITEM_TYPE -> pojo.itemType = JsonUtils.readString(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, GunIndex pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeString(writer, GunIndexTag.NAME_LANG, this.getNameLang());
            JsonUtils.writeString(writer, GunIndexTag.TOOLTIP_LANG, this.getTooltipLang());
            JsonUtils.writeResourceLocation(writer, GunIndexTag.DATA_LOCATION, this.getDataLocation());
            JsonUtils.writeResourceLocation(writer, GunIndexTag.DISPLAY_INDEX_LOCATION, this.getDisplayIndexLocation());
            JsonUtils.writeInt(writer, GunIndexTag.SLOT_SORT, this.getSlotSort());

            JsonUtils.writeToString(writer, GunIndexTag.GUN_CATEGORY, this.gunCategory);
            JsonUtils.writeString(writer, GunIndexTag.ITEM_TYPE, this.itemType);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public GunCategory getGunCategory() {
        return gunCategory;
    }
    public String getItemType() {
        return itemType;
    }

    public void setGunCategory(GunCategory gunCategory) {
        this.gunCategory = gunCategory;
    }
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
}