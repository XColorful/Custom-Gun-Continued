package xiao.customgun.core.api.config;

/**
 * 封装 ForgeConfigSpec.Builder
 */
public interface IModConfigSpecBuilder {

    void startBuild(String path);
    void finishBuild();

    void addComment(String comment);
    void addComments(String... comments);

    <T> IModConfigSpec<T> addConfig(String path, T defaultValue);
    IModConfigSpec<Integer> addConfig(String path, int defaultValue, int min, int max);
    IModConfigSpec<Double> addConfig(String path, double defaultValue, double min, double max);

    void buildAndRegister(ModConfigType type);
}
