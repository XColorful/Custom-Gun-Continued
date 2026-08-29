/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.api.model.IModelObjectRender;
import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockRenderer;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.compat.oculus.OculusCompat;
import dev.xcolorful.customgun.client.model.bedrock.NodeTransform;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry.NodeName;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry._Bone;
import dev.xcolorful.customgun.client.util.ClientModelUtils;
import dev.xcolorful.customgun.core.resource.instance.PojoInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 对应原模组{@code com.tacz.guns.client.model.bedrock.BedrockModel}
 */
public class ModelObject extends PojoInstance<BedrockModel> implements IModelObjectRender {

    /**
     * 存储 ModelRender 子模型的 HashMap
     * <br>
     * private只是为了集中看方法引用，如{@link #modelMap_put}
     */
    private final HashMap<String, IBedrockRenderer> modelMap = new HashMap<>();
    /**
     * 存储 Bones 的 HashMap，主要是给后面寻找父骨骼进行坐标转换用的
     */
    protected final HashMap<String, _Bone> indexBones = new HashMap<>();
    /**
     * 哪些模型需要渲染。加载进父骨骼的子骨骼是不需要渲染的
     */
    protected final List<BedrockPart> shouldRender = new LinkedList<>();

    /**
     * 委托到渲染结束时执行的渲染器，用于特殊部分的渲染，如手臂
     */
    protected ArrayList<IModelComponentRenderer> delegateRenderers = new ArrayList<>();

    /**
     * 模型的中心点
     */
    protected @Nullable Vec3 offset = null;
    protected boolean hasSize = false;
    protected float width = 0;
    protected float height = 0;

    ModelObject(@NotNull BedrockModel pojo) {
        super(pojo);
    }

