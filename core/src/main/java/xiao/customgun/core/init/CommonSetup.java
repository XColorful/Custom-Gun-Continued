/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.core.init;

import xiao.customgun.core.network.NetworkHandler;

public class CommonSetup {

    private static final CommonSetup INSTANCE = new CommonSetup();

    public static CommonSetup get() {
        return INSTANCE;
    }

    private CommonSetup() {}

    public void onCommonSetup() {
        NetworkHandler.get().registerMessages();
    }
}
