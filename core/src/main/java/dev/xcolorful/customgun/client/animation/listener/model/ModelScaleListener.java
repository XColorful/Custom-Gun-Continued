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

public class ModelScaleListener implements IAnimationListener {
    
    private final IBedrockRenderer renderer;
    
    public ModelScaleListener(IBedrockRenderer renderer) {
        this.renderer = renderer;
    }
    @Override
    public ObjectAnimationChannel.ChannelType getType() {
        return ObjectAnimationChannel.ChannelType.SCALE;
    }

    @Override
    public void update(float[] values, boolean blend) {
        if (blend) {
            this.renderer.setScaleX(this.renderer.getScaleX() * values[0]);
            this.renderer.setScaleY(this.renderer.getScaleY() * values[1]);
            this.renderer.setScaleZ(this.renderer.getScaleZ() * values[2]);
        } else {
            this.renderer.setScaleX(values[0]);
            this.renderer.setScaleY(values[1]);
            this.renderer.setScaleZ(values[2]);
        }
    }

    @Override
    public float[] initialValue() {
        return new float[]{1f, 1f, 1f};
    }
}
