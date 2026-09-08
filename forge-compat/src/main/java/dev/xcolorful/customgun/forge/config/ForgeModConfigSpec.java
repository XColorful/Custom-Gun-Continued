package dev.xcolorful.customgun.forge.config;

import dev.xcolorful.customgun.core.api.config.IModConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeModConfigSpec<T> implements IModConfigSpec<T> {
    private final ForgeConfigSpec.ConfigValue<T> configValue;

    public ForgeModConfigSpec(ForgeConfigSpec.ConfigValue<T> configValue) {
        this.configValue = configValue;
    }

    @Override
    public T get() {
        return configValue.get();
    }

    @Override
    public void set(T value) {
        configValue.set(value);
        if (true) return; // 让IDE保留下面的引用关系

        // [1.21.1neoforge, )
        // 自 1.21.1neoforge 起 set 不会自动 save
        configValue.save();
    }
}