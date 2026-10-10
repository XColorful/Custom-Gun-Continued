package dev.xcolorful.customgun.core.init;

import dev.xcolorful.customgun.core.entity.LivingShooterSyncKey;
import dev.xcolorful.customgun.core.network.NetworkHandler;
import dev.xcolorful.customgun.core.text.placeholder.gun.GunAmmoParser;

public class CommonSetup {

    private static final CommonSetup INSTANCE = new CommonSetup();
    public static CommonSetup get() {
        return INSTANCE;
    }
    private CommonSetup() {}

    public void onCommonSetup() {
        NetworkHandler.get().registerMessages();
        LivingShooterSyncKey.registerAll();

        this._onBuiltinAddonClientSetup();
    }
    /**
     * 模组内置的扩展模块，相当于可拆卸的独立扩展模组
     */
    private void _onBuiltinAddonClientSetup() {
        GunAmmoParser.init();
    }

    private boolean LOAD_COMPLETE = false;
    public boolean isLoadComplete() {
        return LOAD_COMPLETE;
    }
    public void onLoadComplete() {
        LOAD_COMPLETE = true;
    }
}
