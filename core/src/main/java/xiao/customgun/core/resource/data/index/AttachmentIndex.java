/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.index;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.resource.data.index.AttachmentIndexTag;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class AttachmentIndex extends _DataIndex<AttachmentIndex> {

    private AttachmentCategory attachmentCategory;

    /**
     * 从创造模式物品栏和 JEI 中隐藏
     */
    private boolean hideInGame = false;

    private static final AttachmentIndex PARSER = new AttachmentIndex();
    public static AttachmentIndex fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected AttachmentIndex fromJsonReader(JsonReader reader) throws IOException {
        AttachmentIndex pojo = new AttachmentIndex();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case AttachmentIndexTag.NAME_LANG, AttachmentIndexTag.NAME_LANG_OLD1 -> pojo.setNameLang(JsonUtils.readString(reader));
                    case AttachmentIndexTag.TOOLTIP_LANG, AttachmentIndexTag.TOOLTIP_LANG_OLD1 -> pojo.setTooltipLang(JsonUtils.readString(reader));
                    case AttachmentIndexTag.DATA_LOCATION, AttachmentIndexTag.DATA_LOCATION_OLD1 -> pojo.setDataLocation(JsonUtils.readResourceLocation(reader));
                    case AttachmentIndexTag.DISPLAY_INDEX_LOCATION, AttachmentIndexTag.DISPLAY_INDEX_LOCATION_OLD1 -> pojo.setDisplayIndexLocation(JsonUtils.readResourceLocation(reader));
                    case AttachmentIndexTag.SLOT_SORT, AttachmentIndexTag.SLOT_SORT_OLD1 -> pojo.setSlotSort(JsonUtils.readInt(reader));

                    case AttachmentIndexTag.ATTACHMENT_CATEGORY, AttachmentIndexTag.ATTACHMENT_CATEGORY_OLD1 -> pojo.attachmentCategory = JsonUtils.readFromString(reader, AttachmentCategory::fromString);
                    case AttachmentIndexTag.HIDE_IN_GAME, AttachmentIndexTag.HIDE_IN_GAME_OLD1 -> pojo.hideInGame = JsonUtils.readBoolean(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, AttachmentIndex pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
        else writer.nullValue();
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeString(writer, AttachmentIndexTag.NAME_LANG, this.getNameLang());
            JsonUtils.writeString(writer, AttachmentIndexTag.TOOLTIP_LANG, this.getTooltipLang());
            JsonUtils.writeResourceLocation(writer, AttachmentIndexTag.DATA_LOCATION, this.getDataLocation());
            JsonUtils.writeResourceLocation(writer, AttachmentIndexTag.DISPLAY_INDEX_LOCATION, this.getDisplayIndexLocation());
            JsonUtils.writeInt(writer, AttachmentIndexTag.SLOT_SORT, this.getSlotSort());

            JsonUtils.writeToString(writer, AttachmentIndexTag.ATTACHMENT_CATEGORY, this.attachmentCategory);
            JsonUtils.writeBoolean(writer, AttachmentIndexTag.HIDE_IN_GAME, this.hideInGame);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public AttachmentCategory getAttachmentCategory() {
        return attachmentCategory;
    }
    public boolean isHideInGame() {
        return hideInGame;
    }

    public void setAttachmentCategory(AttachmentCategory attachmentCategory) {
        this.attachmentCategory = attachmentCategory;
    }
    public void setHideInGame(boolean hideInGame) {
        this.hideInGame = hideInGame;
    }
}