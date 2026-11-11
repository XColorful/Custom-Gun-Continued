package dev.xcolorful.customgun.client.api.minecraft.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * 平台侧注册 core shader 的入口，由平台在资源重载事件里实现
 * <p>
 * core 不能直接依赖平台的着色器注册事件（Forge: {@code RegisterShadersEvent}），
 * 因此由 {@link dev.xcolorful.customgun.client.init.registry.ClientRenderRegistry#onRegisterShaders}
 * 声明要注册哪些 shader，平台只负责把事件转成这个接口。
 * <p>
 * 资源重载会重新创建 {@link ShaderInstance}，{@code onLoaded} 必须在每次重载时都回调。
 */
public interface ICoreShaderRegistrar {

    /**
     * @param location 形如 {@code customgun:cgc_baked_entity}，对应 {@code assets/<ns>/shaders/core/<path>.json}
     * @param onLoaded 加载成功后的回调（每次资源重载都会调用）
     */
    void registerCoreShader(@NotNull ResourceLocation location,
                            @NotNull VertexFormat format,
                            @NotNull Consumer<ShaderInstance> onLoaded);
}
