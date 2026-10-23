package dev.xcolorful.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.resource.assets.AssetsFolderType;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.resource.FileExtensionType;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public abstract class _AssetsDisplay<T extends _AssetsDisplay<T>> extends ResourcePojo<T> {

    private @Nullable Identifier modelLocation;
    private @Nullable _ModelTransform modelTransform;
    private @Nullable Identifier textureLocation;
    private @Nullable Identifier slotTextureLocation;

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
    protected boolean readCustomKey(JsonReader reader, String key) throws IOException {
        return super.readCustomKey(reader, key);
    }

    @Override
    protected void writeCustomValue(JsonWriter writer) {
        super.writeCustomValue(writer);
    }

    // --------Getter & Setter--------

    public final @Nullable Identifier getModelLocation() {
        return modelLocation;
    }
    public final @Nullable _ModelTransform getModelTransform() {
        return modelTransform;
    }
    public final @Nullable Identifier getTextureLocation() {
        return textureLocation;
    }
    public final @Nullable Identifier getSlotTextureLocation() {
        return slotTextureLocation;
    }

    public final void setModelLocation(Identifier modelLocation) {
        this.modelLocation = modelLocation;
    }
    public final void setModelTransform(_ModelTransform modelTransform) {
        this.modelTransform = modelTransform;
    }
    public final void setTextureLocation(Identifier textureLocation) {
        this.textureLocation = textureLocation;
    }
    public final void setSlotTextureLocation(Identifier slotTextureLocation) {
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
    public static @Nullable Identifier applyTextureLocationBackCompatibility(@Nullable Identifier textureLocation) {
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