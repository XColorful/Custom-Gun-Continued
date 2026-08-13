/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xcolorful.customgun.client.animation.statemachine.ItemAnimStateContext;
import dev.xcolorful.customgun.client.animation.statemachine.LuaAnimStateMachine;
import dev.xcolorful.customgun.client.api.animation.statemachine.GunAnimationState;
import dev.xcolorful.customgun.client.api.event.IComputeCameraAnglesEvent;
import dev.xcolorful.customgun.client.api.event.IRenderHandEvent;
import dev.xcolorful.customgun.client.api.event.render.BeforeRenderHandEvent;
import dev.xcolorful.customgun.client.api.renderer.item.IAnimateGeoItemRenderer;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.api.sound.gun.GunSoundType;
import dev.xcolorful.customgun.client.compat.minecraft.BlockEntityWithoutLevelRenderer;
import dev.xcolorful.customgun.client.config.SoundConfig;
import dev.xcolorful.customgun.client.model.AnimatedModelObject;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.sound.SoundPlayManager;
import dev.xcolorful.customgun.core.api.item.IAnimationItem;
import dev.xcolorful.customgun.core.config.GunConfig;
import dev.xcolorful.customgun.core.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;

/**
 * 抽象的基岩版动画物品模型BEWLR，包含一些默认实现
 * @param <M> 基岩版模型
 * @param <CTX> 动画状态机上下文
 */
