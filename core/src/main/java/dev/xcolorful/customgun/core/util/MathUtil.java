/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

/**
 * 数学工具就没必要拆到{@code util.math}包下了
 * <ul>
 *     <li>每段工具能排在一起/用内部类隔离，就足够维护了</li>
 *     <li>Util类本身就相当于"全能易用门面"，内部排列/代理对调用方都是一样的</li>
 *     <li>类名直接带{@code MathUtil}有强调属性更好，比如{@link MathUtil.Quaternion}</li>
 *     <li>Math是大家都认识的，但是Math的词汇不一定 (悲报：啥是四元数)</li>
 *     <li>当MathUtil里出现多个内部类的时候，IDE可以折叠，从而看到内部类列表，diff也隔离在不同区域，缺点是从网页看blame很痛苦</li>
 * </ul>
 */
public class MathUtil {

    public static double magnificationToFovMultiplier(double magnification, double currentFov) {
        return magnificationToFov(magnification, currentFov) / currentFov;
    }
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
    public static double fovToMagnification(double currentFov, double originFov) {
        return Math.tan(Math.toRadians(originFov / 2)) / Math.tan(Math.toRadians(currentFov / 2));
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

    public static double copySign(double magnitude, double sign) {
        return Math.abs(magnitude) * (sign < 0 ? -1 : 1);
    }

    /**
     * 将负旋转角(弧度)转换为等效的正角(角度)
     * @param angle 弧度
     * @return 等效正角(角度)
     */
    public static double toDegreePositive(double angle) {
        while (angle < 0) {
            angle += Math.PI * 2;
        }
        return Math.toDegrees(angle);
    }

    public static Vector3f getEulerAngles(Matrix4f matrix) {
        Vector3f dest = new Vector3f();
        dest.x = (float) Math.atan2(matrix.m12(), matrix.m22());
        // 对 sqrt 的参数做 clamp：旋转到 ±90° 万向锁时浮点误差会让 m02² 略微超过 1，sqrt(负数) 产生 NaN
        dest.y = (float) Math.atan2(-matrix.m02(), Math.sqrt(Math.max(0.0f, 1.0f - matrix.m02() * matrix.m02())));
        dest.z = (float) Math.atan2(matrix.m01(), matrix.m00());
        return dest;
    }

    public static Vector3f toAngle(Vector3f vector3f) {
        return vector3f.set((float) Math.toRadians(vector3f.x()), (float) Math.toRadians(vector3f.y()), (float) Math.toRadians(vector3f.z()));
    }

    public static float[] solveEquations(float[][] coefficients, float[] constants) {
        int n = constants.length;
        // 高斯消元
        for (int pivot = 0; pivot < n - 1; pivot++) {
            for (int row = pivot + 1; row < n; row++) {
                float factor = coefficients[row][pivot] / coefficients[pivot][pivot];
                for (int col = pivot; col < n; col++) {
                    coefficients[row][col] -= coefficients[pivot][col] * factor;
                }
                constants[row] -= constants[pivot] * factor;
            }
        }
        // 回代求解
        float[] solution = new float[n];
        for (int i = n - 1; i >= 0; i--) {
            float sum = 0.0f;
            for (int j = i + 1; j < n; j++) {
                sum += coefficients[i][j] * solution[j];
            }
            solution[i] = (constants[i] - sum) / coefficients[i][i];
        }
        return solution;
    }

    /**
     * 在两个变换矩阵之间旋转、位移的插值
     * @param resultMatrix 输出结果将乘进此矩阵
     */
    public static void applyMatrixLerp(Matrix4f fromMatrix, Matrix4f toMatrix, Matrix4f resultMatrix, float alpha) {
        // 计算位移的插值
        Vector3f translation = new Vector3f(toMatrix.m30() - fromMatrix.m30(), toMatrix.m31() - fromMatrix.m31(), toMatrix.m32() - fromMatrix.m32());
        translation.mul(alpha);
        // 计算旋转的插值：直接从矩阵提取四元数，避免走欧拉角在 90° 万向锁处 sqrt(负数) 产生 NaN
        Quaternionf qFrom = fromMatrix.getNormalizedRotation(new Quaternionf());
        Quaternionf qTo = toMatrix.getNormalizedRotation(new Quaternionf());
        Quaternionf qRelative = Quaternion.getRelative(qFrom, qTo);
        Quaternionf qLerped = Quaternion.slerp(new Quaternionf(0, 0, 0, 1), qRelative, alpha);
        // 应用位移和旋转
        resultMatrix.m30(resultMatrix.m30() + translation.x);
        resultMatrix.m31(resultMatrix.m31() + translation.y);
        resultMatrix.m32(resultMatrix.m32() + translation.z);
        resultMatrix.rotate(qLerped);
    }

    public static double getTwoVecAngle(Vec3 v1, Vec3 v2) {
        double dotProduct = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
        double magnitude1 = Math.sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z);
        double magnitude2 = Math.sqrt(v2.x * v2.x + v2.y * v2.y + v2.z * v2.z);
        if (magnitude1 * magnitude2 == 0) {
            return -1;
        }
        double cos = dotProduct / (magnitude1 * magnitude2);
        return Math.acos(cos);
    }

