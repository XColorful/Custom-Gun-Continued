/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.client.api.event;

public enum RenderLevelStage {
    AFTER_SKY,
    AFTER_SOLID_BLOCKS,
    AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS,
    AFTER_CUTOUT_BLOCKS,
    AFTER_ENTITIES,
    AFTER_BLOCK_ENTITIES,
    AFTER_TRANSLUCENT_BLOCKS,
    AFTER_TRIPWIRE_BLOCKS,
    AFTER_PARTICLES,
    AFTER_WEATHER,
    AFTER_LEVEL;
}
