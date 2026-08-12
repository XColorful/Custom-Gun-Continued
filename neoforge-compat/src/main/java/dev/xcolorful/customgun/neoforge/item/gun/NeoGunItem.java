/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.neoforge.item.gun;

import dev.xcolorful.customgun.client.renderer.item.GunItemRenderer;
import dev.xcolorful.customgun.core.item.gun.GunItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class NeoGunItem extends GunItem {

    /**
     * 预留给扩展模组用:
     * <ul>
     *     <li>扩展模组core模块只能继承{@link GunItem}而不是{@link NeoGunItem} (除非不想做平台抽象层)</li>
     *     <li>因而留一个static接口</li>
     * </ul>
     * 比如注册<code>MyGunItem extends GunItem</code>并重载final getter/setter来锁定子Manager
     */
    @OnlyIn(Dist.CLIENT)
    public static void registerClientExtension(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            GunItemRenderer gunItemRenderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.gunItemRenderer == null) {
                    this.gunItemRenderer = new GunItemRenderer();
                }

                return this.gunItemRenderer;
            }
        });
    }
}
