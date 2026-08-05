/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.neoforge.item.attachment;

import dev.xcolorful.customgun.core.item.attachment.AttachmentItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class NeoAttachmentItem extends AttachmentItem {

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        registerClientExtension(consumer);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerClientExtension(Consumer<IClientItemExtensions> consumer) {
    }
}
