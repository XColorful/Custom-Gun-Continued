/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.listener.constraint;

import dev.xcolorful.customgun.client.api.animation.ObjectAnimationChannel;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListener;

public class ConstraintTranslateListener implements IAnimationListener {

    private final ConstraintObject constraint;

    public ConstraintTranslateListener(ConstraintObject constraint) {
        this.constraint = constraint;
    }
    @Override
    public ObjectAnimationChannel.ChannelType getType() {
        return ObjectAnimationChannel.ChannelType.TRANSLATION;
    }

    @Override
    public void update(float[] values, boolean blend) {
        if (blend) {
            constraint.translationConstraint.set(
                    Math.max(constraint.translationConstraint.x(), values[0] * 16),
                    Math.max(constraint.translationConstraint.y(), values[1] * 16),
                    Math.max(constraint.translationConstraint.z(), values[2] * 16)
            );
        } else {
            constraint.translationConstraint.set(
                    values[0] * 16,
                    values[1] * 16,
                    values[2] * 16
            );
        }
    }

    @Override
    public float[] initialValue() {
        float[] recover = new float[3];
        if (constraint.bone != null) {
            recover[0] = constraint.bone.getPivot()[0] / 16f;
            recover[1] = -constraint.bone.getPivot()[1] / 16f;
            recover[2] = constraint.bone.getPivot()[2] / 16f;
        } else {
            recover[0] = constraint.node.x / 16f;
            recover[1] = constraint.node.y / 16f;
            recover[2] = constraint.node.z / 16f;
        }
        return recover;
    }
}