    public static @Nullable ModelObject fromPojo(BedrockModel pojo) {
        if (pojo == null) return null;
        ModelObject instance = new ModelObject(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        { // 加载模型
            BedrockModel pojo = this.getPojo();
            String formatVersion = pojo.getFormatVersion();
            if (true) {
                this.loadNewModel(pojo);
            } else {
                this.loadLegacyModel(pojo);
            }
        }

        { // 应用发光
            for (IBedrockRenderer iRenderer : this.modelMap_values()) {
                var renderer = iRenderer.getModelRenderer();
                if (NodeName.Suffix.ILLUMINATE.matches(renderer.name)) {
                    renderer.illuminated = true;
                }
            }
        }

        return true;
    }

    @Override protected boolean isPojoValid() {
        if (!super.isPojoValid()) return false;

        return true;
    }

    protected void loadNewModel(BedrockModel pojo) {
        _ModelLoader.loadNewModel(this, pojo);
    }
    protected void loadLegacyModel(BedrockModel pojo) {
        _ModelLoader.loadLegacyModel(this, pojo);
    }

    protected @Nullable List<BedrockPart> getPath(@Nullable IBedrockRenderer renderer) {
        if (renderer == null) return null;

        BedrockPart part = renderer.getModelRenderer();
        List<BedrockPart> path = new ArrayList<>();
        Deque<BedrockPart> parents = new ArrayDeque<>();
        do {
            parents.push(part);
            part = part.getParent();
        } while (part != null);

        while (!parents.isEmpty()) {
            path.add(parents.pop());
        }
        return path;
    }

    // --------Getter--------

    public boolean hasSize() {
        return this.hasSize;
    }
    public float getWidth() {
        return this.width;
    }
    public float getHeight() {
        return this.height;
    }
    public BedrockPart getNode(String nodeName) {
        @Nullable IBedrockRenderer renderer = this.modelMap.get(nodeName);
        return renderer != null ? renderer.getModelRenderer() : null;
    }
    public _Bone getBone(String name) {
        return this.indexBones.get(name);
    }
    public List<BedrockPart> getShouldRender() {
        return this.shouldRender;
    }
    public HashMap<String, _Bone> getIndexBones() {
        return this.indexBones;
    }

    @ApiStatus.Internal protected @Nullable IBedrockRenderer modelMap_get(String nodeName) {
        return this.modelMap.get(nodeName);
    }
    @ApiStatus.Internal protected Collection<IBedrockRenderer> modelMap_values() {
        return this.modelMap.values();
    }
    @ApiStatus.Internal protected Set<Map.Entry<String, IBedrockRenderer>> modelMap_entrySet() {
        return this.modelMap.entrySet();
    }

    // --------Setter--------

    public void delegateRender(IModelComponentRenderer renderer) {
        delegateRenderers.add(renderer);
    }

    @ApiStatus.Internal
    protected void setRotationAngle(BedrockPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
        modelRenderer.setInitRotationAngle(x, y, z);
    }

    @ApiStatus.Internal protected IBedrockRenderer modelMap_put(String key, IBedrockRenderer renderer) {
        return this.modelMap.put(key, renderer);
    }
    @ApiStatus.Internal protected IBedrockRenderer modelMap_putIfAbsent(String key, IBedrockRenderer renderer) {
        return this.modelMap.putIfAbsent(key, renderer);
    }

    // --------IRenderObject--------

    public void render(PoseStack matrixStack,
                       ItemDisplayContext transformType,
                       RenderType renderType,
                       int light, int overlay,
                       float red, float green, float blue, float alpha) {
        @Nullable Object collector = ClientRenderHelper.FirstPersonArmHelper.getFirstPersonArmCollector();
        if (collector == null) return;

        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer builder = bufferSource.getBuffer(renderType);

        List<BedrockPart> parts = new ArrayList<>(); { // [26.2, )
            for (IBedrockRenderer renderer : this.modelMap_values()) {
                parts.add(renderer.getModelRenderer());
            }
        }
        List<NodeTransform> animatedTransforms = new ArrayList<>(parts.size()); { // [26.2, )
            for (BedrockPart part : parts) {
                animatedTransforms.add(NodeTransform.capture(part));
            }
        }

        RenderType bakedRenderType = ClientRenderHelper.bakePipelineState(renderType); // [26.2, )

        {
            // flush 阶段节点已被清空，先保存当前（已清空）状态，恢复动画状态绘制，最后再还原已清空状态
            List<NodeTransform> cleanedTransforms = new ArrayList<>(parts.size()); { // [26.2, )
                for (int i = 0; i < parts.size(); i++) {
                    BedrockPart part = parts.get(i);
                    cleanedTransforms.add(NodeTransform.capture(part));
                }
                for (int i = 0; i < animatedTransforms.size(); i++) {
                    NodeTransform transform = animatedTransforms.get(i);
                    transform.apply();
                }
            }

            try {
                matrixStack.pushPose(); {
                    for (int i = 0; i < this.shouldRender.size(); i++) {
                        this.shouldRender.get(i)
                                .render(matrixStack,
                                        transformType,
                                        builder,
                                        light, overlay,
                                        red, green, blue, alpha);
                    }
                }
                matrixStack.popPose();

                if (!OculusCompat.endBatch(bufferSource)) {
                    bufferSource.endBatch();
                }

                for (int i = 0; i < this.delegateRenderers.size(); i++) {
                    IModelComponentRenderer renderer = delegateRenderers.get(i);
                    renderer.render(matrixStack,
                            builder,
                            transformType,
                            light, overlay);
                }
                this.delegateRenderers = new ArrayList<>();
            } finally {
                for (int i = 0; i < cleanedTransforms.size(); i++) { // [26.2, )
                    NodeTransform transform = cleanedTransforms.get(i);
                    transform.apply();
                }
            }
        }
    }

    // --------Deprecated--------

    /*
    原模组不仅整了个隐藏width/height说明的Vec2，还可能因为@Nullable亮了灯所以忘了
     */
    @Deprecated(forRemoval = true) public @Nullable Vec2 getSize() {
        return this.hasSize ? new Vec2(this.width, this.height) : null;
    }

    @Deprecated(forRemoval = true) protected float convertPivot(_Bone bones, int index) {
        return ClientModelUtils.pivot_BEtoJE(this.indexBones, bones, index);
    }
}
