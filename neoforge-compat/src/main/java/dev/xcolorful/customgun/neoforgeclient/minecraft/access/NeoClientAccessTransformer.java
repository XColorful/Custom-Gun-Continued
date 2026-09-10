package dev.xcolorful.customgun.neoforgeclient.minecraft.access;

//import com.mojang.blaze3d.pipeline.RenderPipeline; // 1.21.4没有RenderPipeline
import dev.xcolorful.customgun.client.api.minecraft.access.IClientAccessTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderType;
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

    @Override public Object
    getState(
            RenderType renderType
    ) {
        return null; // renderType.state;
    }

    @Override public Object
    new_RenderSetup(
            Object renderPipeline,
            Object renderSetup
    ) {
        return null;
    }
}
