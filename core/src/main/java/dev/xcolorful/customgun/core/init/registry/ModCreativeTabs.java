package dev.xcolorful.customgun.core.init.registry;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.init.registry.IRegistrar;
import dev.xcolorful.customgun.core.api.init.registry.IRegistryObject;
import dev.xcolorful.customgun.core.api.item.ammo.AmmoCategory;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.gun.GunCategory;
import dev.xcolorful.customgun.core.api.minecraft.tab.AmmoTab;
import dev.xcolorful.customgun.core.api.minecraft.tab.AttachmentTab;
import dev.xcolorful.customgun.core.api.minecraft.tab.GunTab;
import net.minecraft.world.item.CreativeModeTab;

import static dev.xcolorful.customgun.core.api.item.ammo.AmmoCategory.AMMO;
import static dev.xcolorful.customgun.core.api.item.ammo.AmmoCategory.EXPLOSIVE;
import static dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory.*;
import static dev.xcolorful.customgun.core.api.item.gun.GunCategory.*;

/**
 * 使用 {@link AmmoCategory}, {@link AttachmentCategory}, {@link GunCategory}
 * <ul>
 *     <li>加{@link CreativeModeTab.Builder#withSearchBar()}是为了从tab拿的物品不重复显示类别，尤其是子弹</li>
 * </ul>
 */
public class ModCreativeTabs {
    public static final IRegistrar<CreativeModeTab> TABS = CustomGun.getRegistrarFactory().createCreativeTabs(CustomGun.MOD_ID);


    // --------Ammo--------

    public static IRegistryObject<CreativeModeTab> AMMO_TAB = TABS.register(AMMO.getCategoryName(), () -> CreativeModeTab.builder()
            .title(AMMO.getCategoryLang())
            .icon(AmmoTab::buildAmmoTabItem)
            .displayItems((parameters, output) -> output.acceptAll(AmmoTab.buildAmmoItems(AMMO)))
            .withSearchBar()
            .build()
    );
    public static IRegistryObject<CreativeModeTab> EXPLOSIVE_TAB = TABS.register(EXPLOSIVE.getCategoryName(), () -> CreativeModeTab.builder()
            .title(EXPLOSIVE.getCategoryLang()).withTabsBefore(AMMO_TAB.getRegistryName())
            .icon(AmmoTab::buildExplosiveTabItem)
            .displayItems((parameters, output) -> output.acceptAll(AmmoTab.buildAmmoItems(EXPLOSIVE)))
            .withSearchBar()
            .build()
    );

    // --------AttachmentCategory--------

    public static IRegistryObject<CreativeModeTab> ATTACHMENT_MUZZLE_TAB = TABS.register(MUZZLE.getCategoryName(), () -> CreativeModeTab.builder()
            .title(MUZZLE.getCategoryLang()).withTabsBefore(EXPLOSIVE_TAB.getRegistryName())
            .icon(AttachmentTab::buildMuzzleTabItem)
            .displayItems((parameters, output) -> output.acceptAll(AttachmentTab.buildAttachmentItems(MUZZLE)))
            .withSearchBar()
            .build()
    );
    public static IRegistryObject<CreativeModeTab> ATTACHMENT_LASER_TAB = TABS.register(LASER.getCategoryName(), () -> CreativeModeTab.builder()
            .title(LASER.getCategoryLang()).withTabsBefore(ATTACHMENT_MUZZLE_TAB.getRegistryName())
            .icon(AttachmentTab::buildLaserTabItem)
            .displayItems((parameters, output) -> output.acceptAll(AttachmentTab.buildAttachmentItems(LASER)))
            .withSearchBar()
            .build()
    );
    public static IRegistryObject<CreativeModeTab> ATTACHMENT_GRIP_TAB = TABS.register(GRIP.getCategoryName(), () -> CreativeModeTab.builder()
            .title(GRIP.getCategoryLang()).withTabsBefore(ATTACHMENT_LASER_TAB.getRegistryName())
            .icon(AttachmentTab::buildGripTabItem)
            .displayItems((parameters, output) -> output.acceptAll(AttachmentTab.buildAttachmentItems(GRIP)))
            .withSearchBar()
            .build()
    );
    public static IRegistryObject<CreativeModeTab> ATTACHMENT_MAGAZINE_TAB = TABS.register(MAGAZINE.getCategoryName(), () -> CreativeModeTab.builder()
            .title(MAGAZINE.getCategoryLang()).withTabsBefore(ATTACHMENT_GRIP_TAB.getRegistryName())
            .icon(AttachmentTab::buildMagazineTabItem)
            .displayItems((parameters, output) -> output.acceptAll(AttachmentTab.buildAttachmentItems(MAGAZINE)))
            .withSearchBar()
            .build()
    );
    public static IRegistryObject<CreativeModeTab> ATTACHMENT_SCOPE_TAB = TABS.register(SCOPE.getCategoryName(), () -> CreativeModeTab.builder()
            .title(SCOPE.getCategoryLang()).withTabsBefore(ATTACHMENT_MAGAZINE_TAB.getRegistryName())
            .icon(AttachmentTab::buildScopeTabItem)
            .displayItems((parameters, output) -> output.acceptAll(AttachmentTab.buildAttachmentItems(SCOPE)))
            .withSearchBar()
            .build()
    );
    public static IRegistryObject<CreativeModeTab> ATTACHMENT_STOCK_TAB = TABS.register(STOCK.getCategoryName(), () -> CreativeModeTab.builder()
            .title(STOCK.getCategoryLang()).withTabsBefore(ATTACHMENT_SCOPE_TAB.getRegistryName())
            .icon(AttachmentTab::buildStockTabItem)
            .displayItems((parameters, output) -> output.acceptAll(AttachmentTab.buildAttachmentItems(STOCK)))
            .withSearchBar()
            .build()
    );

