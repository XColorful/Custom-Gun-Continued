package dev.xcolorful.customgun.forge.config;

import dev.xcolorful.customgun.core.api.config.ModConfigType;
import net.minecraftforge.fml.config.ModConfig;

public class ModConfigTypeHelper {

    public static ModConfigType convert(ModConfig.Type type) {
        return switch (type) {
            case COMMON -> ModConfigType.COMMON;
            case CLIENT -> ModConfigType.CLIENT;
            case SERVER -> ModConfigType.SERVER;
        };
    }

    public static ModConfig.Type convert(ModConfigType type) {
        return switch (type) {
            case COMMON -> ModConfig.Type.COMMON;
            case CLIENT -> ModConfig.Type.CLIENT;
            case SERVER -> ModConfig.Type.SERVER;
        };
    }
}
