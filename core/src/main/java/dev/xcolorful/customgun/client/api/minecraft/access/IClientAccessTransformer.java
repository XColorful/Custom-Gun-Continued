package dev.xcolorful.customgun.client.api.minecraft.access;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.Map;
import java.util.function.Consumer;

public interface IClientAccessTransformer {

    void // 返回值
    startUseItem( // 函数名
            Minecraft minecraft // 类名
    ); // 参数列表

    @Nullable Component
    getOverlayMessageString(
            Gui gui
    );

    void
    ensureHasSentCarriedItem(
            MultiPlayerGameMode gameMode
    );

    /**
     * [1.21.6, ) 烘焙网格自己开 RenderPass，必须知道该 RenderType 画到哪个输出目标
     * @return {@code RenderStateShard.OutputStateShard}；拿不到时返回 {@code null}
     */
//    @Deprecated(since = "26.3")
    @ApiStatus.AvailableSince("1.21.6")
    @Nullable Object
    getOutputState(
            RenderType renderType
    );

    /**
     * [1.21.6, ) 烘焙网格沿用该 RenderType 的管线状态（混合 / 剔除 / 深度），只把着色器换成烘焙用的
     */
    @ApiStatus.AvailableSince("1.21.6")
    RenderPipeline
    getRenderPipeline(
            RenderType renderType
    );

    /**
     * [1.21.11, ) 1.21.11 起 {@link RenderType} 的状态由 {@code RenderSetup} 携带（字段包可见）
     * <li>贴图 / 采样器绑定，含 lightmap 与 overlay</li>
     */
//    @Deprecated(since = "26.2")
    @ApiStatus.AvailableSince("1.21.11")
    @NotNull Map<String, RenderSetup.TextureAndSampler>
    getRenderSetupTextures(
            RenderType renderType
    );

    /**
     * <ul>
     *     <li>[1.21.11, ) glint 等贴图变换不再写进 {@link RenderSystem}，改由 {@code RenderSetup} 提供</li>
     *     <li>[26.2, ) 修饰器改为作用于模型视图矩阵本身（之前作用于矩阵栈）</li>
     * </ul>
     */
    @ApiStatus.AvailableSince("1.21.11")
    @NotNull Matrix4f // Matrix4fStack
    getRenderSetupTextureTransform(
            RenderType renderType
    );

    /**
     * [1.21.11, ) layering 变换由 {@code RenderSetup} 提供；没有时返回 {@code null}
     */
    @ApiStatus.AvailableSince("1.21.11")
    @Nullable
    Consumer<Matrix4fStack>
    getRenderSetupLayering(
            RenderType renderType
    );

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

    @ApiStatus.AvailableSince("26.3")
    RenderPipeline
    RenderSystem_getPIPELINE_MODIFIERS_apply(
            RenderPipeline renderPipeline
    );
}
