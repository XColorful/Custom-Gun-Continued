/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry;

public class _BoneTag {

    public static final String NAME = "name";
    public static final String PARENT = "parent";
    public static final String PIVOT = "pivot";
    public static final String ROTATION = "rotation";
    public static final String CUBES = "cubes";
    public static final String MIRROR = "mirror";

    private _BoneTag() {}

    public static class Node {

        public static final String CAMERA = "camera";
        public static final String CONSTRAINT = "constraint";
        public static final String IDLE_VIEW = "idle_view";
        // ----Gun model----
        public static final String BULLET_IN_BARREL = "bullet_in_barrel";
        public static final String BULLET_IN_MAG = "bullet_in_mag";
        public static final String BULLET_CHAIN = "bullet_chain";
        public static final String CARRY = "carry";
        public static final String MAG_EXTENDED_1 = "mag_extended_1";
        public static final String MAG_EXTENDED_2 = "mag_extended_2";
        public static final String MAG_EXTENDED_3 = "mag_extended_3";
        public static final String MAG_STANDARD = "mag_standard";
        public static final String MOUNT = "mount";
        public static final String SIGHT = "sight";
        public static final String SIGHT_FOLDED = "sight_folded";
        public static final String IRON_VIEW = "iron_view";
        public static final String REFIT_VIEW = "refit_view";
        public static final String THIRD_PERSON_HAND_ORIGIN = "thirdperson_hand";
        public static final String FIXED_ORIGIN = "fixed";
        public static final String GROUND_ORIGIN = "ground";
        public static final String SHELL_ORIGIN = "shell";
        public static final String MUZZLE_FLASH_ORIGIN = "muzzle_flash";
        public static final String LEFTHAND_POS = "lefthand_pos";
        public static final String RIGHTHAND_POS = "righthand_pos";
        public static final String MAG_NORMAL = "magazine";
        public static final String MAG_ADDITIONAL = "additional_magazine";
        public static final String ATTACHMENT_ADAPTER = "attachment_adapter";
        public static final String HANDGUARD_DEFAULT = "handguard_default";
        public static final String HANDGUARD_TACTICAL = "handguard_tactical";
        public static final String ROOT = "root";
        // ----Attachment model----
        public static final String SCOPE_VIEW = "scope_view";
        public static final String SCOPE_BODY = "scope_body";
        public static final String OCULAR_RING = "ocular_ring";
        public static final String DIVISION = "division";
        public static final String OCULAR = "ocular";
        public static final String OCULAR_SIGHT = "ocular_sight";
        public static final String OCULAR_SCOPE = "ocular_scope";
        public static final String LASER_BEAM = "laser_beam";

        // --------prefix--------
        public static class Prefix {
            public static final String REFIT_VIEW = "refit";
        }

        // --------suffix--------
        public static class Suffix {
            public static final String ILLUMINATE = "illuminated";
            public static final String ATTACHMENT_POS = "pos";
            public static final String DEFAULT_ATTACHMENT = "default";
            public static final String REFIT_VIEW = "view";
        }

        private Node() {}
    }
}
