package xiao.customgun.core.resource;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;
import xiao.customgun.core.util.FileUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 将资源包中的文件流式解析为 ResourceFile
 * <p>
 * 跟 {@link ResourcePojoManager} 隔离维护，以利于去虚拟化优化
 */
public abstract class ResourceFileManager<T extends ResourceFile<T>>
        extends SimplePreparableReloadListener<Map<ResourceLocation, T>> {

    private final String managerName;
    private final ResourceLocation registryName;
    private final PackType packType;
    private final List<FileToIdConverter> fileToIdConverters;
    private final FileUtils.ReadFunction<T> fromStream;
    protected Map<ResourceLocation, T> fileMap;

    private boolean validateAtRead;
    /**
     * {@link #prepare}阶段是多线程并发的
     * 如果validate依赖其他数据，应该延迟到 {@link #apply} 或者使用时验证
     */
    private boolean validateAtApply;
    private boolean logParseException;

    public ResourceFileManager(PackType packType, String prefix, String extension, FileUtils.ReadFunction<T> fromStream) {
        this(packType, List.of(new FileToIdConverter(prefix, extension)), fromStream);
    }
    public ResourceFileManager(PackType packType, List<String> prefixList, String extension, FileUtils.ReadFunction<T> fromStream) {
        this(packType, prefixList.stream()
                        .map(prefix -> new FileToIdConverter(prefix, extension))
                        .toList(),
                fromStream);
    }
    public ResourceFileManager(PackType packType, List<FileToIdConverter> fileToIdConverters, FileUtils.ReadFunction<T> fromStream) {
        this(packType, fileToIdConverters, fromStream, true, false, true);
    }
    public ResourceFileManager(PackType packType, List<FileToIdConverter> fileToIdConverters, FileUtils.ReadFunction<T> fromStream,
                               boolean validateAtRead, boolean validateAtApply, boolean logParseException) {
        this.managerName = this.getClass().getSimpleName();
        this.registryName = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, this.managerName.toLowerCase()));
        this.packType = packType;
        this.fileToIdConverters = fileToIdConverters;
        this.fromStream = fromStream;
        this.fileMap = new HashMap<>();
        this.validateAtRead = validateAtRead;
        this.validateAtApply = validateAtApply;
        this.logParseException = logParseException;
    }

    public ResourceLocation getRegistryName() {
        return this.registryName;
    }
    public PackType getPackType() {
        return this.packType;
    }
    public List<FileToIdConverter> getFileToIdConverters() {
        return this.fileToIdConverters;
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

    public T getFile(ResourceLocation fileLocation) {
        return fileMap.get(fileLocation);
    }

    public Map<ResourceLocation, T> getAllFiles() {
        return fileMap;
    }

    /**
     * 多线程处理
     */
    @Override
    protected @NotNull Map<ResourceLocation, T> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        profiler.push(() -> this.managerName);
        Map<ResourceLocation, T> map = new HashMap<>();
        try {
            for (FileToIdConverter fileToIdConverter : this.fileToIdConverters) {
                fileToIdConverter.listMatchingResources(resourceManager).forEach((location, resource) -> {
                    var fileLocation = fileToIdConverter.fileToId(location);
                    CustomGun.LOGGER.debug("Prepare rl: {}", fileLocation);
                    if (!isFileLocationValid(fileLocation)) return;

                    try (InputStream inputStream = resource.open()) {

                        T file = fromStream.apply(inputStream, fileLocation);
                        if (file != null) {
                            // 能并发，提前验证能省开销
                            if (this.validateAtRead) file.validate();

                            if (file.isValid()) {
                                onPrepareFile(map, fileLocation, file);
                            } else if (this.logParseException) {
                                CustomGun.LOGGER.warn("{}: File validation failed at prepare stage, skipping: {}", this.managerName, fileLocation);
                            }
                        }
                    } catch (Exception e) { // IO 异常或其他未知错误
                        if (this.logParseException) {
                            CustomGun.LOGGER.error("{}: Failed to read file at: {}", this.managerName, fileLocation, e);
                        }
                    }
                });
            }
        } finally {
            profiler.pop();
        }
        return map;
    }
    protected boolean isFileLocationValid(ResourceLocation fileLocation) {
        return true;
    }
    protected void onPrepareFile(Map<ResourceLocation, T> map, ResourceLocation fileLocation, T file) {
        map.put(fileLocation, file);
    }

    /**
     * 单线程处理 (线程安全)
     */
    @Override
    protected void apply(@NotNull Map<ResourceLocation, T> pObject, @NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
        if (this.validateAtApply) {
            var iterator = pObject.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                T file = entry.getValue();
                file.validate();
                if (!file.isValid()) {
                    iterator.remove();
                    if (this.logParseException) {
                        CustomGun.LOGGER.warn("{}: File validation failed at apply stage, skipping: {}", this.managerName, entry.getKey());
                    }
                }
            }
        }
        onApplyFileMap(pObject);
    }
    protected void onApplyFileMap(Map<ResourceLocation, T> newFileMap) {
        this.fileMap = newFileMap;
    }
}