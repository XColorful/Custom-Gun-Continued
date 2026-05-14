package xiao.customgun.forge.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import xiao.customgun.core.api.config.IModConfigSpec;
import xiao.customgun.core.api.config.IModConfigSpecBuilder;
import xiao.customgun.core.api.config.ModConfigType;

public class ForgeModConfigSpecBuilder implements IModConfigSpecBuilder {
    private final ForgeConfigSpec.Builder builder;

    public ForgeModConfigSpecBuilder() {
        this.builder = new ForgeConfigSpec.Builder();
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
        return new ForgeModConfigSpec<>(builder.define(path, defaultValue));
    }
    @Override public IModConfigSpec<Integer> addConfig(String path, int defaultValue, int min, int max) {
        return new ForgeModConfigSpec<>(builder.defineInRange(path, defaultValue, min, max));
    }
    @Override public IModConfigSpec<Double> addConfig(String path, double defaultValue, double min, double max) {
        return new ForgeModConfigSpec<>(builder.defineInRange(path, defaultValue, min, max));
    }

    @Override
    public void buildAndRegister(ModConfigType type) {
        ModConfig.Type forgeType = ModConfigTypeHelper.convert(type);
        ForgeConfigSpec spec = builder.build();
        ModLoadingContext.get().registerConfig(forgeType, spec);
    }
}