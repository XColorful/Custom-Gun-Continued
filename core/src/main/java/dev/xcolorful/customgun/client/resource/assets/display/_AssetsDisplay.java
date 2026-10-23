package dev.xcolorful.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.resource.assets.AssetsFolderType;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.resource.FileExtensionType;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public abstract class _AssetsDisplay<T extends _AssetsDisplay<T>> extends ResourcePojo<T> {

    private @Nullable ResourceLocation modelLocation;
    private @Nullable _ModelTransform modelTransform;
    private @Nullable ResourceLocation textureLocation;
    private @Nullable ResourceLocation slotTextureLocation;

    @Override
    protected void validatePojo() {
        this.setValid(true);
    }

    @Override
    protected T copyTo(final T pojo) {
        pojo.setModelLocation(this.modelLocation);

        @Nullable _ModelTransform modelTransform = null; {
            if (this.modelTransform != null) modelTransform = this.modelTransform.copy();
        } pojo.setModelTransform(modelTransform);
        pojo.setTextureLocation(this.textureLocation);
        pojo.setSlotTextureLocation(this.slotTextureLocation);
        return pojo;
    }

    @Override
    protected boolean readUnknownKey(JsonReader reader, String key) throws IOException {
        return super.readUnknownKey(reader, key);
    }

    // --------Getter & Setter--------

    public final @Nullable ResourceLocation getModelLocation() {
        return modelLocation;
    }
    public final @Nullable _ModelTransform getModelTransform() {
        return modelTransform;
    }
    public final @Nullable ResourceLocation getTextureLocation() {
        return textureLocation;
    }
    public final @Nullable ResourceLocation getSlotTextureLocation() {
        return slotTextureLocation;
    }

    public final void setModelLocation(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
    }
    public final void setModelTransform(_ModelTransform modelTransform) {
        this.modelTransform = modelTransform;
    }
    public final void setTextureLocation(ResourceLocation textureLocation) {
        this.textureLocation = textureLocation;
    }
    public final void setSlotTextureLocation(ResourceLocation slotTextureLocation) {
        this.slotTextureLocation = slotTextureLocation;
    }

    // --------Back Compatibility--------

    @SuppressWarnings("unchecked")
    @Override
    public T applyBackCompatibility() {
        this.textureLocation = applyTextureLocationBackCompatibility(this.textureLocation);
        this.slotTextureLocation = applyTextureLocationBackCompatibility(this.slotTextureLocation);
        return (T) this;
    }

    private static final IMcRegistry mcRegistry = CustomGun.getMcRegistry();
    private static final String textureLocationPrefix = AssetsFolderType.TEXTURES.getFolderPathPrefix();
    private static final String textureLocationSuffix = FileExtensionType.PNG.getExtensionNameWithDot();
    public static @Nullable ResourceLocation applyTextureLocationBackCompatibility(@Nullable ResourceLocation textureLocation) {
        if (textureLocation == null) return null;

        String rlPath = textureLocation.getPath();

        if (!rlPath.startsWith(textureLocationPrefix)) {
            rlPath = textureLocationPrefix + rlPath;
            if (!rlPath.endsWith(textureLocationSuffix)) {
                rlPath = rlPath + textureLocationSuffix;
            }
            return mcRegistry.createResourceLocation(textureLocation.getNamespace() + ":" + rlPath);
        } else {
            // 如果美术知道要加前缀，就不检查 (可自定其他后缀?)
        }

        return textureLocation;
    }
}