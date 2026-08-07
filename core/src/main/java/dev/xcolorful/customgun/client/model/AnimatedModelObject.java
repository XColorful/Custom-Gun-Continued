/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model;

import dev.xcolorful.customgun.client.animation.listener.camera.CameraAnimationObject;
import dev.xcolorful.customgun.client.animation.listener.constraint.ConstraintObject;
import dev.xcolorful.customgun.client.animation.listener.model.ModelRotateListener;
import dev.xcolorful.customgun.client.animation.listener.model.ModelScaleListener;
import dev.xcolorful.customgun.client.animation.listener.model.ModelTranslateListener;
import dev.xcolorful.customgun.client.api.animation.ObjectAnimationChannel;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListener;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListenerSupplier;
import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockRenderer;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.model.bedrock.FunctionalBedrockPart;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock._GeometryModel;
import dev.xcolorful.customgun.client.resource.assets.model.bedrock.geometry._Bone;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry.NodeName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.List;
import java.util.function.Function;

public class AnimatedModelObject extends ModelObject implements IAnimationListenerSupplier {

    private final @NotNull CameraAnimationObject cameraAnimationObject = new CameraAnimationObject();

    /**
     * 动画约束组的路径
     */
    protected @Nullable List<BedrockPart> constraintPath;
    private @Nullable ConstraintObject constraintObject;

    /**
     * 根组
     */
    protected @Nullable BedrockPart root;
    /**
     * 第一人称idle状态摄像机定位组的路径
     */
    protected @Nullable List<BedrockPart> idleSightPath;

    AnimatedModelObject(@NotNull BedrockModel pojo) {
        super(pojo);
    }

    @Override public boolean resetCache() {
        if (!super.resetCache()) return false;

        { // 初始化相机动画对象
            IBedrockRenderer cameraRenderer = this.modelMap_get(NodeName.CAMERA.getName());
            if (cameraRenderer != null) {
                this.cameraAnimationObject.cameraRenderer = cameraRenderer;
            }
        }

        { // 初始化动画约束对象
            this.constraintPath = this.getPath(this.modelMap_get(NodeName.CONSTRAINT.getName()));
            if (this.constraintPath != null) {
                this.constraintObject = new ConstraintObject();
                BedrockPart constraintNode = this.constraintPath.get(this.constraintPath.size() - 1);
                if (this.shouldRender.contains(constraintNode)) {
                    this.constraintObject.bone = this.indexBones.get(NodeName.CONSTRAINT.getName());
                } else {
                    this.constraintObject.node = constraintNode;
                }
            }
        }

        {
            IBedrockRenderer renderer = this.modelMap_get(NodeName.ROOT.getName());
            this.root = renderer != null ? renderer.getModelRenderer() : null;
            this.idleSightPath = this.getPath(this.modelMap_get(NodeName.IDLE_VIEW.getName()));
        }

        return true;
    }

    @Override
    protected void loadNewModel(BedrockModel pojo) {
        {
            List<_GeometryModel> geometryModels = pojo.getGeometryModels();
            assert !geometryModels.isEmpty();
            _GeometryModel geometryModel = geometryModels.get(0);
            geometryModel.deco();

            if (geometryModel.getBones() == null) return;

            List<_Bone> bones = geometryModel.getBones();
            for (int i = 0; i < bones.size(); i++) {
                _Bone bone = bones.get(i);
                FunctionalBedrockPart bedrockPart = new FunctionalBedrockPart(bone.getName(), null);
                this.modelMap_putIfAbsent(bone.getName(), IBedrockRenderer.of(bedrockPart));
            }
        }

        super.loadNewModel(pojo);
    }
    @Override
    protected void loadLegacyModel(BedrockModel pojo) {
        {
        }

        super.loadLegacyModel(pojo);
    }

    public void cleanAnimationTransform() {
        for (IBedrockRenderer renderer : this.modelMap_values()) {
            renderer.setOffsetX(0);
            renderer.setOffsetY(0);
            renderer.setOffsetZ(0);
            renderer.getAdditionalQuaternion().set(0, 0, 0, 1);
            renderer.setScaleX(1);
            renderer.setScaleY(1);
            renderer.setScaleZ(1);
        }

        if (this.constraintObject != null) {
            this.constraintObject.rotationConstraint.set(0, 0, 0);
            this.constraintObject.translationConstraint.set(0, 0, 0);
        }
    }

    public void cleanCameraAnimationTransform() {
        this.cameraAnimationObject.rotationQuaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
    }

    // --------Getter--------

    public @Nullable List<BedrockPart> getConstraintPath() {
        return this.constraintPath;
    }
    public @NotNull CameraAnimationObject getCameraAnimationObject() {
        return this.cameraAnimationObject;
    }
    public @Nullable ConstraintObject getConstraintObject() {
        return this.constraintObject;
    }
    public boolean getRenderHand() {
        return true;
    }
    public BedrockPart getRootNode() {
        return this.root;
    }
    public @Nullable List<BedrockPart> getIdleSightPath() {
        return this.idleSightPath;
    }

    // --------Setter--------

    /**
     * @param nodeName 想要进行编程渲染流程的 node 名称
     * @param function 输入为 BedrockPart，返回 IModelRenderer 以替换渲染
     */
    public void setFunctionalRenderer(String nodeName, Function<BedrockPart, IModelComponentRenderer> function) {
        IBedrockRenderer renderer = this.modelMap_get(nodeName);
        if (renderer == null) {
            FunctionalBedrockPart functionalPart = new FunctionalBedrockPart(nodeName, function);
            this.modelMap_put(nodeName, IBedrockRenderer.of(functionalPart));
        } else if (renderer.getModelRenderer() instanceof FunctionalBedrockPart functionalPart) {
            functionalPart.functionalRenderer = function;
        }
    }

    // --------IAnimationListenerSupplier--------

    @Override
    public @Nullable IAnimationListener supplyListeners(String nodeName, ObjectAnimationChannel.ChannelType type) {
        IBedrockRenderer renderer = this.modelMap_get(nodeName);
        if (renderer == null) return null;

        IAnimationListener cameraListener = this.cameraAnimationObject.supplyListeners(nodeName, type);
        if (cameraListener != null) return cameraListener;

        if (this.constraintObject != null) {
            IAnimationListener constraintListener = this.constraintObject.supplyListeners(nodeName, type);
            if (constraintListener != null) return constraintListener;
        }

        return switch (type) {
            case TRANSLATION -> new ModelTranslateListener(this, renderer, nodeName);
            case ROTATION -> new ModelRotateListener(renderer);
            case SCALE -> new ModelScaleListener(renderer);
            // 增加类型使此处强制编译不通过
        };
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true) public static final String CAMERA_NODE_NAME = NodeName.CAMERA.getName();
    @Deprecated(forRemoval = true) public static final String CONSTRAINT_NODE = NodeName.CONSTRAINT.getName();
}
