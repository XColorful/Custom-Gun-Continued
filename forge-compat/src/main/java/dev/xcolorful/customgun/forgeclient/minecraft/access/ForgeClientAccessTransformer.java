package dev.xcolorful.customgun.forgeclient.minecraft.access;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.xcolorful.customgun.client.api.minecraft.access.IClientAccessTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;

public class ForgeClientAccessTransformer implements IClientAccessTransformer {

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
