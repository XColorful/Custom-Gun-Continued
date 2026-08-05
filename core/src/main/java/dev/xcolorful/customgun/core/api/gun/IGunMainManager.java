/*
 * Go to BattleRoyale GameManager 的设计
 */

package dev.xcolorful.customgun.core.api.gun;

import dev.xcolorful.customgun.core.api.gun.action.IGunActionManager;
import dev.xcolorful.customgun.core.api.gun.attack.IGunAttackManager;
import dev.xcolorful.customgun.core.api.gun.inventory.IGunInventoryManager;
import dev.xcolorful.customgun.core.api.gun.script.IGunScriptManager;
import dev.xcolorful.customgun.core.api.gun.state.IGunStateManager;
import dev.xcolorful.customgun.core.api.item.IGun;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IGunMainManager extends IGunSubManager {

    boolean registerRuntimeGroup(GunManagerGroup gunManagerGroup);

    @NotNull GunManagerGroup getManagerGroup(String managerGroupTag);

    // --------便利方法--------

    default @NotNull GunManagerGroup getManagerGroup(IGun iGun, ItemStack gunItem) {
        return this.getManagerGroup(iGun.getManagerGroupTag(gunItem));
    }

    default IGunActionManager getGunActionManager(IGun iGun, ItemStack gunItem) {
        return this.getManagerGroup(iGun.getManagerGroupTag(gunItem)).gunActionManager();
    }
    default IGunAttackManager getGunAttackManager(IGun iGun, ItemStack gunItem) {
        return this.getManagerGroup(iGun.getManagerGroupTag(gunItem)).gunAttackManager();
    }
    default IGunInventoryManager getGunInventoryManager(IGun iGun, ItemStack gunItem) {
        return this.getManagerGroup(iGun.getManagerGroupTag(gunItem)).gunInventoryManager();
    }
    default IGunScriptManager getGunScriptManager(IGun iGun, ItemStack gunItem) {
        return this.getManagerGroup(iGun.getManagerGroupTag(gunItem)).gunScriptManager();
    }
    default IGunStateManager getGunStateManager(IGun iGun, ItemStack gunItem) {
        return this.getManagerGroup(iGun.getManagerGroupTag(gunItem)).gunStateManager();
    }
}
