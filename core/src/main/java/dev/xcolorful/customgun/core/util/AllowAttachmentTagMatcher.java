/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.util;

import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.network._AttachmentInstallabilityCache;
import net.minecraft.resources.ResourceLocation;

/**
 * @deprecated 有刺鼻的味道
 * Go to {@link _AttachmentInstallabilityCache}
 */
@Deprecated(forRemoval = true)
public class AllowAttachmentTagMatcher {

    @Deprecated
    public static boolean match(ResourceLocation gunId, ResourceLocation attachmentId) {
        return ResourceApi.hasAttachmentInstallability(attachmentId, gunId);
    }
}
