package dev.xcolorful.customgun.core.api.gun;

import dev.xcolorful.customgun.core.api.gun.action.IGunActionManager;
import dev.xcolorful.customgun.core.api.gun.attack.IGunAttackManager;
import dev.xcolorful.customgun.core.api.gun.inventory.IGunInventoryManager;
import dev.xcolorful.customgun.core.api.gun.script.IGunScriptManager;
import dev.xcolorful.customgun.core.api.gun.state.IGunStateManager;
import org.jetbrains.annotations.NotNull;

/*
文档译名: 枪械管理器组 (XiaoColorful译)
 */
public record GunManagerGroup(String managerGroupTag,
                              @NotNull IGunActionManager gunActionManager,
                              @NotNull IGunAttackManager gunAttackManager,
                              @NotNull IGunInventoryManager gunInventoryManager,
                              @NotNull IGunScriptManager gunScriptManager,
                              @NotNull IGunStateManager gunStateManager) {
}
