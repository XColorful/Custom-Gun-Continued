package xiao.customgun.core.api.gun;

import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.gun.ammo.IGunInventoryManager;
import xiao.customgun.core.api.gun.shoot.IGunAttackManager;
import xiao.customgun.core.api.gun.state.IGunActionManager;

public record GunManagerGroup(String managerGroupTag,
                              @NotNull IGunAttackManager gunAttackManager,
                              @NotNull IGunActionManager gunActionManager,
                              @NotNull IGunInventoryManager gunInventoryManager) {
}
