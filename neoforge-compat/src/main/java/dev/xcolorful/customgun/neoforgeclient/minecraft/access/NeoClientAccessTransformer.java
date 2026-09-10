package dev.xcolorful.customgun.neoforgeclient.minecraft.access;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.xcolorful.customgun.client.api.minecraft.access.IClientAccessTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
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
        return gui.overlayMessageString;

        // [26.2, )
//        return gui.hud.overlayMessageString;
    }

    @Override public void
    ensureHasSentCarriedItem(
            MultiPlayerGameMode gameMode
    ) {
        gameMode.ensureHasSentCarriedItem();
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
