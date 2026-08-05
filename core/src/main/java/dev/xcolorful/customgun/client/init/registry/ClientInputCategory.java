/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.client.init.registry;

import dev.xcolorful.customgun.client.api.minecraft.input.CustomInputCategory;

public class ClientInputCategory {
    @Deprecated(forRemoval = true) public static final String CONFIG = CustomInputCategory.CONFIG.getRegistryLocation()
            .getPath();
    public static final String PLAYER = CustomInputCategory.PLAYER.getRegistryLocation()
            .getPath();
    public static final String SHOOTER = CustomInputCategory.SHOOTER.getRegistryLocation()
            .getPath();
}
