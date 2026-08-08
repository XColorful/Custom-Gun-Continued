/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.listener.constraint;

import dev.xcolorful.customgun.client.api.animation.AnimationChannelType;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListener;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListenerSupplier;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry._Bone;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry.NodeName;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class ConstraintObject implements IAnimationListenerSupplier {

    public Vector3f translationConstraint = new Vector3f(0, 0, 0);
    public Vector3f rotationConstraint = new Vector3f(0, 0, 0);
    /**
     * 当相机的节点为根时，node为空
     */
    public BedrockPart node;
    /**
     * 当相机的节点不为根时，bone为空
     */
    public _Bone bone;

    @Nullable
    @Override
    public IAnimationListener supplyListeners(String nodeName, AnimationChannelType type) {
        if (!NodeName.CONSTRAINT.matches(nodeName)) return null;

        return switch (type) {
            case ROTATION -> new ConstraintRotateListener(this);
            case TRANSLATION -> new ConstraintTranslateListener(this);
            case SCALE -> null;
            // 增加类型使此处强制编译不通过
        };
    }
}
