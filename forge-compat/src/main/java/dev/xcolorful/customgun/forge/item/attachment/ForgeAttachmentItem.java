/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.forge.item.attachment;

import dev.xcolorful.customgun.core.item.attachment.AttachmentItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class ForgeAttachmentItem extends AttachmentItem {

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        registerClientExtension(consumer);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerClientExtension(Consumer<IClientItemExtensions> consumer) {
    }
}
