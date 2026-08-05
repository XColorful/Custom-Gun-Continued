/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.gun.inventory;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.item.AmmoProperty;
import dev.xcolorful.customgun.core.api.item.IAmmo;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.ammo.IAmmoGetter;
import dev.xcolorful.customgun.core.api.item.builder.AmmoBuilder;
import dev.xcolorful.customgun.core.api.minecraft.capability.IInventoryCapability;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.gun._ReloadData;
import dev.xcolorful.customgun.core.resource.data.index.AmmoIndex;
import dev.xcolorful.customgun.core.resource.instance.data.AmmoIndexInstance;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class _DefaultGunInventory {

    protected static void retrieveAmmoFromGun(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                              ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return;

        int magAmmoCount = iGun.getMagAmmoCount(gunItem); // 只取回弹匣的子弹
        if (magAmmoCount <= 0) return;

        GunData gunData = gunIndexInstance.getGunData();
        _ReloadData reloadData = gunData.getReloadData();

        // --------虚拟备弹--------
        if (iGun.useDummyAmmo(gunItem)) {
            iGun.setMagAmmoCount(gunItem, 0);
            // 不返还的类型
            if (!reloadData.getAmmoFeedType().canRetrieveAmmo()) {
                return;
            }
            iGun.setDummyAmmoCount(gunItem, magAmmoCount);
            return;
        }
        // --------背包直读/燃料类型不返还--------
        else if (!reloadData.getAmmoFeedType().canRetrieveAmmo()) {
            iGun.setMagAmmoCount(gunItem, 0);
            return;
        }

        var ammoLocation = gunData.getAmmoLocation();
        @Nullable AmmoIndexInstance ammoIndexInstance = ResourceApi.getAmmoIndexInstance(ammoLocation);
        if (ammoIndexInstance == null) return;

        // --------退弹--------

        // 优先退到背包 (物品)
        int dropAmmoRemain = retrieveAmmoToInventory(iGun, gunItem, livingShooter, gunData, magAmmoCount);
        // 退弹到世界 (物品实体)
        if (dropAmmoRemain > 0) dropAmmoRemain = dropAmmoToWorld(iGun, gunItem, livingShooter, gunData, dropAmmoRemain);
        iGun.setMagAmmoCount(gunItem, dropAmmoRemain);
    }
    /**
     * @return 剩余待退弹的数量
     */
    @ApiStatus.Internal
    public static int retrieveAmmoToInventory(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                              LivingEntity livingShooter,
                                              GunData gunData,
                                              int dropAmmoRemain) {
        var ammoLocation = gunData.getAmmoLocation();
        @Nullable AmmoIndexInstance ammoIndexInstance = ResourceApi.getAmmoIndexInstance(ammoLocation);
        if (ammoIndexInstance == null) return dropAmmoRemain;

        @Nullable IInventoryCapability inventoryCapability = CustomGun.getCapabilityProvider().getItemHandler(livingShooter, null);
        if (inventoryCapability == null) return dropAmmoRemain;

        AmmoIndex ammoIndex = ammoIndexInstance.getPojo();
        int maxStackSize = ammoIndex.getMaxStackSize();
        // 退弹到背包 (物品)
        for (int i = 0; i < inventoryCapability.getContainerSize(); i++) {
            final ItemStack slotItemReadOnly = inventoryCapability.getItemReadOnly(i);
            @Nullable IAmmo iAmmo = IAmmoGetter.fromItemStack(slotItemReadOnly);

            if (iAmmo != null) {
                // ammo物品 -> 堆叠
                int existAmmoCount = iAmmo.getAmmoCount(slotItemReadOnly);
                int stackSize = Math.min(dropAmmoRemain, Math.max(0, maxStackSize - existAmmoCount));
                if (stackSize == 0) continue;

                ItemStack modifiedItem = inventoryCapability.extractItem(i,
                        slotItemReadOnly.getCount(), // 取整个ItemStack
                        false);
                iAmmo = IAmmoGetter.fromItemStack(modifiedItem);
                if (iAmmo == null) {
                    CustomGun.LOGGER.warn("_DefaultGunInventory: slot {} is IAmmo before but not in extracted item in IInventoryCapability of {}", i, livingShooter.toString());
                    continue;
                }

                iAmmo.setAmmoCount(modifiedItem, existAmmoCount + stackSize);
                ItemStack remain = inventoryCapability.insertItem(i, modifiedItem, false);
                if (!remain.isEmpty()) {
                    CustomGun.LOGGER.warn("_DefaultGunInventory: can't fully insert item after extraction in slot {} in IInventoryCapability of {}", i, livingShooter.toString());
                }

                @Nullable IAmmo _iAmmo = IAmmoGetter.fromItemStack(remain);
                int remainAmmoCount = _iAmmo != null ? _iAmmo.getAmmoCount(remain) : remain.getCount();
                dropAmmoRemain -= stackSize - remainAmmoCount;
            } else if (slotItemReadOnly.isEmpty()) {
                // 空位置 -> 添加物品
                int slotSizeLimit = inventoryCapability.getMaxStackSize(i);
                int stackSize = Math.min(dropAmmoRemain, Math.min(slotSizeLimit, maxStackSize));

                ItemStack ammoItem = AmmoBuilder.create(ModItems.AMMO.get())
                        // 子弹ResourceLocation
                        .setProperty(AmmoProperty.AMMO_LOCATION,
                                ResourceLocation.class,
                                ammoLocation)
                        .setProperty(AmmoProperty.AMMO_COUNT,
                                Integer.class,
                                stackSize)
                        .build();
                ItemStack remain = inventoryCapability.insertItem(i, ammoItem, false);
                @Nullable IAmmo _iAmmo = IAmmoGetter.fromItemStack(remain);
                dropAmmoRemain -= stackSize - (_iAmmo != null ? _iAmmo.getAmmoCount(remain) : remain.getCount());
            }
        }
        return dropAmmoRemain;
    }
    /**
     * @return 剩余待退弹的数量
     */
    @ApiStatus.Internal
    public static int dropAmmoToWorld(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                      LivingEntity livingShooter,
                                      GunData gunData,
                                      int dropAmmoRemain) {
        if (!(livingShooter.level() instanceof ServerLevel serverLevel)) return dropAmmoRemain;

        var ammoLocation = gunData.getAmmoLocation();
        @Nullable AmmoIndexInstance ammoIndexInstance = ResourceApi.getAmmoIndexInstance(ammoLocation);
        if (ammoIndexInstance == null) return dropAmmoRemain;

        AmmoIndex ammoIndex = ammoIndexInstance.getPojo();
        int maxStackSize = ammoIndex.getMaxStackSize();

        Vec3 pos = livingShooter.position();
        int maxRounds = (dropAmmoRemain + maxStackSize - 1) / maxStackSize;
        for (int i = 0; i < maxRounds; i++) { // 不用while是为了防止生成失败导致卡死
            int stackSize = Math.min(dropAmmoRemain, maxStackSize);
            ItemStack ammoItem = AmmoBuilder.create(ModItems.AMMO.get())
                    // 子弹ResourceLocation
                    .setProperty(AmmoProperty.AMMO_LOCATION,
                            ResourceLocation.class,
                            ammoLocation)
                    .setProperty(AmmoProperty.AMMO_COUNT,
                            Integer.class,
                            stackSize)
                    .build();
            ItemEntity itemEntity = new ItemEntity(serverLevel, pos.x, pos.y, pos.z, ammoItem);
            itemEntity.setPickUpDelay(10);
            itemEntity.setThrower(livingShooter.getUUID());

            if (!serverLevel.addFreshEntity(itemEntity)) {
                CustomGun.LOGGER.warn("_DefaultGunInventory: Failed to add item entity {} to world, canceled dropAmmoToWorld", itemEntity.toString());
                break;
            }

            dropAmmoRemain -= stackSize;
        }

        return dropAmmoRemain;
    }

    /**
     * @param requiredAmmoCount 需要的扣除的子弹数
     * @return 已经扣除的子弹数
     */
    @ApiStatus.Internal
    public static int findAndExtractInventoryAmmo(IInventoryCapability inventoryCapability,
                                                  @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                  int requiredAmmoCount) {
        int extracted = 0;
        for (int i = 0; i < inventoryCapability.getContainerSize() && requiredAmmoCount > 0; i++) {
            final ItemStack slotItemReadOnly = inventoryCapability.getItemReadOnly(i);
            @Nullable IAmmo iAmmo = IAmmoGetter.fromItemStack(slotItemReadOnly);
            if (iAmmo == null || !iGun.isMatchedAmmo(gunItem, slotItemReadOnly)) continue;

            ItemStack modifiedItem = inventoryCapability.extractItem(i,
                    slotItemReadOnly.getCount(), // 取整个ItemStack
                    false);
            iAmmo = IAmmoGetter.fromItemStack(modifiedItem);
            if (iAmmo == null) {
                CustomGun.LOGGER.warn("_DefaultGunInventory: slot {} is IAmmo before but not in extracted item in IInventoryCapability", i);
                continue;
            }

            int existAmmoCount = iAmmo.getAmmoCount(slotItemReadOnly);
            int currentExtract;
            if (existAmmoCount <= requiredAmmoCount) {
                // 全部扣除
                currentExtract = existAmmoCount;
                iAmmo.setAmmoCount(modifiedItem, 0);

                if (!modifiedItem.isEmpty()) { // 没抽成ItemStack.EMPTY就放回去，适用于IAmmoBox
                    ItemStack remain = inventoryCapability.insertItem(i, modifiedItem, false);
                    if (!remain.isEmpty()) {
                        CustomGun.LOGGER.warn("_DefaultGunInventory: can't fully insert item after extraction in slot {} in IInventoryCapability", i);
                    }
                }
            } else {
                // 部分扣除 (需要塞回)
                currentExtract = requiredAmmoCount;

                iAmmo.setAmmoCount(modifiedItem, existAmmoCount - currentExtract);
                ItemStack remain = inventoryCapability.insertItem(i, modifiedItem, false);
                if (!remain.isEmpty()) {
                    CustomGun.LOGGER.warn("_DefaultGunInventory: can't fully insert item after extraction in slot {} in IInventoryCapability", i);
                }
            }

            extracted += currentExtract;
            requiredAmmoCount -= currentExtract;
        }
        return extracted;
    }

    /**
     * @param requiredAmmoCount 需要的扣除的子弹数
     * @return 已经扣除的子弹数
     */
    @ApiStatus.Internal
    public static int findAndExtractDummyAmmo(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                              int requiredAmmoCount) {
        int dummyAmmoCount = iGun.useDummyAmmo(gunItem) ? iGun.getDummyAmmoCount(gunItem) : 0;
        if (dummyAmmoCount <= 0) return 0;

        int extract = Math.min(dummyAmmoCount, requiredAmmoCount);
        iGun.setDummyAmmoCount(gunItem, dummyAmmoCount - extract);
        int remain = iGun.getDummyAmmoCount(gunItem);
        return dummyAmmoCount - remain;
    }
}