    public static float splineCurve(float[] y, float tension, float alpha) {
        if (y.length != 4) {
            throw new IllegalArgumentException("y value length must be 4 when doing catmull-rom spline");
        }
        if (tension < 0 || tension > 1) {
            throw new IllegalArgumentException("tension must be 0~1 when doing catmull-rom spline");
        }
        float v0 = (y[2] - y[0]) * 0.5f;
        float v1 = (y[3] - y[1]) * 0.5f;
        float t2 = alpha * alpha;
        float t3 = alpha * t2;
        float h1 = 2f * t3 - 3f * t2 + 1f;
        float h2 = -2f * t3 + 3f * t2;
        float h3 = t3 - 2f * t2 + alpha;
        float h4 = t3 - t2;
        return h1 * y[1] + h2 * y[2] + h3 * v0 + h4 * v1;
    }

    public static class Quaternion {

        public static final float[] QUATERNION_ONE = {0, 0, 0, 1};

        /**
         * 按照 z(roll) -> y(yaw) -> x(pitch) 的旋转顺序，求四元数
         * @param pitch 绕 x 轴旋转的弧度
         * @param yaw 绕 y 轴旋转的弧度
         * @param roll 绕 z 轴旋转的弧度
         * @return 四元数，前三个数是虚部，最后一个数是实部
         */
        public static float[] fromEulerAngles(float pitch, float yaw, float roll) {
            double cy = Math.cos(roll * 0.5);
            double sy = Math.sin(roll * 0.5);
            double cp = Math.cos(yaw * 0.5);
            double sp = Math.sin(yaw * 0.5);
            double cr = Math.cos(pitch * 0.5);
            double sr = Math.sin(pitch * 0.5);
            return new float[]{
                    (float) (cy * cp * sr - sy * sp * cr),
                    (float) (sy * cp * sr + cy * sp * cr),
                    (float) (sy * cp * cr - cy * sp * sr),
                    (float) (cy * cp * cr + sy * sp * sr)
            };
        }
        public static Quaternionf of(float pitch, float yaw, float roll) {
            return set(new Quaternionf(), pitch, yaw, roll);
        }
        public static Quaternionf of(float[] q) {
            return new Quaternionf(q[0], q[1], q[2], q[3]);
        }
        /**
         * 按照 z(roll) -> y(yaw) -> x(pitch) 的旋转顺序，求四元数
         * @param pitch 绕 x 轴旋转的弧度
         * @param yaw 绕 y 轴旋转的弧度
         * @param roll 绕 z 轴旋转的弧度
         * @param quaternion 求解的结果将写入这个四元数中
         */
        public static Quaternionf set(@NotNull Quaternionf quaternion, float pitch, float yaw, float roll) {
            double cy = Math.cos(roll * 0.5);
            double sy = Math.sin(roll * 0.5);
            double cp = Math.cos(yaw * 0.5);
            double sp = Math.sin(yaw * 0.5);
            double cr = Math.cos(pitch * 0.5);
            double sr = Math.sin(pitch * 0.5);
            return quaternion.set(
                    (float) (cy * cp * sr - sy * sp * cr),
                    (float) (sy * cp * sr + cy * sp * cr),
                    (float) (sy * cp * cr - cy * sp * sr),
                    (float) (cy * cp * cr + sy * sp * sr)
            );
        }

