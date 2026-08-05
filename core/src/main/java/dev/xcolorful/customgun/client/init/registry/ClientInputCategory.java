/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.client.init.registry;

import dev.xcolorful.customgun.client.api.minecraft.input.CustomInputCategory;
import net.minecraft.client.KeyMapping;

public class ClientInputCategory {
    @Deprecated(forRemoval = true) public static final KeyMapping.Category CONFIG = new KeyMapping.Category(CustomInputCategory.CONFIG.getRegistryLocation());
    public static final KeyMapping.Category PLAYER = new KeyMapping.Category(CustomInputCategory.PLAYER.getRegistryLocation());
    public static final KeyMapping.Category SHOOTER = new KeyMapping.Category(CustomInputCategory.SHOOTER.getRegistryLocation());
}
