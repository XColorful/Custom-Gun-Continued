package xiao.customgun.neoforge.config;

import net.neoforged.fml.config.ModConfig;
import xiao.customgun.core.api.config.ModConfigType;

public class ModConfigTypeHelper {

    public static ModConfigType convert(ModConfig.Type type) {
        return switch (type) {
            case COMMON -> ModConfigType.COMMON;
            case CLIENT -> ModConfigType.CLIENT;
            case SERVER -> ModConfigType.SERVER;
            default -> ModConfigType.COMMON;
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