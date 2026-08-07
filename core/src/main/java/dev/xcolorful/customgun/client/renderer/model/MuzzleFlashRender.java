/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.model.GunModelObject;
import dev.xcolorful.customgun.client.model.bedrock.SlotModel;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class MuzzleFlashRender implements IModelComponentRenderer {
    public static class State {
        /**
         * 当前是否渲染自身的枪口火焰
         */
        public static boolean isSelf = false;
        private static long lastShootTimestamp = -1;
        private static boolean muzzleFlashStartMark = false;
        private static float muzzleFlashRandomRotate = 0;
        private static Matrix3f muzzleFlashNormal = new Matrix3f();
        private static Matrix4f muzzleFlashPose = new Matrix4f();
    }
    private static final SlotModel MUZZLE_FLASH_MODEL = new SlotModel(true);
    /**
     * 枪口火焰显示时长
     */
    private static final long TIME_RANGE_MS = 50;


    private final GunModelObject gunModelObject;

    public MuzzleFlashRender(GunModelObject gunModelObject) {
        this.gunModelObject = gunModelObject;
    }

    @Override
    public void render(PoseStack poseStack,
                       VertexConsumer vertexBuffer,
                       ItemDisplayContext transformType,
                       int light, int overlay) {
        // TODO
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true) private static final long TIME_RANGE = TIME_RANGE_MS;
}
