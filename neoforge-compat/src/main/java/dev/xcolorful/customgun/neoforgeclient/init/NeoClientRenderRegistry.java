package dev.xcolorful.customgun.neoforgeclient.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.init.registry.ClientRenderRegistry;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID)
public class NeoClientRenderRegistry {

    /**
     * {@code RegisterShadersEvent} 每次资源重载都会重新触发，{@code onLoaded} 由 core 覆盖保存最新实例
     */
    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        ClientRenderRegistry.onRegisterShaders((location, format, onLoaded) -> {
            try {
                event.registerShader(
                        new ShaderInstance(event.getResourceProvider(), location, format),
                        onLoaded);
            } catch (Exception e) {
                /*
                ShaderInstance 会把编译 / JSON 错误包成运行时异常，这里一并兜住：
                注册失败时 core 会退回即时管线，不能让资源重载崩掉
                 */
                CustomGun.LOGGER.error("Failed to register core shader {}", location, e);
            }
        });
    }
}
