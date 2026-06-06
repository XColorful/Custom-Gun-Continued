/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * 改成跟 BattleRoyale 同构的写法
 */

package xiao.customgun.core.init.registry;

import net.minecraft.world.item.Item;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.init.registry.IRegistrar;
import xiao.customgun.core.api.init.registry.IRegistryObject;
import xiao.customgun.core.api.minecraft.item.ItemType;
import xiao.customgun.core.item.ammo.AmmoItem;
import xiao.customgun.core.item.ammobox.AmmoBoxItem;
import xiao.customgun.core.item.attachment.AttachmentItem;
import xiao.customgun.core.item.gun.GunItem;

public class ModItems {
    public static final IRegistrar<Item> ITEMS = CustomGun.getRegistrarFactory().createItems(CustomGun.MOD_ID);


    public static final Item.Properties CUSTOM_ITEM_PROPERTY = new Item.Properties()
            .stacksTo(1); // 堆叠数会重载getter来修改

    public static final IRegistryObject<Item> GUN = ITEMS.register(ItemType.GUN.getTagName(), GunItem::new);
    public static final IRegistryObject<Item> ATTACHMENT = ITEMS.register(ItemType.ATTACHMENT.getTagName(), AttachmentItem::new);
    public static final IRegistryObject<Item> AMMO = ITEMS.register(ItemType.AMMO.getTagName(), AmmoItem::new);
    public static final IRegistryObject<Item> AMMO_BOX = ITEMS.register(ItemType.AMMO_BOX.getTagName(), AmmoBoxItem::new);
}
