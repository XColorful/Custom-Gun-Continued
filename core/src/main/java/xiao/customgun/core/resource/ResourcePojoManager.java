/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;
import xiao.customgun.core.util.JsonUtils;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * 将资源包中的文件流式解析为 Pojo
 */
public abstract class ResourcePojoManager<T extends ResourcePojo<T>>
        extends SimplePreparableReloadListener<Map<Identifier, T>> {

    private final String managerName;
    private final Identifier registryName;
    private final FileToIdConverter fileToIdConverter;
    private final JsonUtils.FromJsonReader<T> fromJson;
    protected Map<Identifier, T> pojoMap;

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

    public ResourcePojoManager(String prefix, String extension, JsonUtils.FromJsonReader<T> fromJson) {
        this(new FileToIdConverter(prefix, extension), fromJson);
    }
    public ResourcePojoManager(FileToIdConverter fileToIdConverter, JsonUtils.FromJsonReader<T> fromJson) {
        this(fileToIdConverter, fromJson, true, true, false, true);
    }
    public ResourcePojoManager(FileToIdConverter fileToIdConverter, JsonUtils.FromJsonReader<T> fromJson,
                               boolean lenientPojo, boolean validateAtRead, boolean validateAtApply, boolean logParseException) {
        this.managerName = this.getClass().getSimpleName();
        this.registryName = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, this.managerName.toLowerCase()));
        this.fileToIdConverter = fileToIdConverter;
        this.fromJson = fromJson;
        this.pojoMap = new HashMap<>();
        this.lenientPojo = lenientPojo;
        this.validateAtRead = validateAtRead;
        this.validateAtApply = validateAtApply;
        this.logParseException = logParseException;
    }

    public Identifier getRegistryName() {
        return this.registryName;
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
            this.fileToIdConverter.listMatchingResources(resourceManager).forEach((location, resource) -> {
                var pojoLocation = fileToIdConverter.fileToId(location);

                try (Reader reader = resource.openAsReader();
                     JsonReader jsonReader = new JsonReader(reader)) {

                    jsonReader.setLenient(this.lenientPojo);
                    T pojo = fromJson.apply(jsonReader);
                    if (pojo != null) {
                        // 能并发，提前验证能省开销
                        if (this.validateAtRead) pojo.validate();

                        if (pojo.isValid()) {
                            map.put(pojoLocation, pojo);
                        } else if (this.logParseException) {
                            CustomGun.LOGGER.warn("{}: Pojo validation failed at prepare stage, skipping: {}", this.managerName, pojoLocation);
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
        } finally {
            profiler.pop();
        }
        return map;
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
        this.pojoMap = pObject;
    }
}