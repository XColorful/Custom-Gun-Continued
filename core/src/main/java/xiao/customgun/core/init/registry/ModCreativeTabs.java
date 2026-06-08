/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.init.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.init.registry.IRegistrar;
import xiao.customgun.core.api.init.registry.IRegistryObject;
import xiao.customgun.core.api.minecraft.tab.AmmoTab;
import xiao.customgun.core.api.minecraft.tab.AttachmentTab;
import xiao.customgun.core.api.minecraft.tab.GunTab;

import static xiao.customgun.core.api.item.ammo.AmmoCategory.*;
import static xiao.customgun.core.api.item.attachment.AttachmentCategory.*;
import static xiao.customgun.core.api.item.gun.GunCategory.*;

public class ModCreativeTabs {
    public static final IRegistrar<CreativeModeTab> TABS = CustomGun.getRegistrarFactory().createCreativeTabs(CustomGun.MOD_ID);


    // --------Ammo--------

    public static IRegistryObject<CreativeModeTab> AMMO_TAB = TABS.register(AMMO.getCategoryName(), () -> CreativeModeTab.builder()
            .title(AMMO.getCategoryLang())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(AmmoTab.buildAmmoItems(AMMO)))
            .build()
    );

    // --------AttachmentCategory--------

    public static IRegistryObject<CreativeModeTab> ATTACHMENT_MUZZLE_TAB = TABS.register(MUZZLE.getCategoryName(), () -> CreativeModeTab.builder()
            .title(MUZZLE.getCategoryLang()).withTabsBefore(AMMO_TAB.getRegistryName())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(AttachmentTab.buildAttachmentItems(MUZZLE)))
            .build()
    );
    public static IRegistryObject<CreativeModeTab> ATTACHMENT_LASER_TAB = TABS.register(LASER.getCategoryName(), () -> CreativeModeTab.builder()
            .title(LASER.getCategoryLang()).withTabsBefore(ATTACHMENT_MUZZLE_TAB.getRegistryName())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(AttachmentTab.buildAttachmentItems(LASER)))
            .build()
    );
    public static IRegistryObject<CreativeModeTab> ATTACHMENT_GRIP_TAB = TABS.register(GRIP.getCategoryName(), () -> CreativeModeTab.builder()
            .title(GRIP.getCategoryLang()).withTabsBefore(ATTACHMENT_LASER_TAB.getRegistryName())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(AttachmentTab.buildAttachmentItems(GRIP)))
            .build()
    );
    public static IRegistryObject<CreativeModeTab> ATTACHMENT_MAGAZINE_TAB = TABS.register(MAGAZINE.getCategoryName(), () -> CreativeModeTab.builder()
            .title(MAGAZINE.getCategoryLang()).withTabsBefore(ATTACHMENT_GRIP_TAB.getRegistryName())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(AttachmentTab.buildAttachmentItems(MAGAZINE)))
            .build()
    );
    public static IRegistryObject<CreativeModeTab> ATTACHMENT_SCOPE_TAB = TABS.register(SCOPE.getCategoryName(), () -> CreativeModeTab.builder()
            .title(SCOPE.getCategoryLang()).withTabsBefore(ATTACHMENT_MAGAZINE_TAB.getRegistryName())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(AttachmentTab.buildAttachmentItems(SCOPE)))
            .build()
    );
    public static IRegistryObject<CreativeModeTab> ATTACHMENT_STOCK_TAB = TABS.register(STOCK.getCategoryName(), () -> CreativeModeTab.builder()
            .title(STOCK.getCategoryLang()).withTabsBefore(ATTACHMENT_SCOPE_TAB.getRegistryName())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(AttachmentTab.buildAttachmentItems(STOCK)))
            .build()
    );

    // --------GunCategory--------

    public static IRegistryObject<CreativeModeTab> GUN_SHOTGUN_TAB = TABS.register(SHOTGUN.getCategoryName(), () -> CreativeModeTab.builder()
            .title(SHOTGUN.getCategoryLang()).withTabsBefore(ATTACHMENT_STOCK_TAB.getRegistryName())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(SHOTGUN)))
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_PISTOL_TAB = TABS.register(PISTOL.getCategoryName(), () -> CreativeModeTab.builder()
            .title(PISTOL.getCategoryLang()).withTabsBefore(GUN_SHOTGUN_TAB.getRegistryName())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(PISTOL)))
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_RIFLE_TAB = TABS.register(RIFLE.getCategoryName(), () -> CreativeModeTab.builder()
            .title(RIFLE.getCategoryLang()).withTabsBefore(GUN_PISTOL_TAB.getRegistryName())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(RIFLE)))
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_SNIPER_TAB = TABS.register(SNIPER.getCategoryName(), () -> CreativeModeTab.builder()
            .title(SNIPER.getCategoryLang()).withTabsBefore(GUN_RIFLE_TAB.getRegistryName())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(SNIPER)))
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_MG_TAB = TABS.register(MG.getCategoryName(), () -> CreativeModeTab.builder()
            .title(MG.getCategoryLang()).withTabsBefore(GUN_SNIPER_TAB.getRegistryName())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(MG)))
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_SMG_TAB = TABS.register(SMG.getCategoryName(), () -> CreativeModeTab.builder()
            .title(SMG.getCategoryLang()).withTabsBefore(GUN_MG_TAB.getRegistryName())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(SMG)))
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_RPG_TAB = TABS.register(RPG.getCategoryName(), () -> CreativeModeTab.builder()
            .title(RPG.getCategoryLang()).withTabsBefore(GUN_SMG_TAB.getRegistryName())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(RPG)))
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_CUSTOM_TAB = TABS.register(CUSTOM.getCategoryName(), () -> CreativeModeTab.builder()
            .title(CUSTOM.getCategoryLang()).withTabsBefore(GUN_RPG_TAB.getRegistryName())
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(CUSTOM)))
            .build()
    );
}
