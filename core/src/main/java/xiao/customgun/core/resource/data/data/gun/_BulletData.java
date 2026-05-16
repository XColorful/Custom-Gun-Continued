/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.data.gun;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import xiao.customgun.core.api.resource.data.data.gun._BulletDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.gun.bullet._BulletSkillData;
import xiao.customgun.core.resource.data.data.gun.bullet._ExplosionData;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public class _BulletData extends ResourcePojo<_BulletData> {

    // 显示数值
    private float displayDamage = 5f;
    private _BulletSkillData bulletSkillData;

    // 子弹飞行参数
    private float lifetimeSeconds = 10f;
    private float bulletSpeed = 5f;
    private float gravity = 0f;
    private float friction = 0.01f;

    // 射击效果
    private int bulletAmount = 1; // 子弹分裂数 (霰弹枪)
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
                    case _BulletDataTag.BULLET_SKILL, _BulletDataTag.BULLET_SKILL_OLD1 -> pojo.bulletSkillData = JsonUtils.read(reader, _BulletSkillData::fromJson);

                    case _BulletDataTag.LIFETIME_SECONDS, _BulletDataTag.LIFETIME_SECONDS_OLD1 -> pojo.lifetimeSeconds = JsonUtils.readFloat(reader);
                    case _BulletDataTag.BULLET_SPEED, _BulletDataTag.BULLET_SPEED_OLD1 -> pojo.bulletSpeed = JsonUtils.readFloat(reader);
                    case _BulletDataTag.GRAVITY -> pojo.gravity = JsonUtils.readFloat(reader);
                    case _BulletDataTag.FRICTION -> pojo.friction = JsonUtils.readFloat(reader);

                    case _BulletDataTag.BULLET_SPILT_AMOUNT, _BulletDataTag.BULLET_SPILT_AMOUNT_OLD1 -> pojo.bulletAmount = JsonUtils.readInt(reader);
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

            JsonUtils.writeInt(writer, _BulletDataTag.BULLET_SPILT_AMOUNT, bulletAmount);
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
    public int getBulletAmount() {
        return bulletAmount;
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
    public void setBulletAmount(int bulletAmount) {
        this.bulletAmount = bulletAmount;
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
}