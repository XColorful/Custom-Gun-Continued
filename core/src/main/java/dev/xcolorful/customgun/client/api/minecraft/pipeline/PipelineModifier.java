package dev.xcolorful.customgun.client.api.minecraft.pipeline;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.minecraft.pipeline.PipelineModifierTag;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("1.21.6")
public enum PipelineModifier implements ResourceTag.RegistryTag {
    NO_COLOR_WRITE(PipelineModifierTag.NO_COLOR_WRITE,
            (pipeline, name) ->
                    null // pipeline.toBuilder().withLocation(name).withColorWrite(false).build()
    ),
    NO_DEPTH_WRITE(PipelineModifierTag.NO_DEPTH_WRITE,
            (pipeline, name) ->
                    null // pipeline.toBuilder().withLocation(name).withDepthWrite(false).build();
    ),
    NO_DEPTH_TEST(PipelineModifierTag.NO_DEPTH_TEST,
            (pipeline, name) ->
                    null // pipeline.toBuilder().withLocation(name).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build();
    ),
    ;

    public final String typeName;
    public final String registryName;
    public final ResourceLocation registryLocation;
    public final IPipelineModifier modifier;
    PipelineModifier(String name, IPipelineModifier modifier) {
        this.typeName = name;
        this.registryLocation = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, name));
        this.registryName = this.registryLocation.toString();
        this.modifier = modifier;
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getRegistryName() {
        return this.registryName;
    }
    @Override public ResourceLocation getRegistryLocation() {
        return this.registryLocation;
    }

    public IPipelineModifier getModifier() {
        return this.modifier;
    }
}
