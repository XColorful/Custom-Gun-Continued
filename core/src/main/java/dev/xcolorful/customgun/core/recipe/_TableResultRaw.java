/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.recipe;

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
import dev.xcolorful.customgun.core.resource.instance.data.AmmoIndexInstance;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 配方加载时部分物品的上下文还未完成初始化<br/>
 * 等待到实际需要使用配方时再进行初始化
 */
public class _TableResultRaw {
    private final @Nullable RecipeResultType recipeResultType;
    private final Identifier pojoLocation;
    private final int resultCount;

    public _TableResultRaw(@NotNull String recipeResultType,
                           @NotNull Identifier pojoLocation,
                           int resultCount) {
        this.recipeResultType = RecipeResultType.fromString(recipeResultType);
        this.pojoLocation = pojoLocation;
        this.resultCount = resultCount;
    }

    public @NotNull TableResult getTableResultOrEmpty() {
        if (this.recipeResultType == null) {
            return getEmpty();
        }
        return switch (this.recipeResultType) {
            case GUN -> this.getGunItemOrEmpty();
            case ATTACHMENT -> this.getAttachmentItemOrEmpty();
            case AMMO -> this.getAmmoItemOrEmpty();
            default -> getEmpty();
        };
    }
    private static @NotNull TableResult getEmpty() {
        return new TableResult(ItemStack.EMPTY, TabGroup.GUN_CUSTOM.registryLocation);
    }
    private @NotNull TableResult getGunItemOrEmpty() {
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(this.pojoLocation);
        if (gunIndexInstance == null) return getEmpty();

        GunIndex gunIndex = gunIndexInstance.getPojo();
        GunData gunData = gunIndexInstance.getGunData();

        // item
        ItemStack gunItem = GunBuilder.create(ModItems.GUN.get())
                // 枪械ResourceLocation
                .setProperty(GunProperty.GUN_LOCATION,
                        Identifier.class,
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
    private @NotNull TableResult getAttachmentItemOrEmpty() {
        @Nullable AttachmentIndexInstance attachmentIndexInstance = ResourceApi.getAttachmentIndexInstance(this.pojoLocation);
        if (attachmentIndexInstance == null) return getEmpty();

        AttachmentIndex attachmentIndex = attachmentIndexInstance.getPojo();

        // item
        ItemStack attachmentItem = AttachmentBuilder.create(ModItems.ATTACHMENT.get())
                // 配件ResourceLocation
                .setProperty(AttachmentProperty.ATTACHMENT_LOCATION,
                        Identifier.class,
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
    private @NotNull TableResult getAmmoItemOrEmpty() {
        @Nullable AmmoIndexInstance ammoIndexInstance = ResourceApi.getAmmoIndexInstance(this.pojoLocation);
        if (ammoIndexInstance == null) return getEmpty();

        // item
        ItemStack ammoItem = AmmoBuilder.create(ModItems.AMMO.get())
                // 子弹ResourceLocation
                .setProperty(AmmoProperty.AMMO_LOCATION,
                        Identifier.class,
                        this.pojoLocation)
                .build();

        // tab group
        AmmoCategory ammoCategory; {
            AmmoIndex ammoIndex = ammoIndexInstance.getPojo();
            ammoCategory = ammoIndex.getAmmoCategory();
        }
        TabGroup tabGroup = _getAmmoTabGroup(ammoCategory);

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
