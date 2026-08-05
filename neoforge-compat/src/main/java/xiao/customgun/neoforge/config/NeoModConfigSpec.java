package xiao.customgun.neoforge.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import xiao.customgun.core.api.config.IModConfigSpec;

public class NeoModConfigSpec<T> implements IModConfigSpec<T> {
    private final ModConfigSpec.ConfigValue<T> configValue;

    public NeoModConfigSpec(ModConfigSpec.ConfigValue<T> configValue) {
        this.configValue = configValue;
    }

    @Override
    public T get() {
        return configValue.get();
    }

    @Override
    public void set(T value) {
        configValue.set(value);
    }
}
