/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model.bedrock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockCube;
import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockCubeCompile;
import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockRenderer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.Random;

public class BedrockPart implements IBedrockCubeCompile, IBedrockRenderer {

    public final @Nullable String name;
    public final ObjectList<IBedrockCube> cubes = new ObjectArrayList<>();
    public final ObjectList<BedrockPart> children = new ObjectArrayList<>();

    public BedrockPart(@Nullable String name) {
        this.name = name;
    }

    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public boolean visible = true;
    public boolean illuminated = false;
    public boolean mirror;
    /**
     * 通常用于动画旋转
     */
    public Quaternionf additionalQuaternion = new Quaternionf(0, 0, 0, 1);
    public float xScale = 1;
    public float yScale = 1;
    public float zScale = 1;

    protected BedrockPart parent;

    private float initRotX;
    private float initRotY;
    private float initRotZ;

    public void translate_rotate_scale(PoseStack poseStack) {
        poseStack.translate(this.offsetX, this.offsetY, this.offsetZ);
        poseStack.translate((this.x / 16.0F), (this.y / 16.0F), (this.z / 16.0F));
        if (this.zRot != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotation(this.zRot));
        }
        if (this.yRot != 0.0F) {
            poseStack.mulPose(Axis.YP.rotation(this.yRot));
        }
        if (this.xRot != 0.0F) {
            poseStack.mulPose(Axis.XP.rotation(this.xRot));
        }
        poseStack.mulPose(additionalQuaternion);
        poseStack.scale(xScale, yScale, zScale);
    }

    // --------Getter--------

    public boolean isEmpty() {
        return this.cubes.isEmpty();
    }

    public BedrockPart getParent() {
        return this.parent;
    }

    // --------Setter--------

    public void setPos(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public void setInitRotationAngle(float x, float y, float z) {
        this.initRotX = x;
        this.initRotY = y;
        this.initRotZ = z;
    }

    @ApiStatus.Internal
    public void setParent(BedrockPart parent) {
        this.parent = parent;
    }

    public void addChild(BedrockPart child) {
        this.children.add(child);
    }

    // --------IBedrockCubeCompile--------

    @Override
    public void compile(PoseStack.Pose pose,
                        VertexConsumer consumer,
                        int light, int overlay,
                        float red, float green, float blue, float alpha) {
        for (int i = 0; i < this.cubes.size(); i++) {
            this.cubes.get(i)
                    .compile(pose,
                            consumer,
                            light, overlay,
                            red, green, blue, alpha);
        }
    }

    // --------IBedrockRenderer--------

    @Override
    public BedrockPart getModelRenderer() {
        return this;
    }

    // ----Getter & Setter----

    @Override public float getRotateAngleX() {
        return this.xRot;
    }
    @Override public float getRotateAngleY() {
        return this.yRot;
    }
    @Override public float getRotateAngleZ() {
        return this.zRot;
    }
    @Override public float getOffsetX() {
        return this.offsetX;
    }
    @Override public float getOffsetY() {
        return this.offsetY;
    }
    @Override public float getOffsetZ() {
        return this.offsetZ;
    }
    @Override public float getRotationPointX() {
        return this.x;
    }
    @Override public float getRotationPointY() {
        return this.y;
    }
    @Override public float getRotationPointZ() {
        return this.z;
    }
    @Override public boolean isVisible() {
        return this.visible;
    }
    @Override public float getInitRotateAngleX() {
        return this.initRotX;
    }
    @Override public float getInitRotateAngleY() {
        return this.initRotY;
    }
    @Override public float getInitRotateAngleZ() {
        return this.initRotZ;
    }
    @Override public Quaternionf getAdditionalQuaternion() {
        return this.additionalQuaternion;
    }
    @Override public float getScaleX() {
        return this.xScale;
    }
    @Override public float getScaleY() {
        return this.yScale;
    }
    @Override public float getScaleZ() {
        return this.zScale;
    }

    @Override public void setRotateAngleX(float xRot) {
        this.xRot = xRot;
    }
    @Override public void setRotateAngleY(float yRot) {
        this.yRot = yRot;
    }
    @Override public void setRotateAngleZ(float zRot) {
        this.zRot = zRot;
    }
    @Override public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }
    @Override public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }
    @Override public void setOffsetZ(float offsetZ) {
        this.offsetZ = offsetZ;
    }
    @Override public void setVisible(boolean visible) {
        this.visible = visible;
    }
    @Override public void setAdditionalQuaternion(Quaternionf quaternion) {
        this.additionalQuaternion = quaternion;
    }
    @Override public void setScaleX(float scaleX) {
        this.xScale = scaleX;
    }
    @Override public void setScaleY(float scaleY) {
        this.yScale = scaleY;
    }
    @Override public void setScaleZ(float scaleZ) {
        this.zScale = scaleZ;
    }

    // --------IBedrockRender--------

    @Override
    public void render(PoseStack poseStack,
                       ItemDisplayContext transformType,
                       VertexConsumer consumer,
                       int light, int overlay,
                       float red, float green, float blue, float alpha) {
        int cubePackedLight = this.illuminated ? LightTexture.pack(15, 15) // 最大亮度
                : light;

        if (this.visible) {
            if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
                poseStack.pushPose(); {
                    this.translate_rotate_scale(poseStack);
                    this.compile(poseStack.last(), consumer, cubePackedLight, overlay, red, green, blue, alpha);

                    for (int i = 0; i < this.children.size(); i++) {
                        this.children.get(i)
                                .render(poseStack,
                                        transformType,
                                        consumer,
                                        cubePackedLight, overlay,
                                        red, green, blue, alpha);
                    }
                }
                poseStack.popPose();
            }
        }
    }

    // --------Deprecated--------

    /**
     * 疑似废弃功能
     */
    @Deprecated(forRemoval = false) public IBedrockCube getRandomCube(Random random) {
        return this.cubes.get(random.nextInt(this.cubes.size()));
    }

    @Deprecated public float getInitRotX() {
        return this.getInitRotateAngleX();
    }
    @Deprecated public float getInitRotY() {
        return this.getInitRotateAngleY();
    }
    @Deprecated public float getInitRotZ() {
        return this.getInitRotateAngleZ();
    }
    @Deprecated(forRemoval = true) public void translateAndRotateAndScale(PoseStack poseStack) {
        this.translate_rotate_scale(poseStack);
    }
}
