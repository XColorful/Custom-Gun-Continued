/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.client.api.event;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("neoforge26.2")
public interface ISubmitCustomGeometryEvent extends IEvent {

    PoseStack getPoseStack();

    Vec3 getCamera_getPosition();

    float getPartialTick();

    @ApiStatus.AvailableSince("neoforge26.2")
    Object getSubmitNodeCollector();
}
