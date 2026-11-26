package dev.xcolorful.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.entity.ShootState;
import dev.xcolorful.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import dev.xcolorful.customgun.core.api.minecraft.attribute.AttributeModifierOperation;
import dev.xcolorful.customgun.core.api.resource.data.data.gun._ShootStateSpeedTag;
import dev.xcolorful.customgun.core.config.SyncConfig;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;

/**
 * 数值语义同 {@link AttributeModifierOperation}，用于配合 {@link ShootState#of} 计算移速
 * <ul>
 *     <li>基础移速用 {@link AttachmentModifierType#WEIGHT}</li>
 *     <li>断疾跑机制用 {@link SyncConfig#ENABLE_SPRINT_WHILE_AIMING}、{@link SyncConfig#ENABLE_SPRINT_WHILE_RELOADING}</li>
 * </ul>
 * @deprecated 再加这个有什么意义吗？单独使部分枪械装弹不断疾跑/装弹时加速？
 */
@Deprecated(forRemoval = true)
public final class _ShootStateSpeed extends ResourcePojo<_ShootStateSpeed> {

    private float base = 0F;
    private float aim = 0F;
    private float reload = 1F;

    private static final _ShootStateSpeed PARSER = new _ShootStateSpeed();
    public static _ShootStateSpeed fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _ShootStateSpeed fromJsonReader(JsonReader reader) throws IOException {
        _ShootStateSpeed pojo = new _ShootStateSpeed();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _ShootStateSpeedTag.BASE -> pojo.base = JsonUtils.readFloat(reader);
                    case _ShootStateSpeedTag.AIM -> pojo.aim = JsonUtils.readFloat(reader);
                    case _ShootStateSpeedTag.RELOAD -> pojo.reload = JsonUtils.readFloat(reader);
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

    public static void toJson(JsonWriter writer, _ShootStateSpeed pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }

    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _ShootStateSpeedTag.BASE, this.base);
            JsonUtils.writeFloat(writer, _ShootStateSpeedTag.AIM, this.aim);
            JsonUtils.writeFloat(writer, _ShootStateSpeedTag.RELOAD, this.reload);
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
        this.setValid(true);
    }

    @Override
    protected _ShootStateSpeed copyTo(final _ShootStateSpeed pojo) {
        pojo.setBase(this.base);
        pojo.setAim(this.aim);
        pojo.setReload(this.reload);
        return pojo;
    }

    @Override
    public _ShootStateSpeed copy() {
        _ShootStateSpeed pojo = copyTo(new _ShootStateSpeed());
        pojo.setValid(this.isValid());
        return pojo;
    }

    // --------Getter & Setter--------

    public float getBase() {
        return base;
    }
    public float getAim() {
        return aim;
    }
    public float getReload() {
        return reload;
    }

    public void setBase(float base) {
        this.base = base;
    }
    public void setAim(float aim) {
        this.aim = aim;
    }
    public void setReload(float reload) {
        this.reload = reload;
    }

    // --------Back compatibility--------
}