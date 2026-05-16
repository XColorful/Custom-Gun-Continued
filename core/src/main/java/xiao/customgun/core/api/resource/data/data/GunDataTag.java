/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.data;

public class GunDataTag {

    // 枪械属性
    public static final String BULLET_DATA = "bullet_data"; public static final String BULLET_DATA_OLD1 = "bullet";
    public static final String AMMO_TYPE = "ammo_type"; public static final String AMMO_TYPE_OLD1 = "ammo";
    public static final String BOLT_TYPE = "bolt_type"; public static final String BOLT_TYPE_OLD1 = "bolt";

    public static final String RPM = "rpm";
    public static final String INACCURACY_DATA = "inaccuracy_data"; public static final String INACCURACY_DATA_OLD1 = "inaccuracy";
    public static final String RECOIL_DATA = "recoil_data"; public static final String RECOIL_DATA_OLD1 = "recoil";
    public static final String CRAWL_RECOIL_MULTIPLIER = "crawl_recoil_multiplier";

    public static final String WEIGHT = "weight";
    public static final String MOVEMENT_DATA = "movement_data"; public static final String MOVEMENT_DATA_OLD1 = "movement_speed";

    public static final String FIRE_SOUND_DATA = "fire_sound_data"; public static final String FIRE_SOUND_DATA_OLD1 = "fire_sound";
    public static final String HURT_BOB_TWEAK_MULTIPLIER = "hurt_bob_tweak_multiplier";

    public static final String RELOAD_DATA = "reload_data"; public static final String RELOAD_DATA_OLD1 = "reload";

    // 枪械脚本
    public static final String SCRIPT_TYPE = "script_type"; public static final String SCRIPT_TYPE_OLD1 = "script";
    public static final String SCRIPT_PARAM = "script_param";

    // 开火模式
    public static final String DEFAULT_FIRE_MODE_TYPE = "default_fire_mode_type"; public static final String DEFAULT_FIRE_MODE_TYPE_OLD1 = "default_fire_mode";
    public static final String FIRE_MODE_TYPE = "fire_mode_types"; public static final String FIRE_MODE_TYPE_OLD1 = "fire_mode";
    public static final String FIRE_MODE_ADJUST_DATA = "fire_mode_adjust_data"; public static final String FIRE_MODE_ADJUST_DATA_OLD1 = "fire_mode_adjust";
    public static final String BURST_DATA = "burst_data";

    // 扩展属性
    public static final String MELEE_DATA = "melee_data"; public static final String MELEE_DATA_OLD1 = "melee";
    public static final String HEAT_DATA = "heat_data"; public static final String HEAT_DATA_OLD1 = "heat";
    public static final String CHARGING_DATA = "charging_data"; public static final String CHARGING_DATA_OLD1 = "charging";

    // 配件
    public static final String ALLOW_ATTACHMENT_TYPES = "allow_attachment_types";
    public static final String EXCLUSIVE_ATTACHMENTS = "exclusive_attachments";
    public static final String DEFAULT_MAG_SIZE = "default_mag_size"; public static final String DEFAULT_MAG_SIZE_OLD1 = "ammo_amount";
    public static final String EXTENDED_MAG_AMMO_SIZE = "extended_mag_ammo_size"; public static final String EXTENDED_MAG_AMMO_SIZE_OLD1 = "extended_mag_ammo_amount";
    public static final String BUILTIN_ATTACHMENTS = "builtin_attachments";

    // 举枪动作
    public static final String ENABLE_CRAWL = "enable_crawl"; public static final String ENABLE_CRAWL_OLD1 = "can_crawl";
    public static final String ENABLE_SLIDE = "enable_slide"; public static final String ENABLE_SLIDE_OLD1 = "can_slide";

    // 操作枪械的时长
    public static final String DRAW_TIME = "draw_time";
    public static final String PUT_AWAY_TIME = "put_away_time";
    public static final String SPRINT_TIME = "sprint_time";
    public static final String AIM_TIME = "aim_time";
    public static final String BOLT_ACTION_TIME = "bolt_action_time";
    public static final String BOLT_FEED_TIME = "bolt_feed_time";

    private GunDataTag() {}
}
