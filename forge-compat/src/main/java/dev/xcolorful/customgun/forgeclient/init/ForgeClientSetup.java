/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.forgeclient.init;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.init.ClientSetup;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.ApiStatus;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeClientSetup {

    private static final ClientSetup CLIENT_SETUP = ClientSetup.get();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(CLIENT_SETUP::onClientSetup);
    }

    /*
    为了跨版本提前知道1.21.4neoforge的移植方式，添加此类作为占位符
     */
    @SubscribeEvent
    public static void onConfigureMainRenderTarget(FMLCommonSetupEvent event) {
        CLIENT_SETUP.onConfigureMainRenderTarget(ForgeClientSetup::ensureMainRenderTargetStencil);
    }
    @ApiStatus.Internal
    public static void ensureMainRenderTargetStencil() {
        RenderSystem.recordRenderCall(() -> ClientRenderUtils.getMainRenderTarget(Minecraft.getInstance()).enableStencil());
    }
}