        /**
         * 将四元数转换为欧拉角
         * @param q 四元数
         * @return 按照 x(pitch) -> y(yaw) -> z(roll) 的顺序的三轴角数组
         */
        public static float[] toEulerAngles(Quaternionf q) {
            float[] angles = new float[3];
            // pitch (x-axis rotation)
            double sinrCosp = 2 * (q.w() * q.x() + q.y() * q.z());
            double cosrCosp = 1 - 2 * (q.x() * q.x() + q.y() * q.y());
            angles[0] = (float) Math.atan2(sinrCosp, cosrCosp);
            // yaw (y-axis rotation)
            double sinp = 2 * (q.w() * q.y() - q.x() * q.z());
            if (Math.abs(sinp) >= 1) {
                // use 90 degrees if out of range
                angles[1] = (float) copySign(Math.PI / 2, sinp);
            } else {
                angles[1] = (float) Math.asin(sinp);
            }
            // roll (z-axis rotation)
            double sinyCosp = 2 * (q.w() * q.z() + q.y() * q.x());
            double cosyCosp = 1 - 2 * (q.y() * q.y() + q.z() * q.z());
            angles[2] = (float) Math.atan2(sinyCosp, cosyCosp);
            return angles;
        }
        /**
         * 将四元数转换为欧拉角
         * @param q 四元数，前三个数是虚部，最后一个数是实部
         * @return 按照 x(pitch) -> y(yaw) -> z(roll) 的顺序的三轴角数组
         */
        public static float[] toEulerAngles(float[] q) {
            float[] angles = new float[3];
            // pitch (x-axis rotation)
            double sinrCosp = 2 * (q[3] * q[0] + q[1] * q[2]);
            double cosrCosp = 1 - 2 * (q[0] * q[0] + q[1] * q[1]);
            angles[0] = (float) Math.atan2(sinrCosp, cosrCosp);
            // yaw (y-axis rotation)
            double sinp = 2 * (q[3] * q[1] - q[2] * q[0]);
            if (Math.abs(sinp) >= 1) {
                angles[1] = (float) copySign(Math.PI / 2, sinp); // use 90 degrees if out of range
            } else {
                angles[1] = (float) Math.asin(sinp);
            }
            // roll (z-axis rotation)
            double sinyCosp = 2 * (q[3] * q[2] + q[1] * q[0]);
            double cosyCosp = 1 - 2 * (q[1] * q[1] + q[2] * q[2]);
            angles[2] = (float) Math.atan2(sinyCosp, cosyCosp);
            return angles;
        }

        /**
         * 求四元数的逆
         * @param quaternion 四元数，前三个数是虚部，最后一个数是实部
         * @return 四元数的逆
         */
        public static float[] inverse(float[] quaternion) {
            float[] result = new float[4];
            // 求共轭
            result[0] = -quaternion[0];
            result[1] = -quaternion[1];
            result[2] = -quaternion[2];
            result[3] = quaternion[3];
            // 求模长平方，进行归一化
            float m2 = quaternion[0] * quaternion[0] + quaternion[1] * quaternion[1] + quaternion[2] * quaternion[2] + quaternion[3] * quaternion[3];
            result[0] = result[0] / m2;
            result[1] = result[1] / m2;
            result[2] = result[2] / m2;
            result[3] = result[3] / m2;
            return result;
        }

