package xiao.customgun.core.api.gun;

import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.gun.inventory.IGunInventoryManager;
import xiao.customgun.core.api.gun.attack.IGunAttackManager;
import xiao.customgun.core.api.gun.action.IGunActionManager;
import xiao.customgun.core.api.gun.state.IGunStateManager;
import xiao.customgun.core.api.gun.state.IGunStateRuntime;

public record GunManagerGroup(String managerGroupTag,
                              @NotNull IGunActionManager gunActionManager,
                              @NotNull IGunAttackManager gunAttackManager,
                              @NotNull IGunInventoryManager gunInventoryManager,
                              @NotNull IGunStateManager gunStateManager) {
}
