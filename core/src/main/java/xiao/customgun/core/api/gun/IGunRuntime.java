package xiao.customgun.core.api.gun;

import xiao.customgun.core.api.gun.inventory.IGunInventoryRuntime;
import xiao.customgun.core.api.gun.attack.IGunAttackRuntime;
import xiao.customgun.core.api.gun.action.IGunActionRuntime;
import xiao.customgun.core.api.gun.state.IGunStateRuntime;

public interface IGunRuntime extends
        IGunActionRuntime,
        IGunAttackRuntime,
        IGunInventoryRuntime,
        IGunStateRuntime {
}
