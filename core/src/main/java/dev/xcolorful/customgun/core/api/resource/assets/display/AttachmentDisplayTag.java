/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.resource.assets.display;

public class AttachmentDisplayTag extends _AssetsDisplayTag {

    // 模型
    public static final String LOD_DISPLAY = "lod_display"; public static final String LOD_DISPLAY_OLD1 = "lod";
    public static final String ADAPTER_NODE_NAME = "adapter_node_name"; public static final String ADAPTER_NODE_NAME_OLD1 = "adapter";

    // 显示
    public static final String SCOPE_ZOOM_SCALE = "scope_zoom_scale"; public static final String SCOPE_ZOOM_SCALE_OLD1 = "zoom";
    public static final String SCOPE_VIEW_INDEX = "scope_view_index"; public static final String SCOPE_VIEW_INDEX_OLD1 = "views";
    public static final String SCOPE_VIEW_FOV = "scope_view_fov"; public static final String SCOPE_VIEW_FOV_OLD2 = "views_fov"; public static final String SCOPE_VIEW_FOV_OLD1 = "fov";
    public static final String ENABLE_SCOPE = "enable_scope";
    public static final String ENABLE_SIGHT = "enable_sight";
    public static final String SHOW_MUZZLE = "show_muzzle";
    public static final String SHOW_MOUNT = "show_mount";
    public static final String MODEL_NODE_TEXT_DISPLAY = GunDisplayTag.MODEL_NODE_TEXT_DISPLAY; public static final String MODEL_NODE_TEXT_DISPLAY_OLD1 = "text_show";
    public static final String LASER_DISPLAY = GunDisplayTag.LASER_DISPLAY; public static final String LASER_DISPLAY_OLD1 = "laser";
    public static final String ATTACHMENT_SOUNDS = "attachment_sounds"; public static final String ATTACHMENT_SOUNDS_OLD1 = "sounds";

    private AttachmentDisplayTag() {}
}
