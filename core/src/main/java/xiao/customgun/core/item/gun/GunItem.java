/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.gun;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;
import xiao.customgun.client.item.gun._GunItem;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.GunDataAccessor;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.minecraft.capability.IInventoryCapability;
import xiao.customgun.core.api.minecraft.item.ItemType;
import xiao.customgun.core.gui.tooltip.gun.GunTooltip;
import xiao.customgun.core.init.registry.ModItems;

import java.util.Optional;
import java.util.function.Supplier;

public class GunItem extends Item implements IGun, GunDataAccessor {

    protected GunItem(Properties properties) {
        super(properties);
    }
    public GunItem() {
        this(ModItems.CUSTOM_ITEM_PROPERTY.apply(ItemType.GUN.getRegistryLocation()));
    }

    // --------Item--------

    /**
     * 阻止玩家手臂挥动
     */
    @Override
    public boolean onEntitySwing(ItemStack gunItem, LivingEntity livingShooter) {
        return true;
    }

    /**
     * 获取在 Tooltip 中渲染的图片
     */
    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack gunItem) {
        return Optional.ofNullable(GunTooltip.fromItem(gunItem));
    }

    // --------Client--------

    @Override
    public @NotNull Component getName(@NotNull ItemStack gunItem) {
        var name = _GunItem.getName(this, gunItem);
        return name != null ? name : super.getName(gunItem);
    }

    // --------IGunRuntime--------

    // ----IGunActionRuntime----
    @Override public boolean startBolt(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunActionManager()
                .startBolt(shooterProperty, gunItem, livingShooter);
    }
    @Override public boolean tickBolt(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunActionManager()
                .tickBolt(shooterProperty, gunItem, livingShooter);
    }
    @Override public boolean canReload(ItemStack gunItem, LivingEntity livingShooter) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunActionManager()
                .canReload(gunItem, livingShooter);
    }
    @Override public boolean startReload(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunActionManager()
                .startReload(shooterProperty, gunItem, livingShooter);
    }
    @Override public ReloadState tickReload(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunActionManager()
                .tickReload(shooterProperty, gunItem, livingShooter);
    }
    @Override public void interruptReload(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter) {
        CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunActionManager()
                .interruptReload(shooterProperty, gunItem, livingShooter);
    }
    @Override public void switchFireMode(ShooterProperty shooterProperty, ItemStack gunItem) {
        CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunActionManager()
                .switchFireMode(shooterProperty, gunItem);
    }
    // ----IGunAttackRuntime----
    @Override public void shoot(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter, Supplier<Float> pitch, Supplier<Float> yaw) {
        CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunAttackManager()
                .shoot(shooterProperty, gunItem, livingShooter, pitch, yaw);
    }
    @Override public void doBulletSpread(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter,
                               Projectile projectile, int bulletId, float processedSpeed,
                               float inaccuracy, float pitch, float yaw) {
        CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunAttackManager()
                .doBulletSpread(shooterProperty, gunItem, livingShooter,
                        projectile, bulletId, processedSpeed,
                        inaccuracy, pitch, yaw);
    }
    @Override public void melee(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter) {
        CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunAttackManager()
                .melee(shooterProperty, gunItem, livingShooter);
    }
    // ----IGunInventoryRuntime----
    @Override public void dropAllAmmo(ItemStack gunItem, LivingEntity livingShooter) {
        CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunInventoryManager()
                .dropAllAmmo(gunItem, livingShooter);
    }
    @Override public int findAndExtractInventoryAmmo(IInventoryCapability inventoryCapability, ItemStack gunItem, int needAmmoCount) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunInventoryManager()
                .findAndExtractInventoryAmmo(inventoryCapability, gunItem, needAmmoCount);
    }
    @Override public int findAndExtractDummyAmmo(ItemStack gunItem, int needAmmoCount) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunInventoryManager()
                .findAndExtractDummyAmmo(gunItem, needAmmoCount);
    }
    // ----IGunScriptRuntime----
    // ----IGunStateRuntime----
    @Override public void tickHeat(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter) {
        CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunStateManager()
                .tickHeat(shooterProperty, gunItem, livingShooter);
    }

    // --------IAnimationItem--------

    @Override
    public boolean switchItemNeedReset(ItemStack oldItem, ItemStack newItem) {
        if (oldItem.isEmpty() || newItem.isEmpty()) {
            return oldItem.isEmpty() && newItem.isEmpty();
        }
        IGun iGun1 = IGunGetter.fromItemStack(oldItem);
        IGun iGun2 = IGunGetter.fromItemStack(newItem);
        if (iGun1 != null && iGun2 != null) {
            return iGun1.getGunLocation(oldItem).equals(iGun2.getGunLocation(newItem))
                    && iGun1.getGunDisplayLocation(oldItem).equals(iGun2.getGunDisplayLocation(newItem));
        }
        return ItemStack.matches(oldItem, newItem);
    }
}