public abstract class AnimateGeoItemRenderer<M extends AnimatedModelObject, CTX extends ItemAnimStateContext>
        extends BlockEntityWithoutLevelRenderer
        implements IAnimateGeoItemRenderer<M, CTX> {

    public ResourceLocation textureLocation;

    protected @Nullable LuaAnimStateMachine<CTX> stateMachine;
    protected M modelObject;

    public AnimateGeoItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public RenderType getRenderType(ItemStack itemStack) {
        return RenderType.entityCutout(this.getTextureLocation(itemStack));
    }

    /**
     * 执行额外的变换
     */
    public void doExtraTransforms(PoseStack poseStack, M modelObject, ItemStack itemStack) {
        applyFirstPersonPositioningTransform(poseStack, modelObject, itemStack);
    }

    public static void applyFirstPersonPositioningTransform(PoseStack poseStack, AnimatedModelObject modelObject, ItemStack itemStack) {
        Matrix4f transformMatrix = new Matrix4f();
        transformMatrix.identity();

        // 应用瞄准定位
        List<BedrockPart> idleNodePath = modelObject.getIdleSightPath();

        Matrix4f idleViewMatrix = getPositioningNodeInverse(idleNodePath);

        // 应用瞄准变换
        MathUtil.applyMatrixLerp(transformMatrix, idleViewMatrix, transformMatrix, 1);

        // 应用变换到 PoseStack
        poseStack.translate(0, 1.5f, 0);
        poseStack.mulPose(transformMatrix);
        poseStack.translate(0, -1.5f, 0);
    }

    /**
     * 获取摄像机定位组的反相矩阵
     */
    public static @NotNull Matrix4f getPositioningNodeInverse(List<BedrockPart> nodePath) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.identity();
        if (nodePath == null) return matrix4f;

        for (int i = nodePath.size() - 1; i >= 0; i--) {
            BedrockPart part = nodePath.get(i);
            // 计算反向的旋转
            matrix4f.rotate(Axis.XN.rotation(part.xRot));
            matrix4f.rotate(Axis.YN.rotation(part.yRot));
            matrix4f.rotate(Axis.ZN.rotation(part.zRot));
            // 计算反向的位移
            if (part.getParent() != null) {
                matrix4f.translate(-part.x / 16.0F, -part.y / 16.0F, -part.z / 16.0F);
            } else {
                matrix4f.translate(-part.x / 16.0F, (1.5F - part.y / 16.0F), -part.z / 16.0F);
            }
        }

        return matrix4f;
    }

    // --------AnimateGeoItemRenderer--------

    // ----Getter----

    public @Nullable M getModel(ItemStack pojoItem) {
        return this.modelObject;
    }
    public @Nullable LuaAnimStateMachine<CTX> getStateMachine(ItemStack itemStack) {
        return this.stateMachine;
    }
    public ResourceLocation getTextureLocation(ItemStack itemStack) {
        return this.textureLocation;
    }
    /**
     * 计算并返回切出动画的时长，单位ms
     * @return 保持时间
     */
    public long getPutAwayTime(ItemStack stack) {
        return 0;
    }

    // ----Setter----

    public void setModel(M modelObject) {
        this.modelObject = modelObject;
    }

    // --------IAnimateGeoItemRendererState--------

    @Override
    public void tryInit(ItemStack itemStack, Player player, float partialTicks) {
        @Nullable LuaAnimStateMachine<CTX> stateMachine = this.getStateMachine(itemStack);
        if (stateMachine == null) return;

        if (stateMachine.isInitialized()) {
            stateMachine.exit();
        }

        stateMachine.setContext(this.initContext(itemStack, player, partialTicks));
        stateMachine.initialize();

        stateMachine.trigger(GunAnimationState.INPUT_DRAW.getConstantName());
    }

    @Override
    public void tryExit(ItemStack itemStack, long putAwayTime) {
        @Nullable LuaAnimStateMachine<CTX> stateMachine = this.getStateMachine(itemStack);
        if (stateMachine == null) return;

        stateMachine.processContextIfExist(context -> {
            context.setPutAwayTime(putAwayTime / 1000f);
        });

        if (!stateMachine.isInitialized()) return;

//        KeepingItemRenderer.cgc$getRenderer().cgc$keep(itemStack, putAwayTime);
        stateMachine.exit();
        // 需要设置的比动画稍长些，避免意外的重初始化（可能是丢精度了）
        // 延后1tick应该基本没有感知
        stateMachine.setExitingTime(putAwayTime + 50);
    }

    public void triggerAnimation(ItemStack itemStack, String input) {
        @Nullable LuaAnimStateMachine<CTX> stateMachine = this.getStateMachine(itemStack);
        if (stateMachine == null) return;

        stateMachine.trigger(input);
    }

    @Override
    public boolean needReInit(ItemStack itemStack) {
        @Nullable LuaAnimStateMachine<CTX> stateMachine = this.getStateMachine(itemStack);
        if (stateMachine == null) return false;

        return !stateMachine.isInitialized() && stateMachine.getExitingTime() < System.currentTimeMillis();
    }

    @Override
    public void visualUpdate(ItemStack itemStack) {
        @Nullable LuaAnimStateMachine<CTX> stateMachine = this.getStateMachine(itemStack);
        if (stateMachine == null) return;

        stateMachine.visualUpdate();
    }

    // --------IAnimateGeoItemRendererOperator--------

    @Override
    public void applyLevelCameraAnimation(IComputeCameraAnglesEvent event, ItemStack pojoItem, LocalPlayer player) {
        this.applyLevelCameraAnimation(event, pojoItem, 1);
    }
    public void applyLevelCameraAnimation(IComputeCameraAnglesEvent event, ItemStack stack, float multiplier) {
        @Nullable M modelObject = this.getModel(stack);
        if (modelObject == null) return;

        Quaternionf q = MathUtil.Quaternion.multiply(modelObject.getCameraAnimationObject().rotationQuaternion, multiplier);
        double yaw = Math.asin(2 * (q.w() * q.y() - q.x() * q.z()));
        double pitch = Math.atan2(2 * (q.w() * q.x() + q.y() * q.z()), 1 - 2 * (q.x() * q.x() + q.y() * q.y()));
        double roll = Math.atan2(2 * (q.w() * q.z() + q.x() * q.y()), 1 - 2 * (q.y() * q.y() + q.z() * q.z()));
        yaw = Math.toDegrees(yaw);
        pitch = Math.toDegrees(pitch);
        roll = Math.toDegrees(roll);
        event.setYaw((float) yaw + event.getYaw());
        event.setPitch((float) pitch + event.getPitch());
        event.setRoll((float) roll + event.getRoll());
    }

    @Override
    public void applyItemInHandCameraAnimation(BeforeRenderHandEvent event, ItemStack pojoItem, LocalPlayer player) {
        applyItemInHandCameraAnimation(event, pojoItem, 1);
    }
    public void applyItemInHandCameraAnimation(BeforeRenderHandEvent event, ItemStack pojoItem, float multiplier) {
        @Nullable M modelObject = this.getModel(pojoItem);
        if (modelObject == null) return;

        Quaternionf quaternion = MathUtil.Quaternion.multiply(modelObject.getCameraAnimationObject().rotationQuaternion, multiplier);
        PoseStack poseStack = event.getPoseStack();
        poseStack.mulPose(quaternion);
    }

    /**
     * 渲染第一人称，暂时只用于玩家
     */
    @Override
    public void renderFirstPerson(PoseStack poseStack,
                                  IRenderHandEvent event,
                                  ItemDisplayContext ctx,
                                  int light, float partialTick,
                                  LocalPlayer player, ItemStack pojoItem) {
        @Nullable M modelObject = this.getModel(pojoItem);
        if (modelObject == null) return;

        poseStack.pushPose(); {
            float xRotOffset = Mth.lerp(partialTick, player.xBobO, player.xBob);
            float yRotOffset = Mth.lerp(partialTick, player.yBobO, player.yBob);
            float xRot = player.getViewXRot(partialTick) - xRotOffset;
            float yRot = player.getViewYRot(partialTick) - yRotOffset;
            poseStack.mulPose(Axis.XP.rotationDegrees(xRot * -0.1F));
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot * -0.1F));

            BedrockPart rootNode = modelObject.getRootNode();
            if (rootNode != null) {
                xRot = (float) Math.tanh(xRot / 25) * 25;
                yRot = (float) Math.tanh(yRot / 25) * 25;
                rootNode.offsetX += yRot * 0.1F / 16F / 3F;
                rootNode.offsetY += -xRot * 0.1F / 16F / 3F;
                rootNode.additionalQuaternion.mul(Axis.XP.rotationDegrees(xRot * 0.05F));
                rootNode.additionalQuaternion.mul(Axis.YP.rotationDegrees(yRot * 0.05F));
            }

            // 从渲染原点 (0, 24, 0) 移动到模型原点 (0, 0, 0)
            poseStack.translate(0, 1.5f, 0);

            // 基岩版模型是上下颠倒的，需要翻转过来
            poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
            doExtraTransforms(poseStack, modelObject, pojoItem);

            var stateMachine = this.getStateMachine(pojoItem);
            if (stateMachine != null) {
                stateMachine.processContextIfExist(context -> {
                    updateContext(context, pojoItem, player, partialTick);
                });
                stateMachine.update();
            }

            modelObject.render(poseStack, ctx, getRenderType(pojoItem), light, OverlayTexture.NO_OVERLAY);

            // 渲染结束后清除动画变换
            modelObject.cleanAnimationTransform();
        }
        poseStack.popPose();
    }

    // --------IBlockEntityWithoutLevelRenderer--------

    @Override
    public void renderByItem(@NotNull ItemStack pojoItem,
                             ItemDisplayContext ctx,
                             @NotNull PoseStack poseStack,
                             @NotNull MultiBufferSource bufferSource,
                             int light, int overlay) {
        if (ctx.firstPerson()) return;

        @Nullable M modelObject = this.getModel(pojoItem);
        if (modelObject == null) return;

        poseStack.pushPose(); {
            // 从渲染原点 (0, 24, 0) 移动到模型原点 (0, 0, 0)
            poseStack.translate(0.5, 1.5f, 0.5);
            // 基岩版模型是上下颠倒的，需要翻转过来。
            poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
            modelObject.render(poseStack, ctx, RenderType.entityCutout(
                    getTextureLocation(pojoItem)
            ), light, overlay);
        }
        poseStack.popPose();
    }

    // --------IFPGeoItemRenderer--------

