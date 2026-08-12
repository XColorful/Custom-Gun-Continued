/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.neoforge.item.ammo;

import dev.xcolorful.customgun.client.renderer.item.AmmoItemRenderer;
import dev.xcolorful.customgun.core.item.ammo.AmmoItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class NeoAmmoItem extends AmmoItem {

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        registerClientExtension(consumer);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerClientExtension(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            AmmoItemRenderer ammoItemRenderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.ammoItemRenderer == null) {
                    Minecraft mc = Minecraft.getInstance();
                    this.ammoItemRenderer = new AmmoItemRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
                }

                return this.ammoItemRenderer;
            }
        });
    }
}
