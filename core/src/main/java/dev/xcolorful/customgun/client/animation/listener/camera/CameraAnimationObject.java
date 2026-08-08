/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.listener.camera;

import dev.xcolorful.customgun.client.api.animation.AnimationChannelType;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListener;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListenerSupplier;
import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockRenderer;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry.NodeName;
import org.joml.Quaternionf;

public class CameraAnimationObject implements IAnimationListenerSupplier {
    /**
     * 存在这个四元数中的旋转是世界箱体的旋转，而不是摄像头的旋转（二者互为相反数）
     */
    public Quaternionf rotationQuaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);

    /**
     * 当相机的节点为根时，cameraRenderer为空
     */
    public IBedrockRenderer cameraRenderer;

    @Override
    public IAnimationListener supplyListeners(String nodeName,
                                              AnimationChannelType type) {
        if (!NodeName.CAMERA.matches(nodeName)) return null;

        if (type == AnimationChannelType.ROTATION) return new CameraRotateListener(this);
        else return null;
    }
}
