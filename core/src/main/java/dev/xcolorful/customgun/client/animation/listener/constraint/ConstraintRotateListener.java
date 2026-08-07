/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.listener.constraint;

import dev.xcolorful.customgun.client.api.animation.ObjectAnimationChannel;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListener;
import dev.xcolorful.customgun.core.util.MathUtil;

public class ConstraintRotateListener implements IAnimationListener {

    private final ConstraintObject constraint;

    public ConstraintRotateListener(ConstraintObject constraint) {
        this.constraint = constraint;
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
            constraint.rotationConstraint.set(
                    (float) Math.max(constraint.rotationConstraint.x(), MathUtil.toDegreePositive(values[0])),
                    (float) Math.max(constraint.rotationConstraint.y(), MathUtil.toDegreePositive(values[1])),
                    (float) Math.max(constraint.rotationConstraint.z(), MathUtil.toDegreePositive(values[2]))
            );
        } else {
            constraint.rotationConstraint.set(
                    (float) MathUtil.toDegreePositive(values[0]),
                    (float) MathUtil.toDegreePositive(values[1]),
                    (float) MathUtil.toDegreePositive(values[2]));
        }
    }

    @Override
    public float[] initialValue() {
        return MathUtil.Quaternion.fromEulerAngles(constraint.node.xRot, constraint.node.yRot, constraint.node.zRot);
    }
}
