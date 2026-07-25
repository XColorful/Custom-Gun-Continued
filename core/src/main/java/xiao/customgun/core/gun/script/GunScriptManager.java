package xiao.customgun.core.gun.script;

import xiao.customgun.CustomGun;
import xiao.customgun.core.api.gun.script.IGunScriptManager;

public class GunScriptManager implements IGunScriptManager {
    public static final GunScriptManager INSTANCE = new GunScriptManager();

    protected GunScriptManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, GunScriptManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IGunScriptRuntime--------
}