//    @Override
    public long getPutAwayDuration(ItemStack stack) {
        return this.getPutAwayTime(stack);
    }

//    @Override
    public @Nullable Object createAnimationInstance(ItemStack itemStack, Entity entity) {
        return new Object() {
            private boolean drawn = false;
            private ItemStack lastItem = itemStack;

//            @Override
            public ItemStack currentItem() {
                return this.lastItem;
            }

//            @Override
            public Object getPose() {
//                return DummyPose.INSTANCE;
                return null;
            }

//            @Override
            public void tick(float v) {
            }

//            @Override
            public @NotNull Quaternionf getCameraRoration() {
                return new Quaternionf();
            }

//            @Override
            public void setCameraRotation(@NotNull Quaternionf quaternionf) {
            }

//            @Override
            public Object getCachedPose() {
//                return DummyPose.INSTANCE;
                return null;
            }

//            @Override
            public void updateItem(ItemStack itemStack) {
                this.lastItem = itemStack;
            }

//            @Override
            public void triggerDraw() {
                if (this.drawn) return;

                this.drawn = true;
                Minecraft mc = Minecraft.getInstance();
                tryInit(this.lastItem, mc.player, 0);
                if (mc.player == null) return;

                @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(this.lastItem);
                if (gunDisplayInstance == null) return;

                SoundPlayManager.get().stopCurrentSound();
                SoundPlayManager.get().playClientSound(gunDisplayInstance.getGunSound(GunSoundType.DRAW_SOUND),
                        1.0f, 1.0f,
                        mc.player, false,
                        GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(),
                        true, SoundConfig.DEFAULT_SOUND_CONCURRENCY_LIMIT.get());
            }

            public void triggerPutAway() {
                tryExit(this.lastItem, getPutAwayTime(this.lastItem));
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) return;

                @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(this.lastItem);
                if (gunDisplayInstance == null) return;

                SoundPlayManager.get().stopCurrentSound();
                SoundPlayManager.get().playClientSound(gunDisplayInstance.getGunSound(GunSoundType.PUT_AWAY_SOUND),
                        1.0f, 1.0f,
                        mc.player, false,
                        GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(),
                        true, SoundConfig.DEFAULT_SOUND_CONCURRENCY_LIMIT.get());
            }
        };
    }

//    @Override
    public boolean isSameItem(ItemStack oldStack, ItemStack newStack) {
        // 疑似屎山
        if (oldStack.getItem() instanceof IAnimationItem animationItem) {
            return !animationItem.switchItemNeedReset(oldStack, newStack);
        }
        return ItemStack.matches(oldStack, newStack);
    }

//    @Override
    public boolean blockOffhandRender() {
        return true;
    }
}