        public static float[] mul(float[] q1, float[] q2) {
            return new float[]{
                    Math.fma(q1[3], q2[0], Math.fma(q1[0], q2[3], Math.fma(q1[1], q2[2], -q1[2] * q2[1]))),
                    Math.fma(q1[3], q2[1], Math.fma(-q1[0], q2[2], Math.fma(q1[1], q2[3], q1[2] * q2[0]))),
                    Math.fma(q1[3], q2[2], Math.fma(q1[0], q2[1], Math.fma(-q1[1], q2[0], q1[2] * q2[3]))),
                    Math.fma(q1[3], q2[3], Math.fma(-q1[0], q2[0], Math.fma(-q1[1], q2[1], -q1[2] * q2[2])))
            };
        }

        public static Quaternionf blend(Quaternionf to, Quaternionf from) {
            Quaternionf q1 = new Quaternionf(to);
            Quaternionf q2 = new Quaternionf(from);
            normalize(q1);
            normalize(q2);
            log(q1);
            log(q2);
            q1.set(q1.x() + q2.x(), q1.y() + q2.y(), q1.z() + q2.z(), q1.w() + q2.w());
            exp(q1);
            normalize(q1);
            return to.set(q1.x(), q1.y(), q1.z(), q1.w());
        }

        public static Quaternionf normalize(Quaternionf q) {
            float f = q.x() * q.x() + q.y() * q.y() + q.z() * q.z() + q.w() * q.w();
            if (f > 0) {
                float f1 = (float) Mth.fastInvSqrt(f);
                return q.set(f1 * q.x(), f1 * q.y(), f1 * q.z(), f1 * q.w());
            } else {
                return q.set(0, 0, 0, 1);
            }
        }

        public static Quaternionf log(Quaternionf q) {
            double norm = Math.sqrt(q.x() * q.x() + q.y() * q.y() + q.z() * q.z() + q.w() * q.w());
            double vec = Math.sqrt(q.x() * q.x() + q.y() * q.y() + q.z() * q.z());
            double i = q.w() / norm;
            if (i > 1) {
                i = 1;
            }
            if (i < -1) {
                i = -1;
            }
            double theta = Math.acos(i);
            double factor = vec == 0 ? 0 : theta / vec;
            return q.set(
                    (float) (q.x() * factor),
                    (float) (q.y() * factor),
                    (float) (q.z() * factor),
                    (float) Math.log(norm)
            );
        }

        public static Quaternionf exp(Quaternionf q) {
            double magnitude = Math.sqrt(q.x() * q.x() + q.y() * q.y() + q.z() * q.z());
            double expW = Math.exp(q.w());
            double sinMagnitude = Math.sin(magnitude);
            double coef = magnitude == 0 ? 0 : expW * sinMagnitude / magnitude;
            return q.set(
                    (float) (coef * q.x()),
                    (float) (coef * q.y()),
                    (float) (coef * q.z()),
                    (float) (expW * Math.cos(magnitude))
            );
        }

        public static float[] slerp(float[] from, float[] to, float alpha) {
            float ax = from[0];
            float ay = from[1];
            float az = from[2];
            float aw = from[3];
            float bx = to[0];
            float by = to[1];
            float bz = to[2];
            float bw = to[3];

            float dot = ax * bx + ay * by + az * bz + aw * bw;
            if (dot < 0) {
                bx = -bx;
                by = -by;
                bz = -bz;
                bw = -bw;
                dot = -dot;
            }
            float epsilon = 1e-6f;
            float s0, s1;
            if ((1.0 - dot) > epsilon) {
                float omega = (float) Math.acos(dot);
                float invSinOmega = 1.0f / (float) Math.sin(omega);
                s0 = (float) Math.sin((1.0 - alpha) * omega) * invSinOmega;
                s1 = (float) Math.sin(alpha * omega) * invSinOmega;
            } else {
                s0 = 1.0f - alpha;
                s1 = alpha;
            }
            float[] result = new float[4];
            result[0] = s0 * ax + s1 * bx;
            result[1] = s0 * ay + s1 * by;
            result[2] = s0 * az + s1 * bz;
            result[3] = s0 * aw + s1 * bw;
            return result;
        }

