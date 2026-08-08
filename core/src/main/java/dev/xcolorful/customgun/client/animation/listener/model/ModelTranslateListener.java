/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.listener.model;

import dev.xcolorful.customgun.client.api.animation.AnimationChannelType;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListener;
import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockRenderer;
import dev.xcolorful.customgun.client.model.AnimatedModelObject;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry._Bone;

import javax.annotation.Nullable;

public class ModelTranslateListener implements IAnimationListener {

    private final IBedrockRenderer renderer;
    private final @Nullable _Bone bone;

    public ModelTranslateListener(AnimatedModelObject model, IBedrockRenderer renderer, String nodeName) {
        this.renderer = renderer;
        // 如果当前 node 是根 node（也就是包含于 shouldRender 中），则获取其 bone，以便后续计算相对位移 offset
        if (model.getShouldRender().contains(renderer.getModelRenderer())) {
            this.bone = model.getIndexBones().get(nodeName);
        } else {
            this.bone = null;
        }
    }
    @Override
    public AnimationChannelType getType() {
        return AnimationChannelType.TRANSLATION;
    }

    @Override
    public void update(float[] values, boolean blend) {
        if (blend) {
            // 约束组动画是特殊值，不参与混合
            this.renderer.addOffsetX(values[0]);
            this.renderer.addOffsetY(-values[1]);
            this.renderer.addOffsetZ(values[2]);
        } else {
            this.renderer.setOffsetX(values[0]);
            this.renderer.setOffsetY(-values[1]);
            this.renderer.setOffsetZ(values[2]);
        }
    }

    @Override
    public float[] initialValue() {
        // 目标是让 offset 过渡为 0
        float[] recover = new float[3];
        if (this.bone != null) {
            recover[0] = this.bone.getPivot()[0] / 16f;
            recover[1] = -this.bone.getPivot()[1] / 16f;
            recover[2] = this.bone.getPivot()[2] / 16f;
        } else {
            recover[0] = this.renderer.getRotationPointX() / 16f;
            recover[1] = this.renderer.getRotationPointY() / 16f;
            recover[2] = this.renderer.getRotationPointZ() / 16f;
        }
        return recover;
    }
}
