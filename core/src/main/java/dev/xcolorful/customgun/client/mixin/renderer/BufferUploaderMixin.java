package dev.xcolorful.customgun.client.mixin.renderer;

import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import dev.xcolorful.customgun.client.util.ClientRenderProfiler;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 把一次 {@code endBatch} 拆成「缓冲上传」与「draw」两段，并统计 draw 次数与上传字节
 * <li>只在 F12 采集窗口内计时；未采集时每段只有一次 volatile 读</li>
 */
@Mixin(BufferUploader.class)
public class BufferUploaderMixin {

    @Inject(method = "_drawWithShader", at = @At("HEAD"))
    private static void cgc$countDraw(MeshData meshData,
                                      CallbackInfo ci) {
        // [1.20.1, 1.21.1)
//        if (buffer.isEmpty()) return; // this.drawState.vertexCount == 0;

        // [1.21.1, )
        MeshData.DrawState state = meshData.drawState();
        if (state.vertexCount() == 0) return;

        ClientRenderProfiler.countDraw();
        ClientRenderProfiler.countUploadBytes(
                // [1.20.1, 1.21.1)
//                buffer.drawState().bufferSize()
                // [1.21.1, )
                state.vertexCount() * state.format().getVertexSize()
        );
    }

    @Redirect(
            method = "upload",
            at = @At(
                    value = "INVOKE",
                    // [1.20.1, 1.21.1)
//                    target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;upload(Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;)V"
                    // [1.21.1, )
                    target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;upload(Lcom/mojang/blaze3d/vertex/MeshData;)V"
            )
    )
    private static void cgc$timeUpload(VertexBuffer vertexBuffer,
                                       MeshData meshData) {
        long start = ClientRenderProfiler.begin();
        try {
            vertexBuffer.upload(meshData);
        } finally {
            ClientRenderProfiler.end(ClientRenderProfiler.UPLOAD, start);
        }
    }

    @Redirect(
            method = "_drawWithShader",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V"
            )
    )
    private static void cgc$timeDraw(VertexBuffer vertexBuffer,
                                     Matrix4f modelView,
                                     Matrix4f projection,
                                     ShaderInstance shader) {
        long start = ClientRenderProfiler.begin();
        try {
            vertexBuffer.drawWithShader(modelView, projection, shader);
        } finally {
            ClientRenderProfiler.end(ClientRenderProfiler.DRAW, start);
        }
    }
}
