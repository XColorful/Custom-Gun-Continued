package dev.xcolorful.customgun.neoforge.config;

import dev.xcolorful.customgun.core.api.config.IModConfigSpec;
import dev.xcolorful.customgun.core.api.config.IModConfigSpecBuilder;
import dev.xcolorful.customgun.core.api.config.ModConfigType;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class NeoModConfigSpecBuilder implements IModConfigSpecBuilder {
    private final ModConfigSpec.Builder builder;

    public NeoModConfigSpecBuilder() {
        this.builder = new ModConfigSpec.Builder();
    }

    @Override
    public void startBuild(String path) {
        builder.push(path);
    }

    @Override
    public void finishBuild() {
        builder.pop();
    }

    @Override public void addComment(String comment) {
        builder.comment(comment);
    }
    @Override public void addComments(String... comments) {
        builder.comment(comments);
    }

    @Override public <T> IModConfigSpec<T> addConfig(String path, T defaultValue) {
        return new NeoModConfigSpec<>(builder.define(path, defaultValue));
    }
    @Override public IModConfigSpec<Integer> addConfig(String path, int defaultValue, int min, int max) {
        return new NeoModConfigSpec<>(builder.defineInRange(path, defaultValue, min, max));
    }
    @Override public IModConfigSpec<Double> addConfig(String path, double defaultValue, double min, double max) {
        return new NeoModConfigSpec<>(builder.defineInRange(path, defaultValue, min, max));
    }
    @Override public <E extends Enum<E>> IModConfigSpec<E> addConfig(String path, E defaultValue) {
        return new NeoModConfigSpec<>(builder.defineEnum(path, defaultValue));
    }
    @Override public <T> IModConfigSpec<List<T>> addConfig(String path, List<T> defaultValue) {
        return new NeoModConfigSpec<>(builder.define(path, defaultValue, o -> o instanceof List));
    }

    @Override
    public void buildAndRegister(ModConfigType type) {
        ModConfig.Type neoType = ModConfigTypeHelper.convert(type);
        ModConfigSpec spec = builder.build();
        ModLoadingContext.get().registerConfig(neoType, spec);
    }
}