        public static Quaternionf slerp(Quaternionf from, Quaternionf to, float alpha) {
            float ax = from.x();
            float ay = from.y();
            float az = from.z();
            float aw = from.w();
            float bx = to.x();
            float by = to.y();
            float bz = to.z();
            float bw = to.w();

            float dot = ax * bx + ay * by + az * bz + aw * bw;
            if (dot < 0) {
                bx = -bx;
                by = -by;
                bz = -bz;
                bw = -bw;
                dot = -dot;
            }
            float epsilon = 1e-6f;
            float s0, s1;
            if ((1.0 - dot) > epsilon) {
                float omega = (float) Math.acos(dot);
                float invSinOmega = 1.0f / (float) Math.sin(omega);
                s0 = (float) Math.sin((1.0 - alpha) * omega) * invSinOmega;
                s1 = (float) Math.sin(alpha * omega) * invSinOmega;
            } else {
                s0 = 1.0f - alpha;
                s1 = alpha;
            }
            float rx = s0 * ax + s1 * bx;
            float ry = s0 * ay + s1 * by;
            float rz = s0 * az + s1 * bz;
            float rw = s0 * aw + s1 * bw;
            return new Quaternionf(rx, ry, rz, rw);
        }

        /*
        Given two quaternions A and B, find the quaternion C such that the result of A multiplied by C is equal to B.
        Solve the following equations:
             aw*ci -ak*cj +aj*ck +ai*cw = bi
             ak*ci +aw*cj -ai*ck +aj*cw = bj
            -aj*ci +ai*cj +aw*ck +ak*cw = bk
            -ai*ci -aj*cj -ak*ck +aw*cw = bw
        */
        public static float[] getRelative(float[] qa, float[] qb) {
            float[][] coefficients = {
                    {qa[3], -qa[2], qa[1], qa[0]},
                    {qa[2], qa[3], -qa[0], qa[1]},
                    {-qa[1], qa[0], qa[3], qa[2]},
                    {-qa[0], -qa[1], -qa[2], qa[3]},
            };
            float[] constants = {qb[0], qb[1], qb[2], qb[3]};
            return solveEquations(coefficients, constants);
        }
        public static Quaternionf getRelative(Quaternionf qa, Quaternionf qb) {
            float[][] coefficients = {
                    {qa.w(), -qa.z(), qa.y(), qa.x()},
                    {qa.z(), qa.w(), -qa.x(), qa.y()},
                    {-qa.y(), qa.x(), qa.w(), qa.z()},
                    {-qa.x(), -qa.y(), -qa.z(), qa.w()},
            };
            float[] constants = {qb.x(), qb.y(), qb.z(), qb.w()};
            float[] result = solveEquations(coefficients, constants);
            return new Quaternionf(result[0], result[1], result[2], result[3]);
        }

        public static Pair<Float, Vector3f> getAngleAndAxis(Quaternionf quaternion) {
            double angle = 2 * Math.acos(quaternion.w());
            double sin = Math.sin(angle / 2);
            // 旋转角为 0 或者 2*PI，旋转结果与旋转轴无关
            if (sin == 0) {
                return Pair.of(0f, new Vector3f(0, 0, 0));
            }
            Vector3f axis = new Vector3f(quaternion.x(), quaternion.y(), quaternion.z());
            axis.mul((float) (1 / sin));
            return Pair.of((float) angle, axis);
        }

        public static Pair<Float, Vector3f> getAngleAndAxis(float[] quaternion) {
            double angle = 2 * Math.acos(quaternion[3]);
            double sin = Math.sin(angle / 2);
            // 旋转角为 0 或者 2*PI，旋转结果与旋转轴无关
            if (sin == 0) {
                return Pair.of(0f, new Vector3f(0, 0, 0));
            }
            Vector3f axis = new Vector3f(quaternion[0], quaternion[1], quaternion[2]);
            axis.mul((float) (1 / sin));
            return Pair.of((float) angle, axis);
        }

