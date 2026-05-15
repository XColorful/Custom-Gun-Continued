/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.index;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.Identifier;
import xiao.customgun.core.api.resource.data.index.BlockIndexTag;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class BlockIndex extends _DataIndex<BlockIndex> {

    private Identifier blockType;
    private int maxStackSize = 1;

    private static final BlockIndex PARSER = new BlockIndex();
    public static BlockIndex fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected BlockIndex fromJsonReader(JsonReader reader) throws IOException {
        BlockIndex pojo = new BlockIndex();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case BlockIndexTag.NAME_LANG -> pojo.setNameLang(JsonUtils.readString(reader));
                    case BlockIndexTag.TOOLTIP_LANG -> pojo.setTooltipLang(JsonUtils.readString(reader));
                    case BlockIndexTag.DATA_LOCATION -> pojo.setDataLocation(JsonUtils.readResourceLocation(reader));
                    case BlockIndexTag.DISPLAY_INDEX_LOCATION -> pojo.setDisplayIndexLocation(JsonUtils.readResourceLocation(reader));
                    case BlockIndexTag.SLOT_SORT -> pojo.setSlotSort(JsonUtils.readInt(reader));

                    case BlockIndexTag.BLOCK_TYPE -> pojo.blockType = JsonUtils.readResourceLocation(reader);
                    case BlockIndexTag.MAX_STACK_SIZE -> pojo.maxStackSize = JsonUtils.readInt(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, BlockIndex pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
        else writer.nullValue();
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeString(writer, BlockIndexTag.NAME_LANG, this.getNameLang());
            JsonUtils.writeString(writer, BlockIndexTag.TOOLTIP_LANG, this.getTooltipLang());
            JsonUtils.writeResourceLocation(writer, BlockIndexTag.DATA_LOCATION, this.getDataLocation());
            JsonUtils.writeResourceLocation(writer, BlockIndexTag.DISPLAY_INDEX_LOCATION, this.getDisplayIndexLocation());
            JsonUtils.writeInt(writer, BlockIndexTag.SLOT_SORT, this.getSlotSort());

            JsonUtils.writeResourceLocation(writer, BlockIndexTag.BLOCK_TYPE, this.blockType);
            JsonUtils.writeInt(writer, BlockIndexTag.MAX_STACK_SIZE, this.maxStackSize);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public Identifier getBlockType() {
        return blockType;
    }
    public int getMaxStackSize() {
        return maxStackSize;
    }

    public void setBlockType(Identifier blockType) {
        this.blockType = blockType;
    }
    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
    }
}