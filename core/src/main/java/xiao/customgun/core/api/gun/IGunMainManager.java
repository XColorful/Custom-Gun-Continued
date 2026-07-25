/*
 * Go to BattleRoyale GameManager 的设计
 */

package xiao.customgun.core.api.gun;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.gun.inventory.IGunInventoryManager;
import xiao.customgun.core.api.gun.attack.IGunAttackManager;
import xiao.customgun.core.api.gun.action.IGunActionManager;
import xiao.customgun.core.api.gun.script.IGunScriptManager;
import xiao.customgun.core.api.gun.state.IGunStateManager;
import xiao.customgun.core.api.item.IGun;

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
