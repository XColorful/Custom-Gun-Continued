package dev.xcolorful.customgun.core.resource.data.data.gun.bullet.damage;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.data.data.gun.bullet.damage._DistanceDamageDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class _DistanceDamageData extends ResourcePojo<_DistanceDamageData> {

    private float distance = 0;
    private float damage = 0;

    private static final _DistanceDamageData PARSER = new _DistanceDamageData();
    public static _DistanceDamageData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _DistanceDamageData fromJsonReader(JsonReader reader) throws IOException {
        _DistanceDamageData pojo = new _DistanceDamageData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _DistanceDamageDataTag.DISTANCE -> pojo.distance = JsonUtils.readFloat(reader);
                    case _DistanceDamageDataTag.DAMAGE -> pojo.damage = JsonUtils.readFloat(reader);
                    default -> readUnknownKey(reader, key);
                }
            }
        }
        reader.endObject();
        return pojo;
    }
    @Override
    protected boolean readUnknownKey(JsonReader reader, String key) throws IOException {
        return super.readUnknownKey(reader, key);
    }

    public static void toJson(JsonWriter writer, _DistanceDamageData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _DistanceDamageDataTag.DISTANCE, this.distance);
            JsonUtils.writeFloat(writer, _DistanceDamageDataTag.DAMAGE, this.damage);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    @Override
    protected _DistanceDamageData copyTo(final _DistanceDamageData pojo) {
        pojo.setDistance(this.distance);
        pojo.setDamage(this.damage);
        return pojo;
    }

    @Override
    public _DistanceDamageData copy() {
        _DistanceDamageData pojo = copyTo(new _DistanceDamageData());
        pojo.setValid(this.isValid());
        return pojo;
    }

    // --------Getter & Setter--------

    public float getDistance() {
        return distance;
    }
    public float getDamage() {
        return damage;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
    public void setDamage(float damage) {
        this.damage = damage;
    }

    // --------Back compatibility--------
}