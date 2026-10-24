package dev.xcolorful.customgun.core.resource.data.index;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.api.resource.data.index.BlockIndexTag;
import dev.xcolorful.customgun.core.util.JsonUtils;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public final class BlockIndex extends _DataIndex<BlockIndex> {

    /**
     * Block类型
     */
    private ResourceLocation blockLocation;

    /**
     * 物品堆叠数量
     */
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
                    case BlockIndexTag.NAME_LANG, BlockIndexTag.NAME_LANG_OLD1 -> pojo.setNameLang(JsonUtils.readString(reader));
                    case BlockIndexTag.TOOLTIP_LANG, BlockIndexTag.TOOLTIP_LANG_OLD1 -> pojo.setTooltipLang(JsonUtils.readString(reader));
                    case BlockIndexTag.DATA_LOCATION, BlockIndexTag.DATA_LOCATION_OLD1 -> pojo.setDataLocation(JsonUtils.readResourceLocation(reader));
                    case BlockIndexTag.DISPLAY_INDEX_LOCATION, BlockIndexTag.DISPLAY_INDEX_LOCATION_OLD1 -> pojo.setDisplayIndexLocation(JsonUtils.readResourceLocation(reader));
                    case BlockIndexTag.SLOT_SORT, BlockIndexTag.SLOT_SORT_OLD1 -> pojo.setSlotSort(JsonUtils.readInt(reader));
                    case BlockIndexTag.HIDE_IN_GAME, BlockIndexTag.HIDE_IN_GAME_OLD1 -> pojo.setHideInGame(JsonUtils.readBoolean(reader));

                    case BlockIndexTag.BLOCK_LOCATION, BlockIndexTag.BLOCK_LOCATION_OLD1 -> pojo.blockLocation = JsonUtils.readResourceLocation(reader);
                    case BlockIndexTag.MAX_STACK_SIZE, BlockIndexTag.MAX_STACK_SIZE_OLD1 -> pojo.maxStackSize = JsonUtils.readInt(reader);
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
            JsonUtils.writeBoolean(writer, BlockIndexTag.HIDE_IN_GAME, this.getHideInGame());

            JsonUtils.writeResourceLocation(writer, BlockIndexTag.BLOCK_LOCATION, this.blockLocation);
            JsonUtils.writeInt(writer, BlockIndexTag.MAX_STACK_SIZE, this.maxStackSize);
            writeCustomValue(writer);
        }
        writer.endObject();
    }
    @Override
    protected void writeCustomValue(JsonWriter writer) {
        super.writeCustomValue(writer);
    }

    @Override
    protected void validatePojo() {
        super.validatePojo();
        if (!this.isValid()) return;

        boolean n1 = (this.blockLocation == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    @Override
    protected BlockIndex copyTo(final BlockIndex pojo) {
        super.copyTo(pojo);

        pojo.setBlockLocation(this.blockLocation);

        pojo.setMaxStackSize(this.maxStackSize);
        return pojo;
    }

    @Override
    public BlockIndex copy() {
        BlockIndex pojo = copyTo(new BlockIndex());
        pojo.setValid(this.isValid());
        return pojo;
    }

    // --------Getter & Setter--------

    public ResourceLocation getBlockLocation() {
        return blockLocation;
    }
    public int getMaxStackSize() {
        return maxStackSize;
    }

    public void setBlockLocation(ResourceLocation blockLocation) {
        this.blockLocation = blockLocation;
    }
    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
    }

    // --------Back compatibility--------

    @Override
    public BlockIndex applyBackCompatibility() {
        super.applyBackCompatibility();
        this.blockLocation = this.blockLocation == null ? ResourceTag.NULL_LOCATION : this.blockLocation;
        return this;
    }
}