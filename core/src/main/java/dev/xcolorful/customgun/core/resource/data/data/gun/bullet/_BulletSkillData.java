package dev.xcolorful.customgun.core.resource.data.data.gun.bullet;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.data.data.gun.bullet._BulletSkillDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;
import dev.xcolorful.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class _BulletSkillData extends ResourcePojo<_BulletSkillData> {

    private float armorIgnorePercent = 0.0F;
    private float headshotMultiplier = 1.0F;
    private List<_DistanceDamageData> damageCalculation;

    private static final _BulletSkillData PARSER = new _BulletSkillData();
    public static _BulletSkillData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected _BulletSkillData fromJsonReader(JsonReader reader) throws IOException {
        _BulletSkillData pojo = new _BulletSkillData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _BulletSkillDataTag.ARMOR_IGNORE_PERCENT, _BulletSkillDataTag.ARMOR_IGNORE_PERCENT_OLD1 -> pojo.armorIgnorePercent = JsonUtils.readFloat(reader);
                    case _BulletSkillDataTag.HEADSHOT_MULTIPLIER, _BulletSkillDataTag.HEADSHOT_MULTIPLIER_OLD1 -> pojo.headshotMultiplier = JsonUtils.readFloat(reader);
                    case _BulletSkillDataTag.DAMAGE_CALCULATION, _BulletSkillDataTag.DAMAGE_CALCULATION_OLD1 -> pojo.damageCalculation = JsonUtils.readList(reader, _DistanceDamageData::fromJson);
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

    public static void toJson(JsonWriter writer, _BulletSkillData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _BulletSkillDataTag.ARMOR_IGNORE_PERCENT, this.armorIgnorePercent);
            JsonUtils.writeFloat(writer, _BulletSkillDataTag.HEADSHOT_MULTIPLIER, this.headshotMultiplier);
            JsonUtils.writeList(writer, _BulletSkillDataTag.DAMAGE_CALCULATION, this.damageCalculation, _DistanceDamageData::toJson);
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
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.damageCalculation == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        int size = this.damageCalculation.size();
        for (int i = 0; i < size; i++) {
            var data = this.damageCalculation.get(i);
            data.validate();
            if (!data.isValid()) {
                this.setValid(false);
                return;
            }
        }

        this.setValid(true);
    }

    @Override
    protected _BulletSkillData copyTo(final _BulletSkillData pojo) {
        pojo.setArmorIgnorePercent(this.armorIgnorePercent);
        pojo.setHeadshotMultiplier(this.headshotMultiplier);
        List<_DistanceDamageData> damageCalculation = new ArrayList<>(this.damageCalculation.size()); {
            for (int i = 0; i < this.damageCalculation.size(); i++) {
                damageCalculation.add(this.damageCalculation.get(i).copy());
            }
        } pojo.setDamageCalculation(damageCalculation);
        return pojo;
    }

    @Override
    public _BulletSkillData copy() {
        _BulletSkillData pojo = copyTo(new _BulletSkillData());
        pojo.setValid(this.isValid());
        return pojo;
    }

    // --------Getter & Setter--------

    public float getArmorIgnorePercent() {
        return armorIgnorePercent;
    }
    public float getHeadshotMultiplier() {
        return headshotMultiplier;
    }
    public List<_DistanceDamageData> getDamageCalculation() {
        return damageCalculation;
    }

    public void setArmorIgnorePercent(float armorIgnorePercent) {
        this.armorIgnorePercent = armorIgnorePercent;
    }
    public void setHeadshotMultiplier(float headshotMultiplier) {
        this.headshotMultiplier = headshotMultiplier;
    }
    public void setDamageCalculation(List<_DistanceDamageData> damageCalculation) {
        this.damageCalculation = damageCalculation;
    }

    // --------Back compatibility--------

    @Override
    public _BulletSkillData applyBackCompatibility() {
        if (this.damageCalculation == null) this.damageCalculation = new ArrayList<>();
        else {
            int size = this.damageCalculation.size();
            for (int i = 0; i < size; i++) this.damageCalculation.get(i).applyBackCompatibility();
        }
        return this;
    }
}