    // --------GunCategory--------

    public static IRegistryObject<CreativeModeTab> GUN_PISTOL_TAB = TABS.register(PISTOL.getCategoryName(), () -> CreativeModeTab.builder()
            .title(PISTOL.getCategoryLang()).withTabsBefore(ATTACHMENT_STOCK_TAB.getRegistryName())
            .icon(GunTab::buildPistolTabItem)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(PISTOL)))
            .withSearchBar()
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_AR_TAB = TABS.register(AR.getCategoryName(), () -> CreativeModeTab.builder()
            .title(AR.getCategoryLang()).withTabsBefore(GUN_PISTOL_TAB.getRegistryName())
            .icon(GunTab::buildArTabItem)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(AR)))
            .withSearchBar()
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_DMR_TAB = TABS.register(DMR.getCategoryName(), () -> CreativeModeTab.builder()
            .title(DMR.getCategoryLang()).withTabsBefore(GUN_AR_TAB.getRegistryName())
            .icon(GunTab::buildDmrTabItem)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(DMR)))
            .withSearchBar()
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_SR_TAB = TABS.register(SR.getCategoryName(), () -> CreativeModeTab.builder()
            .title(SR.getCategoryLang()).withTabsBefore(GUN_DMR_TAB.getRegistryName())
            .icon(GunTab::buildSrTabItem)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(SR)))
            .withSearchBar()
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_SHOTGUN_TAB = TABS.register(SHOTGUN.getCategoryName(), () -> CreativeModeTab.builder()
            .title(SHOTGUN.getCategoryLang()).withTabsBefore(GUN_SR_TAB.getRegistryName())
            .icon(GunTab::buildShotgunTabItem)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(SHOTGUN)))
            .withSearchBar()
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_SMG_TAB = TABS.register(SMG.getCategoryName(), () -> CreativeModeTab.builder()
            .title(SMG.getCategoryLang()).withTabsBefore(GUN_SHOTGUN_TAB.getRegistryName())
            .icon(GunTab::buildSmgTabItem)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(SMG)))
            .withSearchBar()
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_MG_TAB = TABS.register(MG.getCategoryName(), () -> CreativeModeTab.builder()
            .title(MG.getCategoryLang()).withTabsBefore(GUN_SMG_TAB.getRegistryName())
            .icon(GunTab::buildMgTabItem)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(MG)))
            .withSearchBar()
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_RPG_TAB = TABS.register(RPG.getCategoryName(), () -> CreativeModeTab.builder()
            .title(RPG.getCategoryLang()).withTabsBefore(GUN_MG_TAB.getRegistryName())
            .icon(GunTab::buildRpgTabItem)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(RPG)))
            .withSearchBar()
            .build()
    );
    public static IRegistryObject<CreativeModeTab> GUN_CUSTOM_TAB = TABS.register(CUSTOM.getCategoryName(), () -> CreativeModeTab.builder()
            .title(CUSTOM.getCategoryLang()).withTabsBefore(GUN_RPG_TAB.getRegistryName())
            .icon(GunTab::buildCustomTabItem)
            .displayItems((parameters, output) -> output.acceptAll(GunTab.buildGunItems(CUSTOM)))
            .withSearchBar()
            .build()
    );
}
