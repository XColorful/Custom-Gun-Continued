/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import xiao.customgun.core.api.item.*;
import xiao.customgun.core.api.item.ammo.IAmmoGetter;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.item.attachment.AttachmentNBTAccessor;
import xiao.customgun.core.api.item.attachment.IAttachmentGetter;
import xiao.customgun.core.api.item.attachment.MagazineCategory;
import xiao.customgun.core.api.item.builder.AttachmentBuilder;
import xiao.customgun.core.api.minecraft.capability.IInventoryCapability;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.developer.PlannedRefactor;
import xiao.customgun.core.init.registry.ModItems;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.data.attachment._MeleeModifierData;
import xiao.customgun.core.resource.data.data.gun._ChargingData;
import xiao.customgun.core.resource.data.data.gun._MeleeData;
import xiao.customgun.core.resource.data.data.gun._ReloadData;
import xiao.customgun.core.resource.data.data.gun.melee._DefaultMeleeData;
import xiao.customgun.core.resource.instance.data.AttachmentIndexInstance;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;
import xiao.customgun.core.util.NBTUtils;

import java.util.Map;

public interface GunDataAccessor extends IGunDataAccess {

    // --------IGunDataAccess--------

    @Override
    default @Nullable String getManagerGroupTag(ItemStack gunItem) {
        return NBTUtils.getString(gunItem, GunProperty.MANAGER_GROUP.getTagName());
    }
    @Override
    default void setManagerGroupTag(ItemStack gunItem, String managerGroupTag) {
        NBTUtils.setString(gunItem, GunProperty.MANAGER_GROUP.getTagName(), managerGroupTag);
    }

    @Override
    default @NotNull ResourceLocation getGunLocation(ItemStack gunItem) {
        var gunLocation = NBTUtils.getResourceLocation(gunItem, GunProperty.GUN_LOCATION.getTagName());
        return gunLocation != null ? gunLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setGunLocation(ItemStack gunItem, ResourceLocation gunLocation) {
        NBTUtils.setResourceLocation(gunItem, GunProperty.GUN_LOCATION.getTagName(), gunLocation);
    }
    @Override
    default @NotNull ResourceLocation getGunDisplayLocation(ItemStack gunItem) {
        var gunDisplayLocation = NBTUtils.getResourceLocation(gunItem, GunProperty.GUN_DISPLAY_LOCATION.getTagName());
        if (gunDisplayLocation != null) return gunDisplayLocation;

        var gunLocation = this.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return ResourceTag.NULL_LOCATION;

        return gunIndexInstance.getPojo().getDisplayIndexLocation();
    }
    @Override
    default void setGunDisplayLocation(ItemStack gunItem, ResourceLocation gunDisplayLocation) {
        NBTUtils.setResourceLocation(gunItem, GunProperty.GUN_DISPLAY_LOCATION.getTagName(), gunDisplayLocation);
    }

    // --------IGunStateAccess--------

    @Override
    default @NotNull FireModeType getFireModeType(ItemStack gunItem) {
        if (gunItem.isEmpty()) return FireModeType.DEFAULT;
        FireModeType fireModeType = FireModeType.fromString(NBTUtils.getString(gunItem, GunProperty.FIRE_MODE_TYPE.getTagName()));
        return fireModeType != null ? fireModeType : FireModeType.DEFAULT;
    }
    @Override
    default void setFireModeType(ItemStack gunItem, FireModeType fireModeType) {
        if (gunItem.isEmpty()) return;
        NBTUtils.setString(gunItem, GunProperty.FIRE_MODE_TYPE.getTagName(), fireModeType.getTagName());
    }

    @Override
    default ChargeType getChargeType(ItemStack gunItem, FireModeType fireModeType) {
        @Nullable Map<FireModeType, _ChargingData> chargingData = _getChargingData(this, gunItem);
        if (chargingData == null) return null;

        @Nullable _ChargingData _chargingData = chargingData.get(fireModeType);
        if (_chargingData == null) return null;

        return _chargingData.getChargeType();
    }
    @ApiStatus.Internal
    static @Nullable Map<FireModeType, _ChargingData> _getChargingData(IGunDataAccess iGun, ItemStack gunItem) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return null;

        GunData gunData = gunIndexInstance.getGunData();
        return gunData.getChargingData();
    }

