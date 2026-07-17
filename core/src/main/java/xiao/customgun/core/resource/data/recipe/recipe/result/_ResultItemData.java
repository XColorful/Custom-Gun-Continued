package xiao.customgun.core.resource.data.recipe.recipe.result;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import xiao.customgun.core.api.resource.data.recipe.recipe.result._ResultItemDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

/**
 * 手动解析 1.20.1 原版{@link CompoundTag#CODEC parse}格式，不属于 TaCZ
 * 即不支持 1.21.1 组件系统，但是高版本实际也只用了 CustomData，从而换取全版本兼容的格式
 */
public class _ResultItemData extends ResourcePojo<_ResultItemData> {

    private ResourceLocation itemLocation;
    private CompoundTag itemNbt;

    private static final _ResultItemData PARSER = new _ResultItemData();
    public static _ResultItemData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _ResultItemData fromJsonReader(JsonReader reader) throws IOException {
        _ResultItemData pojo = new _ResultItemData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _ResultItemDataTag.ITEM_LOCATION, _ResultItemDataTag.ITEM_LOCATION_OLD1 -> pojo.itemLocation = JsonUtils.readResourceLocation(reader);
                    case _ResultItemDataTag.ITEM_NBT, _ResultItemDataTag.ITEM_NBT_OLD1 -> pojo.itemNbt = JsonUtils.readNbt(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _ResultItemData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, _ResultItemDataTag.ITEM_LOCATION, itemLocation);
            JsonUtils.writeNbt(writer, _ResultItemDataTag.ITEM_NBT, itemNbt);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.itemLocation == null | this.itemNbt == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public ResourceLocation getItemLocation() {
        return itemLocation;
    }
    public CompoundTag getItemNbt() {
        return itemNbt;
    }

    public void setItemLocation(ResourceLocation itemLocation) {
        this.itemLocation = itemLocation;
    }
    public void setItemNbt(CompoundTag itemNbt) {
        this.itemNbt = itemNbt;
    }

    // --------Back compatibility--------

    @Override
    public _ResultItemData applyBackCompatibility() {
        if (this.itemNbt == null) this.itemNbt = new CompoundTag();
        return this;
    }
}
