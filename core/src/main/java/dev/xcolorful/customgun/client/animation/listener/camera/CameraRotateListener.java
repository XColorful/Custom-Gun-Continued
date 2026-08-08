/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.listener.camera;

import dev.xcolorful.customgun.client.api.animation.AnimationChannelType;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListener;
import dev.xcolorful.customgun.core.util.MathUtil;
import org.joml.Quaternionf;

public class CameraRotateListener implements IAnimationListener {

    private final CameraAnimationObject camera;

    public CameraRotateListener(CameraAnimationObject camera) {
        this.camera = camera;
    }
    @Override
    public AnimationChannelType getType() {
        return AnimationChannelType.ROTATION;
    }

    @Override
    public void update(float[] values, boolean blend) {
        if (values.length == 4) {
            values = MathUtil.Quaternion.toEulerAngles(values);
        }

        float xRot = values[0];
        float yRot = values[1];
        float zRot = -values[2];
        // 在关键帧中储存的旋转数值并不是摄像头的旋转数值，是世界箱体的旋转数值
        // 最终需要存入rotationQuaternion的是摄像机的旋转（即世界箱体旋转的反相）
        if (blend) {
            Quaternionf quaternion = MathUtil.Quaternion.of(xRot, yRot, zRot);
            MathUtil.Quaternion.blend(camera.rotationQuaternion, quaternion);
        } else {
            MathUtil.Quaternion.set(camera.rotationQuaternion, xRot, yRot, zRot);
        }
    }

    @Override
    public float[] initialValue() {
        return MathUtil.Quaternion.fromEulerAngles(camera.cameraRenderer.getRotateAngleX(), camera.cameraRenderer.getRotateAngleY(), camera.cameraRenderer.getRotateAngleZ());
    }
}
