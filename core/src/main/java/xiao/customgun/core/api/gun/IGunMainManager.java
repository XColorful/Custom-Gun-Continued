/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

/*
 * 跟 BattleRoyale GameManager 的设计高度同构
 */

package xiao.customgun.core.api.gun;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.gun.ammo.IGunInventoryManager;
import xiao.customgun.core.api.gun.shoot.IGunAttackManager;
import xiao.customgun.core.api.gun.state.IGunActionManager;
import xiao.customgun.core.api.item.IGun;

public interface IGunMainManager extends IGunSubManager {

    boolean registerRuntimeGroup(GunManagerGroup gunManagerGroup);

    @NotNull GunManagerGroup getManagerGroup(String managerGroupTag);

    // --------便利方法--------

    default @NotNull GunManagerGroup getManagerGroup(IGun iGun, ItemStack gunItem) {
        return this.getManagerGroup(iGun.getManagerGroupTag(gunItem));
    }

    default IGunAttackManager getGunAttackManager(IGun iGun, ItemStack gunItem) {
        return this.getManagerGroup(iGun.getManagerGroupTag(gunItem)).gunAttackManager();
    }
    default IGunActionManager getGunActionManager(IGun iGun, ItemStack gunItem) {
        return this.getManagerGroup(iGun.getManagerGroupTag(gunItem)).gunActionManager();
    }
    default IGunInventoryManager getGunInventoryManager(IGun iGun, ItemStack gunItem) {
        return this.getManagerGroup(iGun.getManagerGroupTag(gunItem)).gunInventoryManager();
    }
}
