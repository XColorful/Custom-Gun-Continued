/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.interpolator;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.animation.channel.AnimChannelContent;
import dev.xcolorful.customgun.client.api.animation.interpolator.IInterpolator;
import dev.xcolorful.customgun.client.api.animation.interpolator.InterpolatorType;
import dev.xcolorful.customgun.client.api.animation.interpolator.LerpMode;
import dev.xcolorful.customgun.core.util.MathUtil;
import org.joml.Quaternionf;

import java.util.Arrays;

public class CompositeInterpolator implements IInterpolator<CompositeInterpolator> {

    private AnimChannelContent content;

    public CompositeInterpolator() {
    }
    @Override public InterpolatorType getType() {
        return InterpolatorType.COMPOSITE;
    }

    @Override
    public void compile(AnimChannelContent content) {
        this.content = content;
    }

    @Override
    public float[] interpolate(int indexFrom, int indexTo, float alpha) {
        LerpMode fromLerpMode = content.lerpModes[indexFrom];
        LerpMode toLerpMode = content.lerpModes[indexTo];

        if (fromLerpMode == toLerpMode) {
            switch (fromLerpMode) {
                case SPHERICAL_LINEAR -> {
                    return doSphericalLinear(indexFrom, indexTo, alpha);
                }
                case SPHERICAL_SQUAD -> {
                    return doSphericalSquad(indexFrom, indexTo, alpha);
                }
                case CATMULL_ROM -> {
                    return doCatmullromLerp(indexFrom, indexTo, alpha);
                }
            }
        }

        return doOtherLerp(indexFrom, indexTo, alpha);
    }

    @Override
    public CompositeInterpolator clone() {
        try {
            CompositeInterpolator interpolator = (CompositeInterpolator) super.clone();
            interpolator.content = this.content;
            return interpolator;
        } catch (CloneNotSupportedException e) {
            CustomGun.LOGGER.error("Clone not supported.", e);
            throw new RuntimeException(e);
        }
    }

    public static float[] squad(float[] q0, float[] q1, float[] q2, float[] q3, float t) {
        float[] s1 = intermediate(MathUtil.Quaternion.of(q0), MathUtil.Quaternion.of(q1), MathUtil.Quaternion.of(q2));
        float[] s2 = intermediate(MathUtil.Quaternion.of(q1), MathUtil.Quaternion.of(q2), MathUtil.Quaternion.of(q3));

        float[] slerp1 = MathUtil.Quaternion.slerp(q1, q2, t);
        float[] slerp2 = MathUtil.Quaternion.slerp(s1, s2, t);
        return MathUtil.Quaternion.slerp(slerp1, slerp2, 2 * t * (1 - t));
    }

    private float[] getAsQuaternion(int index, boolean needOffset) {
        float[] result = getValue(index, needOffset);
        if (result.length == 3) {
            result = MathUtil.Quaternion.fromEulerAngles(result[0], result[1], result[2]);
        }
        return result;
    }

    private float[] getAsThreeAxis(int index, boolean needOffset) {
        float[] result = getValue(index, needOffset);
        if (result.length == 4) {
            result = MathUtil.Quaternion.toEulerAngles(result);
        }
        return result;
    }

    private float[] getValue(int index, boolean needOffset) {
        boolean isQuaternion = content.values[index].length == 4 || content.values[index].length == 8;
        int offset = 0;
        if (needOffset) {
            offset = switch (content.values[index].length) {
                case 8 -> 4;
                case 6 -> 3;
                default -> 0;
            };
        }
        return Arrays.copyOfRange(content.values[index], offset, offset + (isQuaternion ? 4 : 3));
    }

