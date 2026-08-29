package dev.xcolorful.customgun.client.api.minecraft.access;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.ApiStatus;

public interface IClientAccessTransformer {

    void // 返回值
    startUseItem( // 函数名
            Minecraft minecraft // 类名
    ); // 参数列表

    @ApiStatus.AvailableSince("26.2")
    Object // RenderSetup
    getState(
            RenderType renderType
    );

    @ApiStatus.AvailableSince("26.2")
    Object // RenderSetup
    new_RenderSetup(
            RenderPipeline renderPipeline,
            Object renderSetup
    );
}
