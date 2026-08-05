/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.resource.INetworkCacheReloadListener;
import dev.xcolorful.customgun.core.util.JsonUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 将资源包中的文件流式解析为 Pojo
 */
public abstract class ResourcePojoManager<T extends ResourcePojo<T>>
        extends SimplePreparableReloadListener<Map<Identifier, T>> {

    private final String managerName;
    private final Identifier registryName;
    private final PackType packType;
    private final List<FileToIdConverter> fileToIdConverters;
    private final JsonUtils.ReadFunction<T> fromJson;
    protected @NotNull Map<Identifier, T> pojoMap;

    /**
     * TODO 当前网络缓存使用 JSON String
     * 后续可改为 UTF-8 byte[]（或直接 ByteBuf）以减少 String 分配和
     * UTF-16/UTF-8 转换开销，但需要修改网络协议及客户端解析逻辑
     */
    protected @NotNull Map<Identifier, String> stringMap;

    /**
     * 是否允许带注释的非标准 Json
     * 兼容 TaCZ 把文档放枪包而不是 Wiki 的做法
     */
    private boolean lenientPojo;
    private boolean validateAtRead;
    /**
     * {@link #prepare}阶段是多线程并发的
     * 如果validate依赖其他数据，应该延迟到 {@link #apply} 或者使用时验证
     */
    private boolean validateAtApply;
    private boolean logParseException;

    public ResourcePojoManager(PackType packType, String prefix, String extension, JsonUtils.ReadFunction<T> fromJson) {
        this(packType, List.of(new FileToIdConverter(prefix, extension)), fromJson);
    }
    public ResourcePojoManager(PackType packType, List<String> prefixList, String extension, JsonUtils.ReadFunction<T> fromJson) {
        this(packType, prefixList.stream()
                        .map(prefix -> new FileToIdConverter(prefix, extension))
                        .toList(),
                fromJson);
    }
    public ResourcePojoManager(PackType packType, List<FileToIdConverter> fileToIdConverter, JsonUtils.ReadFunction<T> fromJson) {
        this(packType, fileToIdConverter, fromJson, true, true, false, true);
    }
    public ResourcePojoManager(PackType packType, List<FileToIdConverter> fileToIdConverters, JsonUtils.ReadFunction<T> fromJson,
                               boolean lenientPojo, boolean validateAtRead, boolean validateAtApply, boolean logParseException) {
        this.managerName = this.getClass().getSimpleName();
        this.registryName = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, this.managerName.toLowerCase()));
        this.packType = packType;
        this.fileToIdConverters = fileToIdConverters;
        this.fromJson = fromJson;
        this.pojoMap = new HashMap<>();
        this.lenientPojo = lenientPojo;
        this.validateAtRead = validateAtRead;
        this.validateAtApply = validateAtApply;
        this.logParseException = logParseException;
        this.stringMap = new HashMap<>();
    }

    public Identifier getRegistryName() {
        return this.registryName;
    }
    public PackType getPackType() {
        return this.packType;
    }
    public List<FileToIdConverter> getFileToIdConverters() {
        return this.fileToIdConverters;
    }

    public final void setLenientPojo(boolean lenientPojo) {
        this.lenientPojo = lenientPojo;
    }
    public final void setValidateAtRead(boolean validateAtRead) {
        this.validateAtRead = validateAtRead;
    }
    public final void setValidateAtApply(boolean validateAtApply) {
        this.validateAtApply = validateAtApply;
    }
    public final void setLogParseException(boolean logParseException) {
        this.logParseException = logParseException;
    }

    public T getPojo(Identifier pojoLocation) {
        return pojoMap.get(pojoLocation);
    }

    public Map<Identifier, T> getAllPojo() {
        return pojoMap;
    }

    /**
     * 多线程处理
     */
    @Override
    protected @NotNull Map<Identifier, T> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        profiler.push(() -> this.managerName);
        Map<Identifier, T> map = new HashMap<>();
        try {
            for (FileToIdConverter fileToIdConverter : this.fileToIdConverters) {
                fileToIdConverter.listMatchingResources(resourceManager).forEach((location, resource) -> {
                    var pojoLocation = fileToIdConverter.fileToId(location);
                    if (!isPojoLocationValid(pojoLocation)) return;

                    try (Reader reader = resource.openAsReader();
                         JsonReader jsonReader = new JsonReader(reader)) {

                        jsonReader.setLenient(this.lenientPojo);
                        T pojo = fromJson.apply(jsonReader);
                        if (pojo != null) {
                            // 能并发，提前验证能省开销
                            if (this.validateAtRead) {
                                pojo.validate();
                                if (pojo.isValid()) {
                                    onPreparePojo(map, pojoLocation, pojo);
                                } else if (this.logParseException) {
                                    CustomGun.LOGGER.warn("{}: Pojo validation failed at prepare stage, skipping: {}", this.managerName, pojoLocation);
                                }
                            } else {
                                onPreparePojo(map, pojoLocation, pojo);
                            }
                        }
                    } catch (JsonSyntaxException | MalformedJsonException e) { // JSON 语法错误
                        if (this.logParseException) {
                            CustomGun.LOGGER.error("{}: Malformed JSON file detected at: {}", this.managerName, pojoLocation, e);
                        }
                    } catch (Exception e) { // IO 异常或其他未知错误
                        if (this.logParseException) {
                            CustomGun.LOGGER.error("{}: Failed to read pojo file at: {}", this.managerName, pojoLocation, e);
                        }
                    }
                });
            }
        } finally {
            profiler.pop();
        }
        return map;
    }
    protected boolean isPojoLocationValid(Identifier pojoLocation) {
        return true;
    }
    protected void onPreparePojo(Map<Identifier, T> map, Identifier pojoLocation, T pojo) {
        map.put(pojoLocation, pojo);
    }

    /**
     * 单线程处理 (线程安全)
     */
    @Override
    protected void apply(@NotNull Map<Identifier, T> pObject, @NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
        if (this.validateAtApply) {
            var iterator = pObject.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                T pojo = entry.getValue();
                pojo.validate();
                if (!pojo.isValid()) {
                    iterator.remove();
                    if (this.logParseException) {
                        CustomGun.LOGGER.warn("{}: Pojo validation failed at apply stage, skipping: {}", this.managerName, entry.getKey());
                    }
                }
            }
        }
        onApplyPojoMap(pObject);
        onWriteNetworkCache();
    }
    protected void onApplyPojoMap(Map<Identifier, T> newPojoMap) {
        this.pojoMap = newPojoMap;
    }

    protected void onWriteNetworkCache() {
        if (this instanceof INetworkCacheReloadListener) this.writeStringMap();
    }
    protected void writeStringMap() {
        Map<Identifier, String> newStringMap = new HashMap<>();
        for (var entry : this.pojoMap.entrySet()) {
            var pojoLocation = entry.getKey();
            try (StringWriter stringWriter = new StringWriter();
                 JsonWriter writer = new JsonWriter(stringWriter)) {

                writer.setLenient(this.lenientPojo);
                entry.getValue().toJson(writer);
                newStringMap.put(pojoLocation, stringWriter.toString());
            } catch (Exception e) { // IO 异常或其他未知错误
                if (this.logParseException) {
                    CustomGun.LOGGER.error("{}: Failed to write pojo file at: {}", this.managerName, pojoLocation, e);
                }
            }
        }
        this.stringMap = newStringMap;
    }
    public Map<Identifier, String> getNetworkCache() {
        return this.stringMap;
    }
}