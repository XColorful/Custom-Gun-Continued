/*
 * 跟 BattleRoyale 同构
 */

package dev.xcolorful.customgun.neoforgeclient.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.init.ClientSetup;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ConfigureMainRenderTargetEvent;
import org.jetbrains.annotations.ApiStatus;

@EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID)
public class NeoClientSetup {

    private static final ClientSetup CLIENT_SETUP = ClientSetup.get();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(CLIENT_SETUP::onClientSetup);
    }

    /*
//    为了跨版本提前知道1.21.4neoforge的移植方式，添加此类作为占位符
     */
    @SubscribeEvent
    public static void onConfigureMainRenderTarget(ConfigureMainRenderTargetEvent event) {
        CLIENT_SETUP.onConfigureMainRenderTarget(event::enableStencil);
    }
    @Deprecated(since = "1.21.4")
    @ApiStatus.Internal
    public static void ensureMainRenderTargetStencil() {
        // 1.21.4起变成了构造函数时的参数
//        RenderSystem.recordRenderCall(() -> ClientRenderUtils.getMainRenderTarget(Minecraft.getInstance()).enableStencil());
    }
}
