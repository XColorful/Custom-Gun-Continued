/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.item.gun;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.item.gun._GunItem;
import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ReloadState;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.gun.attack.IGunAttackRuntime;
import dev.xcolorful.customgun.core.api.gun.script.context.GunScriptApi;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.GunDataAccessor;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.item.gun.MeleeType;
import dev.xcolorful.customgun.core.api.item.gun.modifier.GunModifierType;
import dev.xcolorful.customgun.core.api.minecraft.capability.IInventoryCapability;
import dev.xcolorful.customgun.core.api.minecraft.item.ItemType;
import dev.xcolorful.customgun.core.gui.tooltip.gun.GunTooltip;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * 获取供客户端使用的 Tooltip 信息
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
    @Override public boolean startBolt(ShooterProperty shooterProperty, @NotNull IGun iGun, @NotNull ItemStack gunItem, ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunActionManager()
                .startBolt(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter);
    }
    @Override public boolean tickBolt(ShooterProperty shooterProperty, @NotNull IGun iGun, @NotNull ItemStack gunItem, ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunActionManager()
                .tickBolt(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter);
    }
    @Override public boolean canReload(@NotNull IGun iGun, @NotNull ItemStack gunItem, ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunActionManager()
                .canReload(iGun, gunItem, iLivingShooter, livingShooter);
    }
    @Override public boolean startReload(ShooterProperty shooterProperty, @NotNull IGun iGun, @NotNull ItemStack gunItem, ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunActionManager()
                .startReload(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter);
    }
    @Override public ReloadState tickReload(ShooterProperty shooterProperty, @NotNull IGun iGun, @NotNull ItemStack gunItem, ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunActionManager()
                .tickReload(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter);
    }
    @Override public void interruptReload(ShooterProperty shooterProperty, @NotNull IGun iGun, @NotNull ItemStack gunItem, ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunActionManager()
                .interruptReload(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter);
    }
    @Override public boolean switchFireMode(ShooterProperty shooterProperty, @NotNull IGun iGun, @NotNull ItemStack gunItem, ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunActionManager()
                .switchFireMode(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter);
    }
    // ----IGunAttackRuntime----
    @Override public @NotNull IGunAttackRuntime.ShooterFireResult shooterFire(ShooterProperty shooterProperty, @NotNull IGun iGun, @NotNull ItemStack gunItem, ILivingShooter iLivingShooter, LivingEntity livingShooter, Supplier<Float> pitch, Supplier<Float> yaw, float clientChargeProgress) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunAttackManager()
                .shooterFire(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter, pitch, yaw, clientChargeProgress);
    }
    @Override public @NotNull IGunAttackRuntime.GunFireResult gunFire(ShooterProperty shooterProperty, @NotNull IGun iGun, @NotNull ItemStack gunItem, ILivingShooter iLivingShooter, LivingEntity livingShooter, Supplier<Float> pitch, Supplier<Float> yaw) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunAttackManager()
                .gunFire(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter, pitch, yaw);
    }
    @Override public void doBulletSpread(ShooterProperty shooterProperty, @NotNull IGun iGun, @NotNull ItemStack gunItem, ILivingShooter iLivingShooter, LivingEntity livingShooter, @NotNull IGunProjectile iGunProjectile, @NotNull Projectile projectile, int bulletId, float xRot, float yRot, float pow, float uncertainty) {
        CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunAttackManager()
                .doBulletSpread(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter, iGunProjectile, projectile, bulletId, xRot, yRot, pow, uncertainty);
    }
    @Override public @Nullable MeleePreparation prepareMelee(@NotNull IGun iGun, @NotNull ItemStack gunItem, ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunAttackManager()
                .prepareMelee(iGun, gunItem, iLivingShooter, livingShooter);
    }
    @Override public void melee(ShooterProperty shooterProperty, @NotNull IGun iGun, @NotNull ItemStack gunItem, ILivingShooter iLivingShooter, LivingEntity livingShooter, MeleeType meleeType) {
        CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunAttackManager()
                .melee(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter, meleeType);
    }
    // ----IGunInventoryRuntime----
    @Override public void retrieveAmmoFromGun(@NotNull IGun iGun, @NotNull ItemStack gunItem, @Nullable ILivingShooter iLivingShooter, @Nullable LivingEntity livingShooter) {
        CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunInventoryManager()
                .retrieveAmmoFromGun(iGun, gunItem, iLivingShooter, livingShooter);
    }
    @Override public int findAndExtractInventoryAmmo(@NotNull IInventoryCapability inventoryCapability, @NotNull IGun iGun, @NotNull ItemStack gunItem, int requiredAmmoCount) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunInventoryManager()
                .findAndExtractInventoryAmmo(inventoryCapability, iGun, gunItem, requiredAmmoCount);
    }
    @Override public int findAndExtractDummyAmmo(@NotNull IGun iGun, @NotNull ItemStack gunItem, int requiredAmmoCount) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunInventoryManager()
                .findAndExtractDummyAmmo(iGun, gunItem, requiredAmmoCount);
    }
    // ----IGunScriptRuntime----
    @Override public @NotNull <V> V evalByScript(ItemStack gunItem, GunScriptApi scriptApi, GunModifierType modifierType, @NotNull V value) {
        return CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunScriptManager()
                .evalByScript(gunItem, scriptApi, modifierType, value);
    }
    // ----IGunStateRuntime----
    @Override public void tickHeat(ShooterProperty shooterProperty, @NotNull IGun iGun, @NotNull ItemStack gunItem, ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        CustomGun.getGunManager().getManagerGroup(this.getManagerGroupTag(gunItem))
                .gunStateManager()
                .tickHeat(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter);
    }

    // --------IAnimationItem--------

    @Override
    public boolean switchItemNeedReset(ItemStack oldItem, ItemStack newItem) {
        if (oldItem.isEmpty() || newItem.isEmpty()) {
            return oldItem.isEmpty() != newItem.isEmpty();
        }
        IGun iGun1 = IGunGetter.fromItemStack(oldItem);
        IGun iGun2 = IGunGetter.fromItemStack(newItem);
        if (iGun1 != null && iGun2 != null) {
            return !iGun1.getGunLocation(oldItem).equals(iGun2.getGunLocation(newItem))
                    || !iGun1.getGunDisplayLocation(oldItem).equals(iGun2.getGunDisplayLocation(newItem));
        }
        return !ItemStack.matches(oldItem, newItem);
    }
}
