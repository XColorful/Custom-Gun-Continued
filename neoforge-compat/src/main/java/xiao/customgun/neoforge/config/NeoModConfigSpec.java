package dev.xcolorful.customgun.neoforge.config;

import dev.xcolorful.customgun.core.api.config.IModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec;

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
