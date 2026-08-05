package dev.xcolorful.customgun.core.api.gun;

import dev.xcolorful.customgun.core.api.gun.action.IGunActionRuntime;
import dev.xcolorful.customgun.core.api.gun.attack.IGunAttackRuntime;
import dev.xcolorful.customgun.core.api.gun.inventory.IGunInventoryRuntime;
import dev.xcolorful.customgun.core.api.gun.script.IGunScriptRuntime;
import dev.xcolorful.customgun.core.api.gun.state.IGunStateRuntime;

public interface IGunRuntime extends
        IGunActionRuntime,
        IGunAttackRuntime,
        IGunInventoryRuntime,
        IGunScriptRuntime,
        IGunStateRuntime {
}
