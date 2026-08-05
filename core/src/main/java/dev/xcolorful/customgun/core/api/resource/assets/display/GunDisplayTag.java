/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.resource.assets.display;

import dev.xcolorful.customgun.core.api.resource.data.data.GunDataTag;

public class GunDisplayTag extends _AssetsDisplayTag {

    // 材质
    public static final String HUD_TEXTURE_LOCATION = "hud_texture_location"; public static final String HUD_TEXTURE_LOCATION_OLD1 = "hud";
    public static final String HUD_EMPTY_TEXTURE_LOCATION = "hud_empty_texture_location"; public static final String HUD_EMPTY_TEXTURE_LOCATION_OLD1 = "hud_empty";

    // 模型
    public static final String GUN_MODEL_TYPE = "gun_model_type"; public static final String GUN_MODEL_TYPE_OLD1 = "model_type";
    public static final String LOD_DISPLAY = "lod_display"; public static final String LOD_DISPLAY_OLD1 = "lod";
    public static final String ENABLE_TRANSPARENCY = "enable_transparency";

    // 显示
    public static final String IRON_ZOOM_SCALE = "iron_zoom_scale"; public static final String IRON_ZOOM_SCALE_OLD1 = "iron_zoom";
    public static final String IRON_VIEW_FOV = "iron_view_fov"; public static final String IRON_VIEW_FOV_OLD1 = "zoom_model_fov";
    public static final String ENABLE_CROSSHAIR = "enable_crosshair"; public static final String ENABLE_CROSSHAIR_OLD1 = "show_crosshair";
    public static final String MUZZLE_FLASH_DISPLAY = "muzzle_flash_display"; public static final String MUZZLE_FLASH_DISPLAY_OLD1 = "muzzle_flash";
    public static final String MODEL_NODE_TEXT_DISPLAY = "model_node_text_display"; public static final String MODEL_NODE_TEXT_DISPLAY_OLD1 = "text_show";
    public static final String LASER_DISPLAY = "laser_display"; public static final String LASER_DISPLAY_OLD1 = "laser";
    public static final String SURROUND_DISPLAY_BY_HOTBAR = "surround_display_by_hotbar"; public static final String SURROUND_DISPLAY_BY_HOTBAR_OLD1 = "hotbar_show";
    public static final String SURROUND_DISPLAY_BY_OFFHAND = "surround_display_by_offhand"; public static final String SURROUND_DISPLAY_BY_OFFHAND_OLD1 = "offhand_show";
    public static final String DAMAGE_DISPLAY_TYPE = "damage_display_type"; public static final String DAMAGE_DISPLAY_TYPE_OLD1 = "damage_style";
    public static final String AMMO_COUNT_TYPE = "ammo_count_type"; public static final String AMMO_COUNT_TYPE_OLD1 = "ammo_count_style";
    public static final String AMMO_DISPLAY_OVERRIDE = "ammo_display_override"; public static final String AMMO_DISPLAY_OVERRIDE_OLD1 = "ammo";

    // 动画
    public static final String GUN_ANIMATION_LOCATION = "gun_animation_location"; public static final String GUN_ANIMATION_LOCATION_OLD1 = "animation";
    @Deprecated public static final String USE_DEFAULT_ANIMATION = "use_default_animation";
    @Deprecated public static final String DEFAULT_ANIMATION = "default_animation";
    public static final String SCRIPT_LOCATION = GunDataTag.SCRIPT_LOCATION; public static final String SCRIPT_LOCATION_OLD1 = "state_machine";
    public static final String SCRIPT_PARAM = GunDataTag.SCRIPT_PARAM; public static final String SCRIPT_PARAM_OLD1 = "state_machine_param";
    public static final String SHELL_EJECTION_PARAM = "shell_ejection_param"; public static final String SHELL_EJECTION_PARAM_OLD1 = "shell";
    public static final String THIRD_PERSON_ANIMATION_TYPE = "third_person_animation_type"; public static final String THIRD_PERSON_ANIMATION_TYPE_OLD1 = "third_person_animation";
    public static final String PLAYER_ANIMATOR_LOCATION = "player_animator_location"; public static final String PLAYER_ANIMATOR_LOCATION_OLD1 = "player_animator_3rd";
    public static final String PLAYER_ANIMATOR_FIXED_HAND = "player_animator_fixed_hand"; public static final String PLAYER_ANIMATOR_FIXED_HAND_OLD1 = "3rd_fixed_hand";
    public static final String GUN_SOUNDS = "gun_sounds"; public static final String GUN_SOUNDS_OLD1 = "sounds";
    public static final String PRELOAD_SOUND_LOCATION = "preload_sound_location"; public static final String PRELOAD_SOUND_LOCATION_OLD1 = "preload_sounds";

    public static final String CONTROLLABLE_DATA = "controllable_data"; public static final String CONTROLLABLE_DATA_OLD1 = "controllable";

    private GunDisplayTag() {}
}
