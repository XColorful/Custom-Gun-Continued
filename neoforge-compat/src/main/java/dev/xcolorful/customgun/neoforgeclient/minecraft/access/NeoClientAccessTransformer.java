package dev.xcolorful.customgun.neoforgeclient.minecraft.access;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.xcolorful.customgun.client.api.minecraft.access.IClientAccessTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public class NeoClientAccessTransformer implements IClientAccessTransformer {

    @Override public void
    startUseItem(
            Minecraft minecraft
    ) {
        minecraft.startUseItem();
    }

    @Override public RenderSetup
    getState(
            RenderType renderType
    ) {
        return renderType.state;
    }

    @Override public RenderSetup
    new_RenderSetup(
            RenderPipeline renderPipeline,
            RenderSetup renderSetup
    ) {
        return new RenderSetup(renderPipeline,
                renderSetup.textures,
                renderSetup.useLightmap,
                renderSetup.useOverlay,
                renderSetup.layeringTransform,
                renderSetup.outputTarget,
                renderSetup.textureTransform,
                renderSetup.outlineProperty,
                renderSetup.affectsCrumbling,
                renderSetup.sortOnUpload);
    }
}
