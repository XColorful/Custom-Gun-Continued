/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.core.api.resource.data.data.gun._BulletDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet._BulletSkillData;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet._ExplosionData;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;
import dev.xcolorful.customgun.core.util.JsonUtils;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.List;

public final class _BulletData extends ResourcePojo<_BulletData> {

    // 显示数值
    private float displayDamage = 5f;
    private _BulletSkillData bulletSkillData;

    // 子弹飞行参数
    private float lifetimeSeconds = 10f;
    private float bulletSpeed = 5f;
    private float gravity = 0f;
    private float friction = 0.01f;

    // 射击效果
    private int bulletSplitAmount = 1; // 子弹分裂数 (霰弹枪)
    private int pierceCount = 1; // 穿透数
    private int tracerInterval = -1; // 发射子弹为曳光弹的间隔

    // 命中效果
    private boolean fireAspect = false; // 火焰附加
    private int fireAspectSeconds = 2;
    private float knockbackStrength = 0;
    private _ExplosionData explosionData;

    private static final _BulletData PARSER = new _BulletData();
    public static _BulletData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }

    @Override
    protected _BulletData fromJsonReader(JsonReader reader) throws IOException {
        _BulletData pojo = new _BulletData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case _BulletDataTag.DISPLAY_DAMAGE, _BulletDataTag.DISPLAY_DAMAGE_OLD1 -> pojo.displayDamage = JsonUtils.readFloat(reader);
                    case _BulletDataTag.BULLET_SKILL -> pojo.bulletSkillData = JsonUtils.read(reader, _BulletSkillData::fromJson); case _BulletDataTag.BULLET_SKILL_OLD1 -> pojo.bulletSkillDataOld = JsonUtils.read(reader, _BulletSkillData::fromJson);

                    case _BulletDataTag.LIFETIME_SECONDS, _BulletDataTag.LIFETIME_SECONDS_OLD1 -> pojo.lifetimeSeconds = JsonUtils.readFloat(reader);
                    case _BulletDataTag.BULLET_SPEED, _BulletDataTag.BULLET_SPEED_OLD1 -> pojo.bulletSpeed = JsonUtils.readFloat(reader);
                    case _BulletDataTag.GRAVITY -> pojo.gravity = JsonUtils.readFloat(reader);
                    case _BulletDataTag.FRICTION -> pojo.friction = JsonUtils.readFloat(reader);

                    case _BulletDataTag.BULLET_SPILT_AMOUNT, _BulletDataTag.BULLET_SPILT_AMOUNT_OLD1 -> pojo.bulletSplitAmount = JsonUtils.readInt(reader);
                    case _BulletDataTag.PIERCE_COUNT, _BulletDataTag.PIERCE_COUNT_OLD1 -> pojo.pierceCount = JsonUtils.readInt(reader);
                    case _BulletDataTag.TRACER_INTERVAL, _BulletDataTag.TRACER_INTERVAL_OLD1 -> pojo.tracerInterval = JsonUtils.readInt(reader);

                    case _BulletDataTag.FIRE_ASPECT, _BulletDataTag.FIRE_ASPECT_OLD1 -> pojo.fireAspect = JsonUtils.readBoolean(reader);
                    case _BulletDataTag.FIRE_ASPECT_SECONDS, _BulletDataTag.FIRE_ASPECT_SECONDS_OLD1 -> pojo.fireAspectSeconds = JsonUtils.readInt(reader);
                    case _BulletDataTag.KNOCKBACK_STRENGTH, _BulletDataTag.KNOCKBACK_STRENGTH_OLD1 -> pojo.knockbackStrength = JsonUtils.readFloat(reader);
                    case _BulletDataTag.BULLET_EXPLOSION, _BulletDataTag.BULLET_EXPLOSION_OLD1 -> pojo.explosionData = JsonUtils.read(reader, _ExplosionData::fromJson);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, _BulletData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }

    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeFloat(writer, _BulletDataTag.DISPLAY_DAMAGE, displayDamage);
            JsonUtils.write(writer, _BulletDataTag.BULLET_SKILL, bulletSkillData, _BulletSkillData::toJson);

            JsonUtils.writeFloat(writer, _BulletDataTag.LIFETIME_SECONDS, lifetimeSeconds);
            JsonUtils.writeFloat(writer, _BulletDataTag.BULLET_SPEED, bulletSpeed);
            JsonUtils.writeFloat(writer, _BulletDataTag.GRAVITY, gravity);
            JsonUtils.writeFloat(writer, _BulletDataTag.FRICTION, friction);

            JsonUtils.writeInt(writer, _BulletDataTag.BULLET_SPILT_AMOUNT, bulletSplitAmount);
            JsonUtils.writeInt(writer, _BulletDataTag.PIERCE_COUNT, pierceCount);
            JsonUtils.writeInt(writer, _BulletDataTag.TRACER_INTERVAL, tracerInterval);

            JsonUtils.writeBoolean(writer, _BulletDataTag.FIRE_ASPECT, fireAspect);
            JsonUtils.writeInt(writer, _BulletDataTag.FIRE_ASPECT_SECONDS, fireAspectSeconds);
            JsonUtils.writeFloat(writer, _BulletDataTag.KNOCKBACK_STRENGTH, knockbackStrength);
            JsonUtils.write(writer, _BulletDataTag.BULLET_EXPLOSION, explosionData, _ExplosionData::toJson);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();

        boolean n1 = (this.bulletSkillData == null | this.explosionData == null);
        if (n1) {
            this.setValid(false);
            return;
        }
        this.bulletSkillData.validate();
        this.explosionData.validate();
        boolean v1 = (this.bulletSkillData.isValid() & this.explosionData.isValid());
        if (!(v1)) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public float getDisplayDamage() {
        return displayDamage;
    }
    public _BulletSkillData getBulletSkillData() {
        return bulletSkillData;
    }
    public float getLifetimeSeconds() {
        return lifetimeSeconds;
    }
    public float getBulletSpeed() {
        return bulletSpeed;
    }
    public float getGravity() {
        return gravity;
    }
    public float getFriction() {
        return friction;
    }
    public int getBulletSplitAmount() {
        return bulletSplitAmount;
    }
    public int getPierceCount() {
        return pierceCount;
    }
    public int getTracerInterval() {
        return tracerInterval;
    }
    public boolean isFireAspect() {
        return fireAspect;
    }
    public int getFireAspectSeconds() {
        return fireAspectSeconds;
    }
    public float getKnockbackStrength() {
        return knockbackStrength;
    }
    public _ExplosionData getExplosionData() {
        return explosionData;
    }

    public void setDisplayDamage(float displayDamage) {
        this.displayDamage = displayDamage;
    }
    public void setBulletSkillData(_BulletSkillData bulletSkillData) {
        this.bulletSkillData = bulletSkillData;
    }
    public void setLifetimeSeconds(float lifetimeSeconds) {
        this.lifetimeSeconds = lifetimeSeconds;
    }
    public void setBulletSpeed(float bulletSpeed) {
        this.bulletSpeed = bulletSpeed;
    }
    public void setGravity(float gravity) {
        this.gravity = gravity;
    }
    public void setFriction(float friction) {
        this.friction = friction;
    }
    public void setBulletSplitAmount(int bulletSplitAmount) {
        this.bulletSplitAmount = bulletSplitAmount;
    }
    public void setPierceCount(int pierceCount) {
        this.pierceCount = pierceCount;
    }
    public void setTracerInterval(int tracerInterval) {
        this.tracerInterval = tracerInterval;
    }
    public void setFireAspect(boolean fireAspect) {
        this.fireAspect = fireAspect;
    }
    public void setFireAspectSeconds(int fireAspectSeconds) {
        this.fireAspectSeconds = fireAspectSeconds;
    }
    public void setKnockbackStrength(float knockbackStrength) {
        this.knockbackStrength = knockbackStrength;
    }
    public void setExplosionData(_ExplosionData explosionData) {
        this.explosionData = explosionData;
    }

    // --------Back compatibility--------

    @ApiStatus.Internal
    private _BulletSkillData bulletSkillDataOld;

    @Override
    public _BulletData applyBackCompatibility() {
        { // bulletSkillData兼容
            if (bulletSkillDataOld != null & this.bulletSkillData == null) { // 只写了旧格式
                this.bulletSkillData = this.bulletSkillDataOld;
            }

            this.bulletSkillData = this.bulletSkillData == null ? new _BulletSkillData().applyBackCompatibility() : this.bulletSkillData.applyBackCompatibility();

            if (this.bulletSplitAmount > 1 & this.bulletSkillDataOld != null) {
                /*
                旧格式是子弹伤害写总的，运行时除掉split
                新格式直接指定单个子弹伤害，运行时生成split个gun projectile
                 */
                assert this.bulletSkillData != null;
                this.displayDamage /= (float) this.bulletSplitAmount;
                List<_DistanceDamageData> damageCalculation = this.bulletSkillData.getDamageCalculation();
                for (int i = 0; i < damageCalculation.size(); i++) {
                    _DistanceDamageData distanceDamageData = damageCalculation.get(i);

                    // 对(霰弹枪)分裂子弹提前除掉伤害
                    distanceDamageData.setDamage(distanceDamageData.getDamage() / this.bulletSplitAmount);
                }
                this.bulletSkillData.setDamageCalculation(damageCalculation);
            }
            this.bulletSkillDataOld = null;
        }

        this.explosionData = this.explosionData == null ? new _ExplosionData().applyBackCompatibility() : this.explosionData.applyBackCompatibility();
        return this;
    }
}