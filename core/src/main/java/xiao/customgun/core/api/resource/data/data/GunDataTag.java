/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.data;

public class GunDataTag {

    // 枪械属性
    public static final String BULLET_DATA = "bullet"; // 子弹属性
    public static final String AMMO_TYPE = "ammo"; // 子弹类型
    public static final String BOLT_TYPE = "bolt"; // 拉栓类型

    public static final String RPM = "rpm"; // 射速
    public static final String INACCURACY_DATA = "inaccuracy"; // 射击散布
    public static final String RECOIL_DATA = "recoil"; // 后坐力
    public static final String CRAWL_RECOIL_MULTIPLIER = "crawl_recoil_multiplier"; // 蹲后坐力

    public static final String WEIGHT = "weight"; // 基础移速影响
    public static final String MOVEMENT_DATA = "movement_speed"; // 移速数据

    public static final String FIRE_SOUND_DATA = "fire_sound"; // 开火声音范围
    public static final String HURT_BOB_TWEAK_MULTIPLIER = "hurt_bob_tweak_multiplier"; // 被命中者受击晃动

    public static final String RELOAD_DATA = "reload"; // 装弹数据

    // 枪械脚本
    public static final String SCRIPT_TYPE = "script"; // 状态机脚本
    public static final String SCRIPT_PARAM = "script_param"; // 状态机参数

    // 开火模式
    public static final String FIRE_MODE_TYPE = "fire_mode"; // 开火模式
    public static final String FIRE_MODE_DATA = "fire_mode_adjust"; // 开火模式数据
    public static final String BURST_DATA = "burst_data"; // 开火模式(2/3连发)模式数据

    // 扩展属性
    public static final String MELEE_DATA = "melee"; // 近战 (刺刀/枪托)
    public static final String HEAT_DATA = "heat"; // 过热
    public static final String CHARGING_DATA = "charging"; // 蓄力/延迟扳机

    // 配件
    public static final String ALLOW_ATTACHMENT_TYPES = "allow_attachment_types"; // 配件槽
    public static final String EXCLUSIVE_ATTACHMENTS = "exclusive_attachments"; // (疑似已损坏功能)
    public static final String DEFAULT_MAG_SIZE = "ammo_amount"; // 默认弹匣大小
    public static final String EXTENDED_MAG_AMMO_SIZE = "extended_mag_ammo_amount"; // 扩容弹匣大小
    public static final String BUILTIN_ATTACHMENTS = "builtin_attachments"; // 默认配件外观

    // 举枪动作
    public static final String ENABLE_CRAWL = "can_crawl";
    public static final String ENABLE_SLIDE = "can_slide"; // 枪挡在视野中间

    // 操作枪械的时长
    public static final String DRAW_TIME = "draw_time";
    public static final String PUT_AWAY_TIME = "put_away_time";
    public static final String SPRINT_TIME = "sprint_time";
    public static final String AIM_TIME = "aim_time";
    public static final String BOLT_ACTION_TIME = "bolt_action_time";
    public static final String BOLT_FEED_TIME = "bolt_feed_time";

    private GunDataTag() {}
}
