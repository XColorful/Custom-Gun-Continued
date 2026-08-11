/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.animation.interpolator;

import dev.xcolorful.customgun.client.animation.interpolator.*;

import java.util.function.Supplier;

public enum InterpolatorType {
    /**
     * 线性插值
     * <ul>
     *     <li>按照固定比例计算起点与终点之间的过渡值</li>
     * </ul>
     */
    LINEAR(Linear::new),
    /**
     * 球面线性插值
     * <ul>
     *     <li>通过球面路径计算旋转值的平滑过渡</li>
     * </ul>
     */
    SLERP(SLerp::new),
    /**
     * 样条插值
     * <ul>
     *     <li>通过多个控制点生成连续平滑的过渡曲线</li>
     * </ul>
     */
    SPLINE(Spline::new),
    /**
     * 阶梯插值
     * <ul>
     *     <li>保持当前值直到达到指定时间点后切换至目标值</li>
     * </ul>
     */
    STEP(Step::new),
    /**
     * 复合插值
     * <ul>
     *     <li>根据关键帧配置自动选择对应的插值算法</li>
     *     <li>支持线性插值、球面线性插值、样条插值等多种过渡方式</li>
     * </ul>
     */
    COMPOSITE(CompositeInterpolator::new);

    private final Supplier<IInterpolator<?>> factory;
    InterpolatorType(Supplier<IInterpolator<?>> factory) {
        this.factory = factory;
    }

    public IInterpolator<?> create() {
        return factory.get();
    }
}
