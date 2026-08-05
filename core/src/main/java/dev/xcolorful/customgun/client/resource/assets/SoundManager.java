package dev.xcolorful.customgun.client.resource.assets;

import dev.xcolorful.customgun.client.api.resource.assets.AssetsFolderType;
import dev.xcolorful.customgun.client.sound.SoundPlayManager;
import dev.xcolorful.customgun.core.api.resource.FileExtensionType;
import dev.xcolorful.customgun.core.api.resource.assets.AssetsFolderName;
import dev.xcolorful.customgun.core.resource.ResourceFile;
import dev.xcolorful.customgun.core.resource.ResourceFileManager;
import dev.xcolorful.customgun.core.util.FileUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.PackType;

import java.util.Arrays;
import java.util.List;

/**
 * 目录名称{@link AssetsFolderType}
 */
public abstract class SoundManager<T extends ResourceFile<T>> extends ResourceFileManager<T> {

    @Deprecated public static final FileToIdConverter VANILLA_SOUNDS = new FileToIdConverter(AssetsFolderType.SOUNDS.getFolderName(), FileExtensionType.OGG.getExtensionNameWithDot());
    public static final FileToIdConverter MOD_SOUNDS = new FileToIdConverter(AssetsFolderName.MOD_SOUNDS, FileExtensionType.OGG.getExtensionNameWithDot());
    public static final FileToIdConverter MOD_SOUNDS_OLD1 = new FileToIdConverter(AssetsFolderName.MOD_SOUNDS_OLD1, FileExtensionType.OGG.getExtensionNameWithDot());
    public static final List<FileToIdConverter> MOD_SOUNDS_LISTER = List.of(MOD_SOUNDS, MOD_SOUNDS_OLD1);

    @Deprecated
    private SoundManager(FileUtils.ReadFunction<T> fromStream) {
        super(PackType.CLIENT_RESOURCES, Arrays.asList(VANILLA_SOUNDS, MOD_SOUNDS, MOD_SOUNDS_OLD1), fromStream);
    }

    public static void clearCacheOnReload() {
        SoundPlayManager.get().clearCacheOnReload();
    }
}
