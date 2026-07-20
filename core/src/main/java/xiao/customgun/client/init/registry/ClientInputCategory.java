/*
 * 跟 BattleRoyale 同构
 */

package xiao.customgun.client.init.registry;

import xiao.customgun.client.api.minecraft.input.CustomInputCategory;

public class ClientInputCategory {
    public static final String CONFIG = CustomInputCategory.CONFIG.getRegistryLocation()
            .getPath();
    public static final String PLAYER = CustomInputCategory.PLAYER.getRegistryLocation()
            .getPath();
    public static final String SHOOTER = CustomInputCategory.SHOOTER.getRegistryLocation()
            .getPath();
}
