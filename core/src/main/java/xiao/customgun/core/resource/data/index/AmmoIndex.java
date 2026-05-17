/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.index;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.index.AmmoIndexTag;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class AmmoIndex extends _DataIndex<AmmoIndex> {

    /**
     * 物品堆叠数量
     */
    private int maxStackSize = 1;

    private static final AmmoIndex PARSER = new AmmoIndex();
    public static AmmoIndex fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected AmmoIndex fromJsonReader(JsonReader reader) throws IOException {
        AmmoIndex pojo = new AmmoIndex();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case AmmoIndexTag.NAME_LANG, AmmoIndexTag.NAME_LANG_OLD1 -> pojo.setNameLang(JsonUtils.readString(reader));
                    case AmmoIndexTag.TOOLTIP_LANG, AmmoIndexTag.TOOLTIP_LANG_OLD1 -> pojo.setTooltipLang(JsonUtils.readString(reader));
                    case AmmoIndexTag.DISPLAY_INDEX_LOCATION, AmmoIndexTag.DISPLAY_INDEX_LOCATION_OLD1 -> pojo.setDisplayIndexLocation(JsonUtils.readResourceLocation(reader));
                    case AmmoIndexTag.SLOT_SORT, AmmoIndexTag.SLOT_SORT_OLD1 -> pojo.setSlotSort(JsonUtils.readInt(reader));

                    case AmmoIndexTag.MAX_STACK_SIZE, AmmoIndexTag.MAX_STACK_SIZE_OLD1 -> pojo.maxStackSize = JsonUtils.readInt(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, AmmoIndex pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
        else writer.nullValue();
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeString(writer, AmmoIndexTag.NAME_LANG, this.getNameLang());
            JsonUtils.writeString(writer, AmmoIndexTag.TOOLTIP_LANG, this.getTooltipLang());
            JsonUtils.writeResourceLocation(writer, AmmoIndexTag.DISPLAY_INDEX_LOCATION, this.getDisplayIndexLocation());
            JsonUtils.writeInt(writer, AmmoIndexTag.SLOT_SORT, this.getSlotSort());

            JsonUtils.writeInt(writer, AmmoIndexTag.MAX_STACK_SIZE, this.maxStackSize);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
    }
}