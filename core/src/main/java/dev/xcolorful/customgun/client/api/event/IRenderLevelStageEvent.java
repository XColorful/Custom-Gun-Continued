/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * @since 26.2 改用 {@link ISubmitCustomGeometryEvent}
 */
public interface IRenderLevelStageEvent extends IEvent {

    RenderLevelStage getStage();

    Matrix4f getModelViewMatrix();

    Vec3 getCamera_getPosition();

    float getPartialTick();
}