        public static Quaternionf multiply(Quaternionf quaternion, float multiplier) {
            Pair<Float, Vector3f> angleAndAxis = getAngleAndAxis(quaternion);
            float newAngle = angleAndAxis.getLeft() * multiplier;
            Vector3f axis = angleAndAxis.getRight();
            double sin = Math.sin(newAngle / 2);
            double cos = Math.cos(newAngle / 2);
            axis.mul((float) sin);
            return new Quaternionf(axis.x(), axis.y(), axis.z(), (float) cos);
        }

        public static float[] multiply(float[] quaternion, float multiplier) {
            Pair<Float, Vector3f> angleAndAxis = getAngleAndAxis(quaternion);
            float newAngle = angleAndAxis.getLeft() * multiplier;
            Vector3f axis = angleAndAxis.getRight();
            double sin = Math.sin(newAngle / 2);
            double cos = Math.cos(newAngle / 2);
            axis.mul((float) sin);
            return new float[]{axis.x(), axis.y(), axis.z(), (float) cos};
        }

        public static float[] splineCurve(float[][] quaternions, float tension, float alpha) {
            if (quaternions.length != 4) {
                throw new IllegalArgumentException("y value length must be 4 when doing catmull-rom spline");
            }
            if (tension < 0 || tension > 1) {
                throw new IllegalArgumentException("tension must be 0~1 when doing catmull-rom spline");
            }
            float[] angles0 = toEulerAngles(quaternions[0]);
            float[] angles1 = toEulerAngles(quaternions[1]);
            float[] angles2 = toEulerAngles(quaternions[2]);
            float[] angles3 = toEulerAngles(quaternions[3]);
            float[] result = new float[3];
            for (int i = 0; i < 3; i++) {
                float[] input = new float[]{angles0[i], angles1[i], angles2[i], angles3[i]};
                result[i] = MathUtil.splineCurve(input, tension, alpha);
            }
            return fromEulerAngles(result[0], result[1], result[2]);
        }
    }

    /**
     * 基于随机关键点和 smoothstep 插值的平滑随机噪声生成器
     * <ul>
     *     <li>在指定范围内生成随机目标值，并通过三次平滑插值生成连续变化的输出</li>
     *     <li>适用于枪械后坐力、镜头晃动等需要自然随机扰动的动画效果</li>
     * </ul>
     */
    public static class SmoothRandomNoise {

        private final Random random = new Random();
        private final float rangeDown;
        private final float rangeUp;
        private final long periodMs;

        private float prevNum;
        private float num;
        private float value;

        private long prevTime;
        private boolean reverse = false;

        public SmoothRandomNoise(float rangeDown, float rangeUp, long periodMs) {
            this.rangeDown = rangeDown;
            this.rangeUp = rangeUp;
            this.periodMs = periodMs;
            this.reset(0);
        }

        private static double easeInterpolate(double x) {
            return (3 * Math.pow(x, 2) - 2 * Math.pow(x, 3));
        }

        public void setReverse(boolean reverse) {
            this.reverse = reverse;
        }

        /**
         * 重置噪声状态。
         */
        public void reset(long currentTimeMs) {
            prevTime = currentTimeMs;

            prevNum = random.nextFloat() * (rangeUp - rangeDown) + rangeDown;
            num = random.nextFloat() * (rangeUp - rangeDown) + rangeDown;

            if (reverse && prevNum * num > 0) {
                num = -num;
            }

            value = prevNum;
        }

