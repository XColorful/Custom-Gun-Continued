/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.model;

import dev.xcolorful.customgun.client.resource.assets.display._ModelNodeTextDisplay;

import java.util.Map;

public interface IModelTextConsumer {

    void setTextShowList(Map<String, _ModelNodeTextDisplay> modelNodeTextDisplay);
}
