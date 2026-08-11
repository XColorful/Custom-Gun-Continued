/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.forge.item.attachment;

import dev.xcolorful.customgun.client.renderer.item.AttachmentItemRenderer;
import dev.xcolorful.customgun.core.item.attachment.AttachmentItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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
        consumer.accept(new IClientItemExtensions() {
            AttachmentItemRenderer attachmentItemRenderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.attachmentItemRenderer == null) {
                    Minecraft mc = Minecraft.getInstance();
                    this.attachmentItemRenderer = new AttachmentItemRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
                }

                return this.attachmentItemRenderer;
            }
        });
    }
}