    private float[] doOtherLerp(int indexFrom, int indexTo, float alpha) {
        if (indexFrom == indexTo) {
            return getValue(indexTo, alpha > 0);
        }
        float[] valueFrom = getAsThreeAxis(indexFrom, true);
        float[] valueTo = getAsThreeAxis(indexTo, false);
        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            result[i] = valueFrom[i] * (1 - alpha) + valueTo[i] * alpha;
        }
        return result;
    }

    private float[] doCatmullromLerp(int indexFrom, int indexTo, float alpha) {
        if (content.values.length == 1) {
            return getValue(0, alpha > 0);
        }
        float[] vx = new float[4];
        float[] vy = new float[4];
        float[] vz = new float[4];
        int prev = indexFrom == 0 ? 0 : indexFrom - 1;
        int next = indexTo == (content.values.length - 1) ? (content.values.length - 1) : indexTo + 1;
        float[] valuePrev = getAsThreeAxis(prev, true);
        float[] valueFrom = getAsThreeAxis(indexFrom, false);
        float[] valueTo = getAsThreeAxis(indexTo, false);
        float[] valueNext = getAsThreeAxis(next, false);
        vx[0] = valuePrev[0];
        vy[0] = valuePrev[1];
        vz[0] = valuePrev[2];
        vx[1] = valueFrom[0];
        vy[1] = valueFrom[1];
        vz[1] = valueFrom[2];
        vx[2] = valueTo[0];
        vy[2] = valueTo[1];
        vz[2] = valueTo[2];
        vx[3] = valueNext[0];
        vy[3] = valueNext[1];
        vz[3] = valueNext[2];
        // 这里用的是三次样条插值，主要是为了和 BlockBench 中的表现贴合
        // BlockBench 中调用的是 THREE.SplineCurve，其实现是三次样条插值；如果用 Catmull-Rom 插值，区别会比较大
        return new float[]{
                MathUtil.splineCurve(vx, 0.5f, alpha),
                MathUtil.splineCurve(vy, 0.5f, alpha),
                MathUtil.splineCurve(vz, 0.5f, alpha)
        };
    }

    private float[] doSphericalLinear(int indexFrom, int indexTo, float alpha) {
        if (content.values.length == 1) {
            return getAsQuaternion(0, alpha > 0);
        }
        // 如果旋转值有 8 个，后四个为 Post 数值，用于插值起点
        float[] q0 = getAsQuaternion(indexFrom, true);
        float[] q1 = getAsQuaternion(indexTo, false);
        return MathUtil.Quaternion.slerp(q0, q1, alpha);
    }

    private float[] doSphericalSquad(int indexFrom, int indexTo, float alpha) {
        if (content.values.length == 1) {
            return getAsQuaternion(0, alpha > 0);
        }
        int prev = indexFrom == 0 ? 0 : indexFrom - 1;
        int next = indexTo == (content.values.length - 1) ? (content.values.length - 1) : indexTo + 1;
        float[] q0 = getAsQuaternion(prev, true);
        float[] q1 = getAsQuaternion(indexFrom, false);
        float[] q2 = getAsQuaternion(indexTo, false);
        float[] q3 = getAsQuaternion(next, false);

        // 这里用的是三次样条插值，主要是为了和 BlockBench 中的表现贴合
        // BlockBench 中调用的是 THREE.SplineCurve，其实现是三次样条插值；如果用 Catmull-Rom 插值，区别会比较大
//        float[] r = MathUtil.Quaternion.splineCurve(new float[][]{q0, content.values[indexFrom], content.values[indexTo], content.values[next]}, 0.5f, alpha);
        return squad(q0, q1, q2, q3, alpha);
    }

    private static float[] intermediate(Quaternionf q0, Quaternionf q1, Quaternionf q2) {
        if (q1.dot(q0) < 0) {
            q0 = reverse(q0);
        }
        if (q1.dot(q2) < 0) {
            q2 = reverse(q2);
        }
        Quaternionf q0Conj = q0.conjugate(new Quaternionf());
        Quaternionf q1Conj = q1.conjugate(new Quaternionf());
        Quaternionf m0 = q0Conj.mul(q1);
        Quaternionf m1 = q1Conj.mul(q2);
        Quaternionf m0Log = log(m0);
        Quaternionf m1Log = log(m1);
        Quaternionf mLogSum = m0Log.add(m1Log.mul(-1f));
        Quaternionf exp = exp(mLogSum.mul(0.25f));
        Quaternionf result = q1.mul(exp);
        return new float[]{result.x, result.y, result.z ,result.w};
    }

    private static Quaternionf log(Quaternionf q) {
        double sin = Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z);
        double theta = Math.atan2(sin, q.w);
        Quaternionf result = new Quaternionf(q);
        if (sin > 0.0005f) {
            result.mul((float) (theta / sin));
        }
        result.w = 0;
        return result;
    }

    private static Quaternionf exp(Quaternionf q) {
        double theta = Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z);
        double cos = Math.cos(theta);
        Quaternionf result = new Quaternionf(q);
        if (cos < 0.9995){
            result.mul((float) (Math.sin(theta) / theta));
        }
        result.w = (float) cos;
        return result;
    }

    private static Quaternionf reverse(Quaternionf q) {
        Quaternionf result = new Quaternionf(q);
        q.mul(-1f);
        return result;
    }
}
