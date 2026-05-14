package xiao.customgun.forge.config;

import net.minecraftforge.common.ForgeConfigSpec;
import xiao.customgun.core.api.config.IModConfigSpec;

public class ForgeModConfigSpec<T> implements IModConfigSpec<T> {
    private final ForgeConfigSpec.ConfigValue<T> configValue;

    public ForgeModConfigSpec(ForgeConfigSpec.ConfigValue<T> configValue) {
        this.configValue = configValue;
    }

    @Override
    public T get() {
        return configValue.get();
    }
}