/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.model.bedrock;

import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public interface IBedrockRenderer extends IBedrockRender {

    static IBedrockRenderer of(@NotNull BedrockPart bedrockPart) {
        return (IBedrockRenderer) bedrockPart;
    }

    BedrockPart getModelRenderer();

    // --------Getter & Setter--------

    float getRotateAngleX();
    float getRotateAngleY();
    float getRotateAngleZ();
    float getOffsetX();
    float getOffsetY();
    float getOffsetZ();
    float getRotationPointX();
    float getRotationPointY();
    float getRotationPointZ();
    boolean isVisible();
    float getInitRotateAngleX();
    float getInitRotateAngleY();
    float getInitRotateAngleZ();
    Quaternionf getAdditionalQuaternion();
    float getScaleX();
    float getScaleY();
    float getScaleZ();

    void setRotateAngleX(float xRot);
    void setRotateAngleY(float yRot);
    void setRotateAngleZ(float zRot);
    void setOffsetX(float offsetX);
    void setOffsetY(float offsetY);
    void setOffsetZ(float offsetZ);
    void setVisible(boolean visible);
    void setAdditionalQuaternion(Quaternionf quaternion);
    void setScaleX(float scaleX);
    void setScaleY(float scaleY);
    void setScaleZ(float scaleZ);

    default void addOffsetX(float offsetX) {
        setOffsetX(getOffsetX() + offsetX);
    }
    default void addOffsetY(float offsetY) {
        setOffsetY(getOffsetY() + offsetY);
    }
    default void addOffsetZ(float offsetZ) {
        setOffsetZ(getOffsetZ() + offsetZ);
    }

    @Deprecated default boolean isHidden() {
        return !this.isVisible();
    }
    @Deprecated default void setHidden(boolean hidden) {
        this.setVisible(!hidden);
    }
}
