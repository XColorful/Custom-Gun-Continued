package xiao.customgun.core.api.gun;

import xiao.customgun.core.api.gun.ammo.IGunInventoryRuntime;
import xiao.customgun.core.api.gun.shoot.IGunAttackRuntime;
import xiao.customgun.core.api.gun.state.IGunActionRuntime;

public interface IGunRuntime extends IGunAttackRuntime, IGunActionRuntime, IGunInventoryRuntime {
}