    @Override
    default float getScopeZoomScale(ItemStack gunItem) {
        if (!CustomGun.getMcSide().isClientSide()) {
            if (PlannedRefactor.MOVE_ASSETS_TO_DATA) {
                throw new IllegalStateException("GunDataAccessor#getScopeZoomScale is client-side interface (currently)");
            }
        }

        @NotNull var scopeLocation = this.getAttachmentLocation(gunItem, AttachmentCategory.SCOPE);
        boolean builtIn = false;
        if (scopeLocation.equals(ResourceTag.NULL_LOCATION)) {
            scopeLocation = this.getBuiltinAttachmentLocation(gunItem, AttachmentCategory.SCOPE);
            builtIn = true;
        }

        float zoomScale = 1.0f;
        if (!scopeLocation.equals(ResourceTag.NULL_LOCATION)) {
            @Nullable CompoundTag attachmentCustomDataTag = this.getAttachmentCustomDataTag(gunItem, AttachmentCategory.SCOPE);
            int scopeViewIndex = builtIn ? 0 : AttachmentNBTAccessor.INSTANCE.getScopeViewIndex(attachmentCustomDataTag);
            if (PlannedRefactor.MOVE_SCOPE_VIEW_INDEX_TO_CORE) {
                return 0;
            }
            @Nullable ClientAttachmentIndexInstance attachmentIndexInstance = ClientResourceApi.getClientAttachmentIndexInstance(scopeLocation);
            if (attachmentIndexInstance != null) {
                float[] scopeZoomScale = attachmentIndexInstance.getAttachmentDisplay().getScopeZoomScale();
                if (scopeZoomScale != null) {
                    zoomScale = scopeZoomScale[scopeViewIndex % scopeZoomScale.length];
                }
            }
        } else {
            GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
            if (gunDisplayInstance != null) zoomScale = gunDisplayInstance.getPojo().getIronZoomScale();
        }
        return zoomScale;
    }

    @Override
    default boolean hasHeat(ItemStack gunItem) {
        return NBTUtils.hasKey(gunItem, GunProperty.HEAT.getTagName());
    }
    @Override
    default float getHeatCount(ItemStack gunItem) {
        return NBTUtils.getFloat(gunItem, GunProperty.HEAT.getTagName());
    }
    @Override
    default void setHeatCount(ItemStack gunItem, float amount) {
        NBTUtils.setFloat(gunItem, GunProperty.HEAT.getTagName(), amount);
    }
    @Override
    default boolean hasOverheatLock(ItemStack gunItem) {
        return NBTUtils.getBoolean(gunItem, GunProperty.OVERHEAT_LOCK.getTagName());
    }
    @Override
    default void setOverheatLock(ItemStack gunItem, boolean locked) {
        NBTUtils.setBoolean(gunItem, GunProperty.OVERHEAT_LOCK.getTagName(), locked);
    }

    @Override
    default boolean hasAttachmentLock(ItemStack gunItem) {
        return NBTUtils.getBoolean(gunItem, GunProperty.ATTACHMENT_LOCK.getTagName());
    }
    @Override
    default void setAttachmentLock(ItemStack gunItem, boolean value) {
        NBTUtils.setBoolean(gunItem, GunProperty.ATTACHMENT_LOCK.getTagName(), value);
    }

    @Override
    default boolean hasLaserColor(ItemStack gunItem) {
        return NBTUtils.hasKey(gunItem, GunProperty.LASER_COLOR.getTagName());
    }
    @Override
    default int getLaserColorInt(ItemStack gunItem) {
        return NBTUtils.getInt(gunItem, GunProperty.LASER_COLOR.getTagName());
    }
    @Override
    default void setLaserColorInt(ItemStack gunItem, int colorInt) {
        NBTUtils.setInt(gunItem, GunProperty.LASER_COLOR.getTagName(), colorInt);
    }

    @Override
    default boolean hasTooltipMask(ItemStack gunItem) {
        return NBTUtils.hasKey(gunItem, GunProperty.TOOLTIP_MASK.getTagName());
    }
    @Override
    default int getTooltipMask(ItemStack gunItem) {
        return NBTUtils.getInt(gunItem, GunProperty.TOOLTIP_MASK.getTagName());
    }
    @Override
    default void setTooltipMask(ItemStack gunItem, int tooltipMask) {
        NBTUtils.setInt(gunItem, GunProperty.TOOLTIP_MASK.getTagName(), tooltipMask);
    }

