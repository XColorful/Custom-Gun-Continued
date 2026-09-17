package dev.xcolorful.customgun.neoforgeclient.minecraft.access;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.xcolorful.customgun.client.api.minecraft.access.IClientAccessTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class NeoClientAccessTransformer implements IClientAccessTransformer {

    @Override public void
    startUseItem(
            Minecraft minecraft
    ) {
        minecraft.startUseItem();
    }

    @Override public @Nullable Component
    getOverlayMessageString(
            Gui gui
    ) {
        // [1.20.1, 26.2)
//        return gui.overlayMessageString;

        // [26.2, )
        return gui.hud.overlayMessageString;
    }

    @Override public void
    ensureHasSentCarriedItem(
            MultiPlayerGameMode gameMode
    ) {
        gameMode.ensureHasSentCarriedItem();
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
        /*
        26.3 的 RenderSetup 是不可变类：OutputTarget 被 OitPipelineSet 取代
        多出 outlineTextureName / forceSolidModelPhase，构造参数顺序也随之调整
         */
        return new RenderSetup(renderPipeline,
                renderSetup.oitPipelineSet,
                renderSetup.textures,
                renderSetup.useLightmap,
                renderSetup.useOverlay,
                renderSetup.layeringTransform,
                renderSetup.textureTransform,
                renderSetup.outlineProperty,
                renderSetup.outlineTextureName,
                renderSetup.affectsCrumbling,
                renderSetup.sortOnUpload,
                renderSetup.forceSolidModelPhase);
    }

    @Override public RenderPipeline
    RenderSystem_getPIPELINE_MODIFIERS_apply(
            RenderPipeline renderPipeline
    ) {
        return RenderSystem.PIPELINE_MODIFIERS.apply(renderPipeline);
    }
}
