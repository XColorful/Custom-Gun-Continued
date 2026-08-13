/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.client.util;

import dev.xcolorful.customgun.client.api.event.IRenderLevelStageEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ClientRenderUtils {

    public static @NotNull ResourceLocation getMissingTextureLocation() {
        return MissingTextureAtlasSprite.getLocation();
    }

    public static ResourceLocation getSkinTextureLocation(LocalPlayer clientPlayer) {
        return clientPlayer.getSkin().texture();
    }

    /**
     * @return partialTick
     */
    public static float getRenderFrameTime() {
        // 1.20.1-1.20.4
//      return Minecraft.getInstance().getFrameTime();

        // 1.21.1
        return Minecraft.getInstance()
                .getTimer()
                .getGameTimeDeltaPartialTick(false);

        // 1.21.4
//      return Minecraft.getInstance()
//              .getDeltaTracker()
//              .getGameTimeDeltaPartialTick(false);
    }

    /**
     * 创建一个新的模型视图矩阵
     * 该矩阵基于 baseModelView, 并应用了平移到指定世界中心点的变换 (同时抵消了相机偏移)
     */
    @Deprecated(forRemoval = false)
    public static Matrix4f createCenterOffsetMatrix(Matrix4f baseModelView, Vec3 worldCenter, Vec3 cameraPos) {
        return createCenterOffsetMatrix(baseModelView, worldCenter.x, worldCenter.y, worldCenter.z, cameraPos);
    }
    public static Matrix4f createCenterOffsetMatrix(Matrix4f baseModelView, double centerX, double centerY, double centerZ, Vec3 cameraPos) {
        Matrix4f matrix = baseModelView != null ? new Matrix4f(baseModelView) : new Matrix4f();
        // 平移到目标中心点，并抵消相机位置
        matrix.translate(
                (float) (centerX - cameraPos.x()),
                (float) (centerY - cameraPos.y()),
                (float) (centerZ - cameraPos.z()));
        return matrix;
    }

    /**
     * MC各版本通用
     */
    @Deprecated(forRemoval = false)
    public static Matrix4f createCenterOffsetMatrix(IRenderLevelStageEvent event, Vec3 worldCenter, Vec3 cameraPos) {
        return createCenterOffsetMatrix(new Matrix4f(), worldCenter.x, worldCenter.y, worldCenter.z, cameraPos);
    }
    public static Matrix4f createCenterOffsetMatrix(IRenderLevelStageEvent event, double centerX, double centerY, double centerZ, Vec3 cameraPos) {
        return createCenterOffsetMatrix(new Matrix4f(), centerX, centerY, centerZ, cameraPos);
    }

    @ApiStatus.AvailableSince("1.21.4")
    public static class RenderState {

        public static @Nullable LivingEntity getLivingEntity(Object renderState) {
            return null;
        }
    }
}
