/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.util;

import net.minecraft.resources.Identifier;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.network._AttachmentInstallabilityCache;

/**
 * @deprecated 有刺鼻的味道
 * Go to {@link _AttachmentInstallabilityCache}
 */
@Deprecated(forRemoval = true)
public class AllowAttachmentTagMatcher {

    @Deprecated
    public static boolean match(Identifier gunId, Identifier attachmentId) {
        return ResourceApi.hasAttachmentInstallability(attachmentId, gunId);
    }
}
