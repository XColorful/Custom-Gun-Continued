/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.item.gun;

import dev.xcolorful.customgun.core.resource.data.data.gun.recoil._RecoilEntryData;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GunRecoilCalculator {
    private static final SplineInterpolator INTERPOLATOR = new SplineInterpolator();

    /**
     * 根据后坐力关键帧生成三次样条插值函数
     * <br>
     * <br>
     * 输入数据：
     * <ul>
     *     <li>输入数据来自后坐力配置中的关键帧列表，每个 {@link _RecoilEntryData} 对应 JSON 中的一组数据：
     *         {@code {"time": 0.1, "range": [0.8, 1.2]}}</li>
     *     <li>{@code time} 表示关键帧时间，单位为秒；{@code range} 表示该时间点后坐力值的随机取值范围</li>
     * </ul>
     * <br>
     * 数据转换：
     * <ul>
     *     <li>
     *         每个关键帧会转换为插值点：
     *         <ul>
     *             <li>{@code x = time * 1000 + 30}，表示该关键帧在插值函数中的时间坐标，其中额外增加 {@code 30ms} 用于保留开火瞬间 {@code (0, 0)} 到第一个关键帧之间的过渡时间</li>
     *             <li>{@code y = (range[0] + random() * (range[1] - range[0])) * modifier}，表示经过随机取值和倍率缩放后的后坐力值</li>
     *         </ul>
     *     </li>
     *     <li>计算过程中会额外添加初始插值点 {@code (0, 0)}，表示开火瞬间的后坐力初始状态</li>
     * </ul>
     * <br>
     * 插值结果：
     * <ul>
     *     <li>所有插值点通过三次样条插值生成连续函数</li>
     *     <li>三次样条由多个三次多项式组成，并保证相邻曲线段连接处的位置、一阶导数和二阶导数连续，使后坐力曲线保持平滑</li>
     *     <li>返回结果 {@code S(x)} 表示单次开火过程中的后坐力变化函数，输入相对开火时间 {@code x} 后可得到对应时间点的后坐力值</li>
     * </ul>
     * <br>
     * {@link GunCameraHelper}实际表现：
     * <ul>
     *     <li>该函数在每次开火时重新调用，每发子弹都会重新生成后坐力曲线并从时间 {@code x = 0} 开始计算</li>
     *     <li>连续射击时，每发子弹都会重新触发曲线前段，因此表现为每发子弹独立产生后坐力，而不会随着射击时间累计增加后坐力 (避免不消耗弹匣子弹时用尽后坐力曲线)</li>
     *     <li>停止射击后，如果继续使用同一条曲线计算时间，则会进入曲线后段，表现为自然恢复过程</li>
     *     <li>由于每次调用都会重新根据 {@code range} 随机采样后坐力值，因此连续射击时每发子弹的后坐力方向和幅度可能不同</li>
     * </ul>
     * @param keyFrames 后坐力关键帧列表，对应 JSON 中的后坐力配置数据
     * @param modifier 后坐力倍率，用于缩放随机生成的后坐力值
     * @return 后坐力变化的三次样条函数；当关键帧为空时返回 {@code null}
     */
    protected static @Nullable PolynomialSplineFunction getSplineFunction(List<_RecoilEntryData> keyFrames, float modifier) {
        if (keyFrames == null || keyFrames.isEmpty()) {
            return null;
        }

        // 额外预留一个元素，用于添加初始插值点 (0, 0)
        double[] values = new double[keyFrames.size() + 1];
        double[] times = new double[keyFrames.size() + 1];

        // 添加开火瞬间的初始状态
        times[0] = 0;
        values[0] = 0;

        // 根据关键帧时间生成插值函数的时间坐标
        for (int i = 0; i < keyFrames.size(); i++) {
            times[i + 1] = keyFrames.get(i).getTime() * 1000 + 30;
        }

        // 根据关键帧范围随机生成后坐力值，并应用倍率
        for (int i = 0; i < keyFrames.size(); i++) {
            float[] value = keyFrames.get(i).getRange();
            values[i + 1] = (value[0] + Math.random() * (value[1] - value[0])) * modifier;
        }

        // 使用所有时间点和后坐力值生成三次样条曲线
        return INTERPOLATOR.interpolate(times, values);
    }
}
