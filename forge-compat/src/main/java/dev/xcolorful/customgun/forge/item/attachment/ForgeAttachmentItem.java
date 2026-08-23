/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.forge.item.attachment;

import dev.xcolorful.customgun.client.api.item.IItemBEWLR;
import dev.xcolorful.customgun.core.item.attachment.AttachmentItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ForgeAttachmentItem extends AttachmentItem {

    @OnlyIn(Dist.CLIENT)
    @Deprecated(since = "1.21.4")
    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        registerClientExtension(consumer);
    }

    @OnlyIn(Dist.CLIENT)
    @Deprecated(since = "1.21.4")
    public static void registerClientExtension(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            /**
             * {@code dev.xcolorful.customgun.forgeclient.mixin.item.AttachmentItemMixin}注入了{@link IItemBEWLR}
             */
            @Deprecated(since = "1.21.4")
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ((IItemBEWLR) (Object) this).cgc$getBEWLR();
            }
        });
    }
}