    @Override
    default @Nullable MeleeType getGunMeleeType(ItemStack gunItem) {
        if (_getAttachmentMeleeModifierData(this, gunItem, AttachmentCategory.MUZZLE) != null)
            return MeleeType.BAYONET;
        else if (_getAttachmentMeleeModifierData(this, gunItem, AttachmentCategory.STOCK) != null)
            return MeleeType.STOCK;
        else if (_getGunDefaultMeleeData(this, gunItem) != null)
            return MeleeType.PUSH;
        return null;
    }
    @ApiStatus.Internal
    static @Nullable _MeleeModifierData _getAttachmentMeleeModifierData(IGunDataAccess iGun, ItemStack gunItem, AttachmentCategory attachmentCategory) {
        var meleeLocation = iGun.getAttachmentLocation(gunItem, attachmentCategory);
        @Nullable AttachmentIndexInstance attachmentIndexInstance = ResourceApi.getAttachmentIndexInstance(meleeLocation);
        if (attachmentIndexInstance == null) return null;

        AttachmentData attachmentData = attachmentIndexInstance.getAttachmentData();
        @Nullable _MeleeModifierData meleeModifierData = attachmentData.getMeleeModifier();
        return meleeModifierData;
    }
    @ApiStatus.Internal
    static @Nullable _DefaultMeleeData _getGunDefaultMeleeData(IGunDataAccess iGun, ItemStack gunItem) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return null;

