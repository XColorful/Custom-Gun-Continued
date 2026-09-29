package dev.xcolorful.customgun.core.api.config;

import java.util.List;

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
    /**
     * <ul>
     *     枚举和列表必须走这两个重载
     *     <li>它们在 TOML 中的落盘类型是 String / ArrayList</li>
     *     <li>而通用的 addConfig(path, defaultValue) 按 defaultValue 的运行时类型校验</li>
     *     <li>于是每次加载都被判为不正确并回写文件（平台层的 correction + FileWatcher 会构成无限读改写循环）</li>
     *     <li>且 get() 不做类型转换，会把字符串当枚举返回而抛 ClassCastException</li>
     * </ul>
     */
    <E extends Enum<E>> IModConfigSpec<E> addConfig(String path, E defaultValue);
    <T> IModConfigSpec<List<T>> addConfig(String path, List<T> defaultValue);

    void buildAndRegister(ModConfigType type);
}
