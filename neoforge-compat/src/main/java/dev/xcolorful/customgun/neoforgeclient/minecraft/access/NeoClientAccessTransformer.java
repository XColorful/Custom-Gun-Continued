package dev.xcolorful.customgun.neoforgeclient.minecraft.access;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.minecraft.access.IClientAccessTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.Map;
import java.util.function.Consumer;

public class NeoClientAccessTransformer implements IClientAccessTransformer {
    public static final NeoClientAccessTransformer INSTANCE = new NeoClientAccessTransformer();

    /**
     * 手动传NeoForge类为旧实现方式，现直接调用 {@link CustomGunClient#getAccessTransformer()} 即可
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(forRemoval = false)
    public NeoClientAccessTransformer() {
    }

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

    @Override public @Nullable Object
    getOutputState(
            RenderType renderType
    ) {
//        return null;
        // [1.20.6, 1.21.11)
//        return ((RenderType.CompositeRenderType) renderType).state.outputState;

        // [1.21.11, )
        return renderType.state.outputTarget;
    }

    @Override public RenderPipeline
    getRenderPipeline(
            RenderType renderType
    ) {
//        return null;

        // [1.20.6, 1.21.11)
//        return ((RenderType.CompositeRenderType) renderType).renderPipeline;

        // [1.21.11, )
        return renderType.pipeline();
    }

    @Override public @NotNull Map<String, RenderSetup.TextureAndSampler>
    getRenderSetupTextures(
            RenderType renderType
    ) {
        return renderType.state.getTextures();
    }

    @Override public @NotNull Matrix4f
    getRenderSetupTextureTransform(
            RenderType renderType
    ) {
        return renderType.state.textureTransform.getMatrix();
    }

    @Override public @Nullable Consumer<Matrix4fStack>
    getRenderSetupLayering(
            RenderType renderType
    ) {
        return renderType.state.layeringTransform.getModifier();
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

    @Override public RenderPipeline
    RenderSystem_getPIPELINE_MODIFIERS_apply(
            RenderPipeline renderPipeline
    ) {
        return null;
    }
}
