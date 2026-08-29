package dev.xcolorful.customgun.neoforgeclient.minecraft.access;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.xcolorful.customgun.client.api.minecraft.access.IClientAccessTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;

public class NeoClientAccessTransformer implements IClientAccessTransformer {

    @Override public void
    startUseItem(
            Minecraft minecraft
    ) {
        minecraft.startUseItem();
    }

    @Override public Object
    getState(
            RenderType renderType
    ) {
        return null; // renderType.state;
    }

    @Override public Object
    new_RenderSetup(
            RenderPipeline renderPipeline,
            Object renderSetup
    ) {
        return null;
    }
}
