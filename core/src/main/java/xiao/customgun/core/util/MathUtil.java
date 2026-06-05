/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.util;

public class MathUtil {

    /**
     * 根据指定的放大倍数和当前的视场角（FOV），计算缩放后的新视场角
     * <p>
     * 适用于于武器开镜瞄准（ADS）的放大逻辑，通过操作半视角正切值的方式对视场角进行非线性缩放，以保持几何透视的准确性
     * @param magnification 目标放大倍数（例如：2.0 表示 2 倍镜，必须大于 0）
     * @param currentFov    当前的视场角（角度制）
     * @return 放大后的新视场角（角度制）
     */
    public static double magnificationToFov(double magnification, double currentFov) {
        double currentTan = Math.tan(Math.toRadians(currentFov / 2));
        double newTan = currentTan / magnification;
        return Math.toDegrees(Math.atan(newTan)) * 2;
    }

    /**
     * 根据当前的视场角（FOV）和原始视场角，计算缩放后的鼠标灵敏度比例
     * <p>
     * 适用于武器开镜瞄准（ADS）的灵敏度修正逻辑，通过操作半视角正切值并乘以补偿系数的方式计算视角缩放比，以保持不同缩放倍率下鼠标拉枪的视觉一致性
     * @param currentFov  当前开镜后的视场角（角度制）
     * @param originFov   原始的未开镜视场角（角度制）
     * @param coefficient 缩放补偿系数（用于对齐不同屏幕比例或焦距的系数）
     * @return 灵敏度缩放比例系数
     */
    public static double zoomSensitivityRatio(double currentFov, double originFov, double coefficient) {
        return Math.atan(Math.tan(Math.toRadians(currentFov / 2)) * coefficient) /
                Math.atan(Math.tan(Math.toRadians(originFov / 2)) * coefficient);
    }
}