        /**
         * 根据当前时间推进噪声状态。
         */
        public void tick(long currentTimeMs) {
            long periodTime = currentTimeMs - prevTime;
            long repeat = periodTime / periodMs;
            long partialTime = periodTime % periodMs;

            if (repeat == 1) {
                prevNum = num;
                num = random.nextFloat() * (rangeUp - rangeDown) + rangeDown;
                if (reverse && prevNum * num > 0) {
                    num = -num;
                }
                prevTime += periodMs;
            } else if (repeat > 1) {
                prevNum = random.nextFloat() * (rangeUp - rangeDown) + rangeDown;
                num = random.nextFloat() * (rangeUp - rangeDown) + rangeDown;
                if (reverse && prevNum * num > 0) {
                    num = -num;
                }
                prevTime = currentTimeMs - partialTime;
            }

            double x = easeInterpolate((double) partialTime / (double) periodMs);
            value = (float) (prevNum * (1 - x) + num * x);
        }

        /**
         * 推进噪声状态并获取当前值
         */
        public float update_tick_get(long currentTimeMs) {
            tick(currentTimeMs);
            return get();
        }

        /**
         * 获取当前噪声值
         */
        public float get() {
            return value;
        }
    }

    /**
     * 基于二阶微分方程的动态响应计算器
     * <ul>
     *     <li>根据自然频率、阻尼系数和初始响应速度计算系统状态，产生带有惯性和阻尼效果的平滑输出</li>
     *     <li>适用于相机移动、FOV变化、动画插值等需要自然响应效果的场景</li>
     * </ul>
     */
    public static class SecondOrderDynamics {

        private final float k1;
        private final float k2;
        private final float k3;

        private float py;
        private float pyd;
        private float px;

        private float target;

        /**
         * 创建一个二阶动态系统
         * @param f  自然频率，决定系统响应速度
         * @param z  阻尼系数，决定系统震荡程度
         * @param r  初始响应速度，影响目标变化时的跟随程度
         * @param x0 初始位置
         */
        public SecondOrderDynamics(float f, float z, float r, float x0) {
            k1 = (float) (z / (Math.PI * f));
            k2 = (float) (1 / ((2 * Math.PI * f) * (2 * Math.PI * f)));
            k3 = (float) (r * z / (2 * Math.PI * f));

            py = px = x0;
            pyd = 0;

            target = x0;
        }

        /**
         * 更新目标值
         * @param x 当前目标值
         * @return 平滑处理后的结果
         */
        public float update_tick_get(float x) {
            target = x;
            this.tick();
            return this.get();
        }

        /**
         * 执行一次二阶动态计算
         * <p>
         * 使用固定时间步长 0.05s，与原版 {@code SecondOrderDynamics} 的积分步长一致，
         * 保证过渡动画的速度与原版一致。
         */
        public void tick() {
            // 修正罕见的 NAN 错误
            if (Float.isNaN(py)) {
                py = 0;
            }
            if (Float.isNaN(pyd)) {
                pyd = 0;
            }

            float t = 0.05f;

            float xd = (target - px) / t;
            float y = py + t * pyd;

            pyd = pyd + t * (px + k3 * xd - py - k1 * pyd) / k2;
            px = target;
            py = y;
        }

        /**
         * 获取当前二阶动态系统输出值
         * <ul>
         *     <li>输出值会根据当前位置和速度进行少量预测，用于减少视觉响应延迟</li>
         * </ul>
         * @return 当前处理的值，不会推进计算进度
         */
        public float get() {
            // 修正罕见的 NAN 错误
            if (Float.isNaN(py)) {
                py = 0;
            }
            if (Float.isNaN(pyd)) {
                pyd = 0;
            }

            return py + 0.05f * pyd;
        }

        // --------Deprecated--------

        @Deprecated(forRemoval = true) public float update(float x) {
            return this.update_tick_get(x);
        }

        /**
         * 执行一次二阶动态计算，返回目标值
         * @deprecated 该操作会手动推进计算进度，非状态维护方手动tick，会导致效果不一致
         */
        @Deprecated public float tick_get() {
            this.tick();
            return this.get();
        }
    }