        GunData gunData = gunIndexInstance.getGunData();
        _MeleeData meleeData = gunData.getMeleeData();
        return meleeData.getDefaultMeleeData();
    }

    // --------IGunAmmoDataAccess--------

    @Override
    default boolean isMatchedAmmo(ItemStack gunItem, ItemStack ammoItem) {
        return this.consumableAmmoCount(gunItem, ammoItem) > 0;
    }
    @Override
    default int consumableAmmoCount(ItemStack gunItem, ItemStack ammoItem) {
        IAmmo iAmmo = IAmmoGetter.fromItemStack(ammoItem);
        if (iAmmo == null) return 0;

        GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(this.getGunLocation(gunItem));
        if (gunIndexInstance == null) return 0;

        @Nullable var customData = NBTUtils.getCustomData(ammoItem);
        if (customData == null) return 0;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        if (!iAmmo.getAmmoLocation(customDataTag).equals(gunIndexInstance.getGunData().getAmmoLocation())
                && !iAmmo.isAlmightyAmmo(customDataTag)) {
            return 0;
        }
        return iAmmo.getAmmoCount(ammoItem);
    }

    @Override
    default int consumeAmmoOnce(LivingEntity livingEntity, ItemStack gunItem) {
        @Nullable BoltType boltType = _getBoltType(this, gunItem);
        if (boltType == null) return 0;
        return this.consumeAmmoOnce(livingEntity, gunItem, boltType);
    }
    @ApiStatus.Internal
    static @Nullable BoltType _getBoltType(IGunDataAccess iGun, ItemStack gunItem) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return null;

        GunData gunData = gunIndexInstance.getGunData();
        return gunData.getBoltType();
    }

    @Override
    default boolean useDummyAmmo(ItemStack gunItem) {
        return NBTUtils.hasKey(gunItem, GunProperty.DUMMY_AMMO.getTagName());
    }
    @Override
    default int getDummyAmmoCount(ItemStack gunItem) {
        return Math.max(0, NBTUtils.getInt(gunItem, GunProperty.DUMMY_AMMO.getTagName()));
    }
    @Override
    default void addDummyAmmoCount(ItemStack gunItem, int amount) {
        setDummyAmmoCount(gunItem, this.getDummyAmmoCount(gunItem) + amount);
    }
    @Override
    default void setDummyAmmoCount(ItemStack gunItem, int amount) {
        NBTUtils.setInt(gunItem, GunProperty.DUMMY_AMMO.getTagName(), Math.min(amount, this.getDummyAmmoLimit(gunItem)));
    }
    @Override
    default boolean hasDummyAmmoLimit(ItemStack gunItem) {
        return NBTUtils.hasKey(gunItem, GunProperty.DUMMY_AMMO_LIMIT.getTagName());
    }
    @Override
    default int getDummyAmmoLimit(ItemStack gunItem) {
        return Math.max(0, Math.min(NBTUtils.getInt(gunItem, GunProperty.DUMMY_AMMO_LIMIT.getTagName()), Integer.MAX_VALUE));
    }
    @Override
    default void setDummyAmmoLimit(ItemStack gunItem, int max) {
        NBTUtils.setInt(gunItem, GunProperty.DUMMY_AMMO_LIMIT.getTagName(), max);
    }

    @Override
    default boolean useInventoryAmmo(ItemStack gunItem) {
        GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(this.getGunLocation(gunItem));
        if (gunIndexInstance == null) return false;

        GunData gunData = gunIndexInstance.getGunData();
        _ReloadData reloadData = gunData.getReloadData();
        return reloadData.getAmmoFeedType() == AmmoFeedType.INVENTORY;
    }
    @Override
    default boolean hasInventoryAmmo(LivingEntity livingEntity, ItemStack gunItem) {
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return false;

        @Nullable IInventoryCapability inventoryCapability = CustomGun.getCapabilityProvider().getItemHandler(livingEntity, null);
        if (inventoryCapability == null) return false;

        for (int i = 0; i < inventoryCapability.getContainerSize(); i++) {
            ItemStack ammoItem = inventoryCapability.getItemReadOnly(i);

            if (iGun.isMatchedAmmo(gunItem, ammoItem)) {
                return true;
            }
        }
        return false;
    }
    @Override
    default int getInventoryAmmoCount(LivingEntity livingEntity, ItemStack gunItem) {
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return 0;

        @Nullable IInventoryCapability inventoryCapability = CustomGun.getCapabilityProvider().getItemHandler(livingEntity, null);
        if (inventoryCapability == null) return 0;

        int count = 0;
        for (int i = 0; i < inventoryCapability.getContainerSize(); i++) {
            ItemStack ammoItem = inventoryCapability.getItemReadOnly(i);

            count += iGun.consumableAmmoCount(gunItem, ammoItem);
        }
        return count;
    }

    @Override
    default int getMagAmmoCount(ItemStack gunItem) {
        return Math.max(0, NBTUtils.getInt(gunItem, GunProperty.MAG_AMMO.getTagName()));
    }
    @Override
    default int getMagAmmoCountWithBarrel(ItemStack gunItem, BoltType boltType) {
        if (boltType == BoltType.OPEN_BOLT) {
            return this.getMagAmmoCount(gunItem);
        } else {
            return this.getMagAmmoCount(gunItem) + this.getBarrelAmmoCount(gunItem);
        }
    }
    @Override
    default void setMagAmmoCount(ItemStack gunItem, int count) {
        NBTUtils.setInt(gunItem, GunProperty.MAG_AMMO.getTagName(), count);
    }
    @Override
    default int consumeMagAmmoOnce(ItemStack gunItem) {
        int current = this.getMagAmmoCount(gunItem);
        if (current <= 0) return 0;
        NBTUtils.setInt(gunItem, GunProperty.MAG_AMMO.getTagName(), current - 1);
        return 1;
    }

    @Override
    default int getMagAmmoLimit(ItemStack gunItem) {
        var gunLocation = this.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        MagazineCategory magazineCategory = MagazineCategory.NONE;
        {
            var attachmentLocation = this.getAttachmentLocation(gunItem, AttachmentCategory.MAGAZINE);
            AttachmentData attachmentData = ResourceApi.getAttachmentData(attachmentLocation);
            if (attachmentData != null) magazineCategory = attachmentData.getMagazineCategory();
        }
        if (magazineCategory == null || magazineCategory == MagazineCategory.NONE)
            return gunData.getDefaultMagSize();
        int[] extendedMagAmmoSize = gunData.getExtendedMagAmmoSize();
        int index = magazineCategory.getIndex() - 1;
        if (index < 0 || index >= extendedMagAmmoSize.length) return 0;
        else return extendedMagAmmoSize[index];
    }

    @Override
    default int getBarrelAmmoCount(ItemStack gunItem) {
        return Math.max(0, NBTUtils.getInt(gunItem, GunProperty.BARREL_AMMO.getTagName()));
    }
    @Override
    default void setBarrelAmmoCount(ItemStack gunItem, int amount) {
        NBTUtils.setInt(gunItem, GunProperty.BARREL_AMMO.getTagName(), amount);
    }

    @Override
    default int boltBarrelAmmo(LivingEntity livingEntity, ItemStack gunItem) {
        @Nullable BoltType boltType = _getBoltType(this, gunItem);
        if (boltType == null) return 0;
        return boltBarrelAmmo(livingEntity, gunItem, boltType);
    }

    // --------IGunAttachmentDataAccess--------

