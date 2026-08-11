/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model.bedrock;

import org.joml.Vector3f;

public record BedrockVertex(float x, float y, float z, float u, float v) {

    public BedrockVertex remap(float u, float v) {
        return new BedrockVertex(this.x, this.y, this.z, u, v);
    }

    @Deprecated public BedrockVertex(Vector3f pos, float u, float v) {
        this(pos.x, pos.y, pos.z, u, v);
    }
}