    /**
     * 基于缓动曲线的插值计算器
     * <ul>
     *     <li>提供非线性插值函数，将线性变化转换为具有不同速度曲线的平滑过渡</li>
     *     <li>适用于动画、镜头移动、物体运动等需要自然过渡效果的场景</li>
     * </ul>
     */
    public static class Easing {

        /**
         * 计算三次缓出曲线的插值结果
         * <ul>
         *     <li>输入值从 0 到 1 变化时，输出值会以较快速度开始，并逐渐减速接近目标值</li>
         *     <li>适用于需要自然减速效果的动画过渡</li>
         * </ul>
         * @param x 归一化输入值，通常范围为 0 到 1
         * @return 缓动后的归一化输出值
         */
        public static double easeOutCubic(double x) {
            return 1 - Math.pow(1 - x, 3);
        }
    }

    // --------Deprecated--------

    @Deprecated public static final float[] QUATERNION_ONE = Quaternion.QUATERNION_ONE;
    @Deprecated public static float[] toQuaternion(float pitch, float yaw, float roll) {
        return Quaternion.fromEulerAngles(pitch, yaw, roll);
    }
    @Deprecated public static void toQuaternion(float pitch, float yaw, float roll, @NotNull Quaternionf quaternion) {
        Quaternion.set(quaternion, pitch, yaw, roll);
    }
    @Deprecated public static float[] toEulerAngles(Quaternionf q) {
        return Quaternion.toEulerAngles(q);
    }
    @Deprecated public static float[] toEulerAngles(float[] q) {
        return Quaternion.toEulerAngles(q);
    }
    @Deprecated public static float[] inverseQuaternion(float[] quaternion) {
        return Quaternion.inverse(quaternion);
    }
    @Deprecated public static float[] mulQuaternion(float[] q1, float[] q2) {
        return Quaternion.mul(q1, q2);
    }
    @Deprecated public static void blendQuaternion(Quaternionf to, Quaternionf from) {
        Quaternion.blend(to, from);
    }
    @Deprecated public static void normalizeQuaternion(Quaternionf q) {
        Quaternion.normalize(q);
    }
    @Deprecated public static void logQuaternion(Quaternionf q) {
        Quaternion.log(q);
    }
    @Deprecated public static void expQuaternion(Quaternionf q) {
        Quaternion.exp(q);
    }
    @Deprecated public static float[] slerp(float[] from, float[] to, float alpha) {
        return Quaternion.slerp(from, to, alpha);
    }
    @Deprecated public static Quaternionf toQuaternion(float[] q) {
        return Quaternion.of(q);
    }
    @Deprecated public static Quaternionf slerp(Quaternionf from, Quaternionf to, float alpha) {
        return Quaternion.slerp(from, to, alpha);
    }
    @Deprecated public static float[] getRelativeQuaternion(float[] qa, float[] qb) {
        return Quaternion.getRelative(qa, qb);
    }
    @Deprecated public static Quaternionf getRelativeQuaternion(Quaternionf qa, Quaternionf qb) {
        return Quaternion.getRelative(qa, qb);
    }
    @Deprecated public static Pair<Float, Vector3f> getAngleAndAxis(Quaternionf quaternion) {
        return Quaternion.getAngleAndAxis(quaternion);
    }
    @Deprecated public static Pair<Float, Vector3f> getAngleAndAxis(float[] quaternion) {
        return Quaternion.getAngleAndAxis(quaternion);
    }
    @Deprecated public static Quaternionf multiplyQuaternion(Quaternionf quaternion, float multiplier) {
        return Quaternion.multiply(quaternion, multiplier);
    }
    @Deprecated public static float[] multiplyQuaternion(float[] quaternion, float multiplier) {
        return Quaternion.multiply(quaternion, multiplier);
    }
    @Deprecated public static float[] quaternionSplineCurve(float[][] quaternions, float tension, float alpha) {
        return Quaternion.splineCurve(quaternions, tension, alpha);
    }
}
