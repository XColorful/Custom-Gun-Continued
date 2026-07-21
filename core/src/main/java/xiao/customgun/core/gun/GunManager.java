/*
 * Go to BattleRoyale GameManager 的设计
 */

package xiao.customgun.core.gun;

import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.common.McSide;
import xiao.customgun.core.api.gun.GunManagerGroup;
import xiao.customgun.core.api.gun.IGunManager;
import xiao.customgun.core.gun.inventory.GunInventoryManager;
import xiao.customgun.core.gun.attack.GunAttackManager;
import xiao.customgun.core.gun.action.GunActionManager;
import xiao.customgun.core.gun.state.GunStateManager;

import java.util.HashMap;
import java.util.Map;

public class GunManager implements IGunManager {
    public static final GunManager INSTANCE = new GunManager();

    private final GunManagerGroup defaultGroup = new GunManagerGroup("default",
            GunActionManager.INSTANCE,
            GunAttackManager.INSTANCE,
            GunInventoryManager.INSTANCE,
            GunStateManager.INSTANCE);
    private final Map<String, GunManagerGroup> managerGroups;

    protected GunManager() {
        this.managerGroups = new HashMap<>();
        this.registerRuntimeGroup(defaultGroup);
    }

    public static void init(McSide mcSide) {
    }

    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, GunManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IGameMainManager--------

    @Override
    public boolean registerRuntimeGroup(GunManagerGroup gunManagerGroup) {
        return this.managerGroups.putIfAbsent(gunManagerGroup.managerGroupTag(), gunManagerGroup) == null;
    }
    @Override
    public @NotNull GunManagerGroup getManagerGroup(String managerGroupTag) {
        GunManagerGroup group = this.managerGroups.get(managerGroupTag);
        return group != null ? group : this.defaultGroup;
    }
}
