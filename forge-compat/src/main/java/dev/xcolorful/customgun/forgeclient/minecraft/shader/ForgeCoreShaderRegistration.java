package dev.xcolorful.customgun.forgeclient.minecraft.shader;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.init.registry.ClientRenderRegistry;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.client.event.RegisterShadersEvent;

/**
 * 把 Forge 的着色器注册事件转成 core 的 {@link ClientRenderRegistry#onRegisterShaders} 入口
 * <p>
 * core 不能直接 import Forge 事件；注册清单由 core 声明，这里只做转发。
 * {@code RegisterShadersEvent} 每次资源重载都会重新触发，{@code onLoaded} 由 core 覆盖保存最新实例。
 */
public class ForgeCoreShaderRegistration {

    private ForgeCoreShaderRegistration() {}

    public static void onRegisterShaders(RegisterShadersEvent event) {
        ClientRenderRegistry.onRegisterShaders((location, format, onLoaded) -> {
            try {
                event.registerShader(
                        new ShaderInstance(event.getResourceProvider(), location, format),
                        onLoaded);
            } catch (Exception e) {
                // ShaderInstance 会把编译 / JSON 错误包成运行时异常，这里一并兜住：
                // 注册失败时 core 会退回即时管线，不能让资源重载崩掉
                CustomGun.LOGGER.error("Failed to register core shader {}", location, e);
            }
        });
    }
}