//    1.20.1
//    枪械物品序列化字符串 (第一个字符从花括号开始)
//    {
//        "id": "customgun:gun",
//        "count": 1,
//        "tag": {
//            "attachment_scope": {
//                "attachment_rl": ""
//            }
//        }
//    }
//    配件物品序列化字符串
//    {
//        "id": "customgun:attachment",
//        "count": 1,
//        "tag": {
//            "attachment_rl": ""
//        }
//    }
//    1.21.1+
//    枪械物品序列化字符串
//    {
//        "id": "customgun:gun",
//        "components": {
//            "custom_data": {
//                "attachment_scope":{
//                    "attachment_rl": ""
//                }
//            }
//        }
//    }
//    配件物品序列化字符串
//    {
//        "id": "customgun:attachment",
//        "components": {
//            "custom_data": {
//                "attachment_rl": ""
//            }
//        }
//    }
//    1.21.1+将在卸载时手动设置NBT(NBTUtils封装的就已经是将Tag放进custom_data)
//    1.20.1里获取的tag直接视为custom_data，而不是将attachment_scope视为序列化字符串的Tag
//    即忽略"Count"/"count"和"id"，直接创建一个count:1,id:"customgun:attachment"的物品并借NBTUtils写到custom_data
    @Override
    default boolean isAttachmentEnabled(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        if (attachmentCategory == AttachmentCategory.NONE) return false;
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return false;

        GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(gunItem));
        if (gunIndexInstance == null) return false;

        return gunIndexInstance.getGunData().getAllowAttachmentTypes().contains(attachmentCategory);
    }
    @Override
    default boolean canInstallAttachment(ItemStack gunItem, ItemStack attachmentItem) {
        IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
        if (iAttachment == null) return false;

        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return false;

        var gunLocation = iGun.getGunLocation(gunItem);
        var attachmentLocation = iAttachment.getAttachmentLocation(attachmentItem);
        AttachmentCategory category = iAttachment.getAttachmentCategory(attachmentItem);
        if (!this.isAttachmentEnabled(gunItem, category)) {
            return false;
        }
        return ResourceApi.hasAttachmentInstallability(attachmentLocation, gunLocation);
    }

    @Override
    default @NotNull ItemStack getAttachment(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        @Nullable CompoundTag attachmentCustomDataTag = this.getAttachmentCustomDataTag(gunItem, attachmentCategory);

        if (attachmentCustomDataTag == null) {
            return ItemStack.EMPTY;
        }

        return AttachmentBuilder.create(ModItems.ATTACHMENT.get())
                // 先写已有的NBT
                .setCustomDataTag(attachmentCustomDataTag)
                // 配件类型在gun nbt的key
                .setProperty(AttachmentProperty.ATTACHMENT_CATEGORY,
                        AttachmentCategory.class,
                        attachmentCategory)
                .build();
    }
    @Override
    default @NotNull ItemStack getBuiltinAttachment(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return ItemStack.EMPTY;

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(gunItem));
        if (gunIndexInstance == null) {
            return ItemStack.EMPTY;
        }

        var builtinAttachments = gunIndexInstance.getGunData().getBuiltinAttachments();
        if (builtinAttachments.containsKey(attachmentCategory)) {
            return AttachmentBuilder.create(ModItems.ATTACHMENT.get())
                    // 配件ResourceLocation
                    .setProperty(AttachmentProperty.ATTACHMENT_LOCATION,
                            ResourceLocation.class,
                            builtinAttachments.get(attachmentCategory))
                    // 配件类型
                    .setProperty(AttachmentProperty.ATTACHMENT_CATEGORY,
                            AttachmentCategory.class,
                            attachmentCategory)
                    .build();
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    default @Nullable CompoundTag getAttachmentCustomDataTag(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        // 快的情况先排除
        @Nullable var customData = NBTUtils.getCustomData(gunItem);
        if (customData == null) return null;

        // 稍慢的检查
        if (!isAttachmentEnabled(gunItem, attachmentCategory)) {
            return null;
        }

        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData); // 涉及tag复制 (1.21.1+)
        return NBTUtils.getCompoundTag(customDataTag,
                attachmentCategory.getTagName()); // 存在枪械根目录的key用带前缀的版本
//                attachmentCategory.getCategoryName());
    }
    @Override
    default void setAttachmentCustomDataTag(ItemStack gunItem, AttachmentCategory attachmentCategory, CompoundTag attachmentCustomDataTag) {
        @Nullable var customData = NBTUtils.getCustomData(gunItem);
        if (customData == null && attachmentCustomDataTag == null) return;

        @NotNull CompoundTag customDataTag = customData != null ? NBTUtils.getCustomDataTag(customData) : new CompoundTag();

        // 将attachment Tag写入tag
        NBTUtils.setCompoundTag(customDataTag, attachmentCategory.getTagName(), attachmentCustomDataTag);

        // 将tag存入item
        NBTUtils.setCustomDataTag(gunItem, customDataTag);
    }

    @Override
    default @NotNull ResourceLocation getAttachmentLocation(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        return AttachmentNBTAccessor.INSTANCE.getAttachmentLocation(this.getAttachmentCustomDataTag(gunItem, attachmentCategory));
    }

    @Override
    default @NotNull ResourceLocation getBuiltinAttachmentLocation(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(this.getGunLocation(gunItem));
        if (gunIndexInstance == null) return ResourceTag.NULL_LOCATION;
        var location = gunIndexInstance.getGunData().getBuiltinAttachments().get(attachmentCategory);
        return location != null ? location : ResourceTag.NULL_LOCATION;
    }

    @Override
    default boolean installAttachment(ItemStack gunItem, ItemStack attachmentItem) {
        IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
        if (iAttachment == null) return false;

        if (!this.canInstallAttachment(gunItem, attachmentItem)) {
            return false;
        }

        // 没有CustomData就没数据，为无效配件
        @Nullable var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) {
            return false;
        }
        @NotNull CompoundTag attachmentCustomDataTag = NBTUtils.getCustomDataTag(customData);

        AttachmentCategory category = iAttachment.getAttachmentCategory(attachmentItem);
        NBTUtils.setCompoundTag(gunItem,
                category.getTagName(), // 存在枪械根目录的key用带前缀的版本
//                category.getCategoryName(),
                attachmentCustomDataTag);
        return true;
    }
    @Override
    default void removeAttachment(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        NBTUtils.removeKey(gunItem,
                attachmentCategory.getTagName()); // 存在枪械根目录的key用带前缀的版本
//                attachmentCategory.getCategoryName());
    }

    // --------IGunExpAccess--------

    @Override
    default int getGunExp(ItemStack gunItem) {
        return Math.max(0, NBTUtils.getInt(gunItem, GunProperty.GUN_EXP.getTagName()));
    }
    @Override
    default void setGunExp(ItemStack gunItem, int exp) {
        NBTUtils.setInt(gunItem, GunProperty.GUN_EXP.getTagName(), exp);
    }
    @Override
    default int calculateLevel(ItemStack gunItem, int exp) {
        return 0; // TODO 武器经验系统
    }
    @Override
    default int calculateExp(ItemStack gunItem, int level) {
        return 0;
    }
    @Override
    default int getCurrentLevelExp(ItemStack gunItem) {
        int exp = this.getGunExp(gunItem);
        int level = this.calculateLevel(gunItem, exp);
        if (level <= 0) return exp;
        else return exp - this.calculateExp(gunItem, level - 1);
    }
    @Override
    default int getExpToNextLevel(ItemStack gunItem) {
        int exp = this.getGunExp(gunItem);
        int level = this.calculateLevel(gunItem, exp);
        if (level >= this.getMaxLevel(gunItem)) return 0;
        return this.calculateExp(gunItem, level + 1) - exp;
    }
    @Override
    default int getMaxLevel(ItemStack gunItem) {
        return 0;
    }
}
