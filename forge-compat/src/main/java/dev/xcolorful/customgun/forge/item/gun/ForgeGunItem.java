/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.forge.item.gun;

import dev.xcolorful.customgun.client.api.item.IItemBEWLR;
import dev.xcolorful.customgun.core.item.gun.GunItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ForgeGunItem extends GunItem {

    @OnlyIn(Dist.CLIENT)
    @Deprecated(since = "1.21.4")
    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        registerClientExtension(consumer);
    }

    /**
     * 预留给扩展模组用:
     * <ul>
     *     <li>扩展模组core模块只能继承{@link GunItem}而不是{@link ForgeGunItem} (除非不想做平台抽象层)</li>
     *     <li>因而留一个static接口</li>
     * </ul>
     * 比如注册<code>MyGunItem extends GunItem</code>并重载final getter/setter来锁定子Manager
     */
    @OnlyIn(Dist.CLIENT)
    @Deprecated(since = "1.21.4")
    public static void registerClientExtension(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            /**
             * {@code dev.xcolorful.customgun.forgeclient.mixin.item.GunItemMixin}注入了{@link IItemBEWLR}
             */
            @Deprecated(since = "1.21.4")
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ((IItemBEWLR) (Object) this).cgc$getBEWLR();
            }
        });
    }
}
