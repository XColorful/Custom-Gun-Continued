/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.listener.model;

import dev.xcolorful.customgun.client.api.animation.ObjectAnimationChannel;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListener;
import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockRenderer;
import dev.xcolorful.customgun.core.util.MathUtil;

public class ModelRotateListener implements IAnimationListener {
    
    private final IBedrockRenderer renderer;
    
    public ModelRotateListener(final IBedrockRenderer renderer) {
        this.renderer = renderer;
    }
    @Override
    public ObjectAnimationChannel.ChannelType getType() {
        return ObjectAnimationChannel.ChannelType.ROTATION;
    }

    @Override
    public void update(float[] values, boolean blend) {
        if (values.length == 4) {
            values = MathUtil.Quaternion.toEulerAngles(values);
        }
        if (blend) {
            float[] angles = MathUtil.Quaternion.toEulerAngles(this.renderer.getAdditionalQuaternion());
            values[0] += angles[0];
            values[1] += angles[1];
            values[2] += angles[2];
        }
        MathUtil.Quaternion.set(this.renderer.getAdditionalQuaternion(), values[0], values[1], values[2]);
    }

    @Override
    public float[] initialValue() {
        return MathUtil.Quaternion.fromEulerAngles(this.renderer.getRotateAngleX(), this.renderer.getRotateAngleY(), this.renderer.getRotateAngleZ());
    }
}
