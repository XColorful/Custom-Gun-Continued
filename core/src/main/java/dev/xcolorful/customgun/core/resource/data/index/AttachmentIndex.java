package dev.xcolorful.customgun.core.resource.data.index;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.resource.data.index.AttachmentIndexTag;
import dev.xcolorful.customgun.core.util.JsonUtils;

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
                    default -> this.readCustomKey(reader, key);
                }
            }
        }
        reader.endObject();
        return pojo;
    }
    @Override
    protected boolean readCustomKey(JsonReader reader, String key) throws IOException {
        return super.readCustomKey(reader, key);
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
        super.validatePojo();
        if (!this.isValid()) return;

        boolean n1 = (this.attachmentCategory == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    @Override
    protected AttachmentIndex copyTo(final AttachmentIndex pojo) {
        super.copyTo(pojo);

        pojo.setAttachmentCategory(this.attachmentCategory);

        pojo.setHideInGame(this.hideInGame);
        return pojo;
    }

    @Override
    public AttachmentIndex copy() {
        AttachmentIndex pojo = copyTo(new AttachmentIndex());
        pojo.setValid(this.isValid());
        return pojo;
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

    // --------Back compatibility--------

    @Override
    public AttachmentIndex applyBackCompatibility() {
        super.applyBackCompatibility();
        return this;
    }
}