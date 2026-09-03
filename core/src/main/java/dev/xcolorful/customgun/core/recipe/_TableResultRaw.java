/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.recipe;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.item.AmmoProperty;
import dev.xcolorful.customgun.core.api.item.AttachmentProperty;
import dev.xcolorful.customgun.core.api.item.GunProperty;
import dev.xcolorful.customgun.core.api.item.ammo.AmmoCategory;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.builder.AmmoBuilder;
import dev.xcolorful.customgun.core.api.item.builder.AttachmentBuilder;
import dev.xcolorful.customgun.core.api.item.builder.GunBuilder;
import dev.xcolorful.customgun.core.api.item.gun.FireModeType;
import dev.xcolorful.customgun.core.api.minecraft.tab.TabGroup;
import dev.xcolorful.customgun.core.api.recipe.RecipeResultType;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.index.AmmoIndex;
import dev.xcolorful.customgun.core.resource.data.index.AttachmentIndex;
import dev.xcolorful.customgun.core.resource.data.index.GunIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 配方加载时部分物品的上下文还未完成初始化<br/>
 * 等待到实际需要使用配方时再进行初始化
 */
public class _TableResultRaw {
    private final @NotNull RecipeResultType recipeResultType;
    private final ResourceLocation pojoLocation;
    private final int resultCount;

    public _TableResultRaw(@NotNull RecipeResultType recipeResultType,
                           @NotNull ResourceLocation pojoLocation,
                           int resultCount) {
        this.recipeResultType = recipeResultType;
        this.pojoLocation = pojoLocation;
        this.resultCount = resultCount;
    }

    /**
     * TODO {@link dev.xcolorful.customgun.core.resource._AllDataManager#onTagsUpdateEvent}调用的时候疑似还没有Pojo instance?
     * 需要直接读原始Pojo
     * 调试指令为{@link dev.xcolorful.customgun.core.command.sub.DebugCommand#testAllRecipes}
     */
    public @NotNull TableResult prepareTableResultOrEmpty() {
        return switch (this.recipeResultType) {
            case GUN -> this._getGunItemOrEmpty();
            case ATTACHMENT -> this._getAttachmentItemOrEmpty();
            case AMMO -> this._getAmmoItemOrEmpty();
            case CUSTOM -> _getEmpty();
            // 增加类型使此处强制编译不通过
        };
    }
    private static @NotNull TableResult _getEmpty() {
        return new TableResult(ItemStack.EMPTY, TabGroup.GUN_CUSTOM.registryLocation);
    }
    private @NotNull TableResult _getGunItemOrEmpty() {
        // TODO ↓目前读到的是 0
//        CustomGun.LOGGER.debug("All gun index instance: {}", ResourceApi.getAllGunIndexInstance().size());
        @Nullable GunIndex gunIndex = ResourceApi.getGunIndex(this.pojoLocation);
        if (gunIndex == null) {
            CustomGun.LOGGER.debug("getGunItemOrEmpty: index {} null", this.pojoLocation); // 测试log，待删
            return _getEmpty();
        }

        @Nullable GunData gunData = ResourceApi.getGunData(gunIndex.getDataLocation());
        if (gunData == null) {
            CustomGun.LOGGER.debug("getGunItemOrEmpty: data {} null", this.pojoLocation); // 测试log，待删
            return _getEmpty();
        }

        // item
        ItemStack gunItem = GunBuilder.create(ModItems.GUN.get())
                // 枪械ResourceLocation
                .setProperty(GunProperty.GUN_LOCATION,
                        ResourceLocation.class,
                        this.pojoLocation)
                // 开火模式
                .setProperty(GunProperty.FIRE_MODE_TYPE,
                        FireModeType.class,
                        gunData.getDefaultFireModeType())
                // 枪管子弹
                .setProperty(GunProperty.BARREL_AMMO,
                        Integer.class,
                        0)
                .build();
        gunItem.setCount(this.resultCount);

        // tab group
        TabGroup tabGroup = TabGroup.fromString(gunIndex.getGunCategory().getCategoryName());
        if (tabGroup == null) tabGroup = TabGroup.GUN_CUSTOM;

        return new TableResult(gunItem, tabGroup.registryLocation);
    }
    private @NotNull TableResult _getAttachmentItemOrEmpty() {
        @Nullable AttachmentIndex attachmentIndex = ResourceApi.getAttachmentIndex(this.pojoLocation);
        if (attachmentIndex == null) {
            CustomGun.LOGGER.debug("getAttachmentItemOrEmpty: index {} null", this.pojoLocation); // 测试log，待删
            return _getEmpty();
        }

        // item
        ItemStack attachmentItem = AttachmentBuilder.create(ModItems.ATTACHMENT.get())
                // 配件ResourceLocation
                .setProperty(AttachmentProperty.ATTACHMENT_LOCATION,
                        ResourceLocation.class,
                        this.pojoLocation)
                // 配件类型
                .setProperty(AttachmentProperty.ATTACHMENT_CATEGORY,
                        AttachmentCategory.class,
                        attachmentIndex.getAttachmentCategory())
                .build();
        attachmentItem.setCount(this.resultCount);

        // tab group
        TabGroup tabGroup = TabGroup.fromString(attachmentIndex.getAttachmentCategory().getCategoryName());
        if (tabGroup == null) tabGroup = TabGroup.GUN_CUSTOM;

        return new TableResult(attachmentItem, tabGroup.registryLocation);
    }
    private @NotNull TableResult _getAmmoItemOrEmpty() {
        @Nullable AmmoIndex ammoIndex = ResourceApi.getAmmoIndex(this.pojoLocation);
        if (ammoIndex == null) {
            CustomGun.LOGGER.debug("getAmmoItemOrEmpty: index {} null", this.pojoLocation); // 测试log，待删
            return _getEmpty();
        }

        // item
        ItemStack ammoItem = AmmoBuilder.create(ModItems.AMMO.get())
                // 子弹ResourceLocation
                .setProperty(AmmoProperty.AMMO_LOCATION,
                        ResourceLocation.class,
                        this.pojoLocation)
                .setProperty(AmmoProperty.AMMO_COUNT,
                        Integer.class,
                        this.resultCount)
                .build();

        // tab group
        TabGroup tabGroup = _getAmmoTabGroup(ammoIndex.getAmmoCategory());

        return new TableResult(ammoItem, tabGroup.registryLocation);
    }
    private TabGroup _getAmmoTabGroup(AmmoCategory ammoCategory) {
        return switch (ammoCategory) {
            case AMMO -> TabGroup.AMMO;
            case EXPLOSIVE -> TabGroup.EXPLOSIVE;
            // 增加类型使此处强制编译不通过
        };
    }
}
