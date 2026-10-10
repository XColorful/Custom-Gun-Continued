package dev.xcolorful.customgun.neoforge.config;

import dev.xcolorful.customgun.core.api.config.ModConfigType;
import net.neoforged.fml.config.ModConfig;

public class ModConfigTypeHelper {

    public static ModConfigType convert(ModConfig.Type type) {
        return switch (type) {
            case LOCAL -> ModConfigType.COMMON;
            case CLIENT -> ModConfigType.CLIENT;
            case SYNCED -> ModConfigType.SERVER;
            default -> ModConfigType.COMMON;
        };
    }

    public static ModConfig.Type convert(ModConfigType type) {
        return switch (type) {
            case COMMON -> ModConfig.Type.LOCAL;
            case CLIENT -> ModConfig.Type.CLIENT;
            case SERVER -> ModConfig.Type.SYNCED;
        };
    }
}