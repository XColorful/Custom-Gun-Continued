/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.forge.item.ammo;

import dev.xcolorful.customgun.client.api.item.IItemBEWLR;
import dev.xcolorful.customgun.core.item.ammo.AmmoItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ForgeAmmoItem extends AmmoItem {

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
             * {@code dev.xcolorful.customgun.forgeclient.mixin.item.AmmoItemMixin}注入了{@link IItemBEWLR}
             */
            @Deprecated(since = "1.21.4")
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ((IItemBEWLR) (Object) this).cgc$getBEWLR();
            }
        });
    }
}
