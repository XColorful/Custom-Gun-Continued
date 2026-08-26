/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * 改成跟 BattleRoyale 同构的写法
 */

package dev.xcolorful.customgun.core.init.registry;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.init.registry.IRegistrar;
import dev.xcolorful.customgun.core.api.init.registry.IRegistryObject;
import dev.xcolorful.customgun.core.api.minecraft.item.ItemType;
import dev.xcolorful.customgun.core.item.ammo.AmmoItem;
import dev.xcolorful.customgun.core.item.ammobox.AmmoBoxItem;
import dev.xcolorful.customgun.core.item.attachment.AttachmentItem;
import dev.xcolorful.customgun.core.item.gun.GunItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Function;

/**
 * <ul>
 *     <li>用 {@link IRegistrar#register} 的是正常注册，获取的就是传入的类型</li>
 *     <li>用 {@link IRegistrar#registerItem} 的是 hack 实现，也可以当传入的类型用</li>
 * </ul>
 * 类型检查用<code>instanceof</code>而不要用<code>getClass</code>，除非调用方不打算做平台抽象层
 */
public class ModItems {
    public static final IRegistrar<Item> ITEMS = CustomGun.getRegistrarFactory().createItems(CustomGun.MOD_ID);

    public static final Function<ResourceLocation, Item.Properties> CUSTOM_ITEM_PROPERTY = (registryLocation) -> new Item.Properties()
            .stacksTo(1); // 堆叠数会重载getter来修改


    // 暂时不需要hack注册，因此还是用register而不是registerItem
    public static final IRegistryObject<GunItem> GUN = ITEMS.register(ItemType.GUN.getTagName(), GunItem::new);
    public static final IRegistryObject<AttachmentItem> ATTACHMENT = ITEMS.register(ItemType.ATTACHMENT.getTagName(), AttachmentItem::new);
    public static final IRegistryObject<AmmoItem> AMMO = ITEMS.register(ItemType.AMMO.getTagName(), AmmoItem::new);
    public static final IRegistryObject<Item> AMMO_BOX = ITEMS.register(ItemType.AMMO_BOX.getTagName(), AmmoBoxItem::new);
}
