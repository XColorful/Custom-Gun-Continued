/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.xcolorful.customgun.client.animation.listener.camera.CameraAnimationObject;
import dev.xcolorful.customgun.client.animation.screen.RefitScreenTransformState;
import dev.xcolorful.customgun.client.animation.statemachine.GunAnimStateContext;
import dev.xcolorful.customgun.client.animation.statemachine.LuaAnimStateMachine;
import dev.xcolorful.customgun.client.api.animation.statemachine.GunAnimationState;
import dev.xcolorful.customgun.client.api.entity.ILocalShooter;
import dev.xcolorful.customgun.client.api.entity.shooter.ILocalShooterGetter;
import dev.xcolorful.customgun.client.api.event.IComputeCameraAnglesEvent;
import dev.xcolorful.customgun.client.api.event.IRenderHandEvent;
import dev.xcolorful.customgun.client.api.event.render.BeforeRenderHandEvent;
import dev.xcolorful.customgun.client.api.renderer.KeepingItemRenderer;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.model.GunModelObject;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.model.bedrock.SlotModel;
import dev.xcolorful.customgun.client.renderer.item.gun.GunCameraHelper;
import dev.xcolorful.customgun.client.renderer.item.gun.GunRendererAddon;
import dev.xcolorful.customgun.client.renderer.model.MuzzleFlashRender;
import dev.xcolorful.customgun.client.renderer.model.ShellRender;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.client.resource.assets.display._LodDisplay;
import dev.xcolorful.customgun.client.resource.assets.display._ModelTransform;
import dev.xcolorful.customgun.client.resource.assets.display._ModelTransformScale;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.util.ClientRenderDistance;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import dev.xcolorful.customgun.core.util.MathUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

import static net.minecraft.world.item.ItemDisplayContext.*;

/**
 * 负责主要的枪械动画模型渲染
 * <ul>
 *     <li>额外效果见 {@link GunRendererAddon}</li>
 * </ul>
 */
public class GunItemRenderer extends AnimateGeoItemRenderer<GunModelObject, GunAnimStateContext> {
    private static final SlotModel SLOT_GUN_MODEL = new SlotModel();

    public static class State {
        public static final Vector3f muzzleRenderOffset = new Vector3f();
        private static GunModelObject lastGunModelObject;
    }

    public GunItemRenderer() {
        super();
    }

    // --------AnimateGeoItemRenderer--------

    @Override public GunAnimStateContext initContext(ItemStack gunItem, Player player, float partialTick) {
        GunAnimStateContext context = new GunAnimStateContext();
        this.updateContext(context, gunItem, player, partialTick);
        return context;
    }

    @Override public void updateContext(GunAnimStateContext context, ItemStack gunItem, Player player, float partialTick) {
        context.setPartialTicks(partialTick);
        context.setCurrentGunItem(gunItem);
    }

    // ----Getter----

    @Override public @Nullable GunModelObject getModel(ItemStack gunItem) {
        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return null;

        return gunDisplayInstance.getGunModel();
    }
    @Override public @Nullable LuaAnimStateMachine<GunAnimStateContext> getStateMachine(ItemStack gunItem) {
        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return null;

        return gunDisplayInstance.getAnimStateMachine();
    }
    @Override public @Nullable ResourceLocation getTextureLocation(ItemStack gunItem) {
        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return null;

        GunDisplay gunDisplay = gunDisplayInstance.getPojo();
        return gunDisplay.getTextureLocation();
    }


    // --------IAnimateGeoItemRendererState--------


    @Override
    public void tryInit(ItemStack stack, Player player, float partialTick) {
        super.tryInit(stack, player, partialTick);
    }

    @Override
    public void tryExit(ItemStack gunItem, long putAwayTime) {
        @Nullable LuaAnimStateMachine<GunAnimStateContext> stateMachine = getStateMachine(gunItem);
        if (stateMachine == null) return;

        stateMachine.processContextIfExist(context -> {
            context.setPutAwayTime(putAwayTime / 1000F);
            context.setCurrentGunItem(gunItem);
        });

        if (stateMachine.isInitialized()) {
            stateMachine.trigger(GunAnimationState.INPUT_PUT_AWAY.getConstantName());
            KeepingItemRenderer.cgc$getRenderer().cgc$keep(gunItem, putAwayTime);
            stateMachine.exit();
            stateMachine.setExitingTime(putAwayTime + 50);
        }
    }

    @Override
    public long getPutAwayTime(ItemStack gunItem) {
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return 0;

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        return (long) (gunData.getPutAwayTime() * 1000);
    }

    // --------IAnimateGeoItemRendererOperator--------

    @Override
    public void applyLevelCameraAnimation(IComputeCameraAnglesEvent event, ItemStack gunItem, LocalPlayer player) {
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        @Nullable GunModelObject gunModelObject = this.getModel(gunItem);
        if (gunModelObject == null) return;

        if (State.lastGunModelObject != gunModelObject) {
            // 切换枪械模型的时候清理一下摄像机动画数据，以避免上一次播放到一半的摄像机动画影响观感
            gunModelObject.cleanCameraAnimationTransform();
            State.lastGunModelObject = gunModelObject;
        }

        ILocalShooter iLocalShooter = ILocalShooterGetter.fromLocalPlayer(player);

        float partialTicks = ClientRenderUtils.getRenderFrameTime();
        float aimingProgress = iLocalShooter.cgc$getRenderAimingProgress(partialTicks);
        float zoom = iGun.getScopeZoomScale(gunItem);
        float multiplier = 1 - aimingProgress + aimingProgress / (float) Math.sqrt(zoom);

        this.applyLevelCameraAnimation(event, gunItem, multiplier);
    }

    @Override
    public void applyItemInHandCameraAnimation(BeforeRenderHandEvent event, ItemStack gunItem, LocalPlayer player) {
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        @Nullable GunModelObject gunModelObject = this.getModel(gunItem);
        if (gunModelObject == null) return;

        ILocalShooter iLocalShooter = ILocalShooterGetter.fromLocalPlayer(player);

        float partialTicks = ClientRenderUtils.getRenderFrameTime();
        float aimingProgress = iLocalShooter.cgc$getRenderAimingProgress(partialTicks);
        float zoom = iGun.getScopeZoomScale(gunItem);
        float multiplier = 1 - aimingProgress + aimingProgress / (float) Math.sqrt(zoom);
        @NotNull CameraAnimationObject cameraAnimationObject = gunModelObject.getCameraAnimationObject();
        Quaternionf quaternion = MathUtil.Quaternion.multiply(cameraAnimationObject.rotationQuaternion, multiplier);

        PoseStack poseStack = event.getPoseStack();
        poseStack.mulPose(quaternion);

        // TODO 截至目前，摄像机动画数据已消费完毕。是否有更好的清理动画数据的方法？
        // ↑那这是谁设计的东西呢？连个文档都没有
        // 你可是内部开发啊，往日种种，你当真都不记得了？
        // 说什么防辞退编程都没用了
        // 再无话说，请速速动手
        gunModelObject.cleanCameraAnimationTransform();
    }

    @Override
    public void renderFirstPerson(PoseStack poseStack,
                                  IRenderHandEvent event,
                                  ItemDisplayContext ctx,
                                  int light, float partialTick,
                                  LocalPlayer player, ItemStack gunItem) {
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return;

        @Nullable GunModelObject gunModelObject = gunDisplayInstance.getGunModel();
        if (gunModelObject == null) return;

        LuaAnimStateMachine<GunAnimStateContext> animationStateMachine = gunDisplayInstance.getAnimStateMachine();

        // 在渲染之前，先更新动画，让动画数据写入模型
        if (animationStateMachine != null) {
            animationStateMachine.processContextIfExist(context -> {
                updateContext(context, gunItem, player, partialTick);
            });
            animationStateMachine.update();
        }

        // 开启第一人称弹壳和火焰渲染
        MuzzleFlashRender.State.isSelf = true;
        ShellRender.State.isSelf = true;
        poseStack.pushPose(); {
            // 逆转原版施加在手上的延滞效果，改为写入模型动画数据中
            float xRotOffset = Mth.lerp(partialTick, player.xBobO, player.xBob);
            float yRotOffset = Mth.lerp(partialTick, player.yBobO, player.yBob);
            float xRot = player.getViewXRot(partialTick) - xRotOffset;
            float yRot = player.getViewYRot(partialTick) - yRotOffset;
            poseStack.mulPose(Axis.XP.rotationDegrees(xRot * -0.1F));
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot * -0.1F));

            BedrockPart rootNode = gunModelObject.getRootNode();
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

            // 应用持枪姿态变换，如第一人称摄像机定位
            GunRendererAddon.get().applyFirstPersonGunTransform(poseStack, partialTick, gunModelObject, player, gunItem);

            // 如果正在打开改装界面，则取消手臂渲染
            boolean renderHand = gunModelObject.getRenderHand();
            if (RefitScreenTransformState.get().getOpeningProgress() != 0) {
                gunModelObject.setRenderHand(false);
            }

            GunDisplay gunDisplay = gunDisplayInstance.getPojo();
            @Nullable var textureLocation = gunDisplay.getTextureLocation();
            if (textureLocation == null) textureLocation = ClientRenderUtils.getMissingTextureLocation();

            // 调用枪械模型渲染
            RenderType renderType = gunDisplay.getEnableTransparency()
                    ? ClientRenderUtils.RenderType_.entityTranslucent(textureLocation)
                    : ClientRenderUtils.RenderType_.entityCutout(textureLocation);

            try {
                ClientRenderHelper.FirstPersonArmHelper.setFirstPersonArmCollector(event.getMultiBufferSource_SubmitNodeCollector());
                gunModelObject.render(poseStack, ctx, renderType, light, OverlayTexture.NO_OVERLAY, gunItem);
            } finally {
                ClientRenderHelper.FirstPersonArmHelper.setFirstPersonArmCollector(null);
            }

            // 缓存枪口位置，为第一人称曳光弹渲染作准备
            cacheMuzzlePosition(poseStack, gunModelObject);

            // 恢复手臂渲染
            gunModelObject.setRenderHand(renderHand);
        }
        poseStack.popPose();
        // 关闭第一人称弹壳和火焰渲染
        MuzzleFlashRender.State.isSelf = false;
        ShellRender.State.isSelf = false;

        // 渲染完成后，将动画数据从模型中清除，不对其他视角下的模型渲染产生影响
        gunModelObject.cleanAnimationTransform();
    }
    private static void cacheMuzzlePosition(PoseStack poseStack, GunModelObject gunModel) {
        @Nullable List<BedrockPart> muzzleFlashPosPath = gunModel.getMuzzleFlashPosPath();
        if (muzzleFlashPosPath == null) return;

        poseStack.pushPose(); {
            // 计算出枪口相对于摄像机中心的坐标
            for (int i = 0; i < muzzleFlashPosPath.size(); i++) {
                BedrockPart bedrockPart = muzzleFlashPosPath.get(i);
                bedrockPart.translate_rotate_scale(poseStack);
            }

            Matrix4f pose = poseStack.last().pose();
            double levelRenderFov = GunCameraHelper.State.WORLD_FOV_DYNAMICS.get();
            double itemRenderFov = GunCameraHelper.State.ITEM_MODEL_FOV_DYNAMICS.get();

            // 缓存转换后的偏移坐标
            State.muzzleRenderOffset.set(
                    pose.m30(),
                    pose.m31(),
                    pose.m32() * Math.tan(itemRenderFov / 2 * Math.PI / 180) / Math.tan(levelRenderFov / 2 * Math.PI / 180));
        }
        poseStack.popPose();
    }

    // --------IBlockEntityWithoutLevelRenderer--------

    @Override
    public void renderByItem(@NotNull ItemStack gunItem,
                             ItemDisplayContext transformType,
                             @NotNull PoseStack poseStack,
                             @NotNull MultiBufferSource bufferSource,
                             int light, int overlay) {
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance != null) {
            // 先获取 3D 模型，如果为空，统一使用 GUI 渲染
            @Nullable GunModelObject gunModelObject = gunDisplayInstance.getGunModel();
            GunDisplay gunDisplay = gunDisplayInstance.getPojo();
            @Nullable var modelTextureLocation = gunDisplay.getTextureLocation();

            if (
                    // 第一人称就不渲染了，交给别的地方
                    transformType == FIRST_PERSON_LEFT_HAND || transformType == FIRST_PERSON_RIGHT_HAND
                    // 第三人称副手也不渲染了
                    || transformType == THIRD_PERSON_LEFT_HAND
            ) return;

            poseStack.pushPose(); {
                if (transformType == GUI || gunModelObject == null || modelTextureLocation == null) {
                    // GUI 特殊渲染
                    @Nullable var slotTextureLocation = gunDisplay.getSlotTextureLocation();
                    if (slotTextureLocation == null) slotTextureLocation = ClientRenderUtils.getMissingTextureLocation();

                    _renderSlotTexture(poseStack, bufferSource, light, overlay, slotTextureLocation);
                } else {
                    { // 低模替换
                        @Nullable GunModelObject gunModelLod = gunDisplayInstance.getGunModelLod();
                        if (gunModelLod != null // 有低模
                                && ClientRenderDistance.shouldRenderLod(poseStack)) { // 在低模渲染范围
                            @Nullable _LodDisplay lodDisplay = gunDisplay.getLodDisplay();
                            if (lodDisplay != null) {
                                gunModelObject = gunModelLod;
                                modelTextureLocation = lodDisplay.getTextureLocation();
                            }
                        }
                    }
                    if (modelTextureLocation == null) modelTextureLocation = ClientRenderUtils.getMissingTextureLocation();

                    // 移动到模型原点
                    poseStack.translate(0.5, 2, 0.5);

                    // 反转模型
                    poseStack.scale(-1, -1, 1);

                    @Nullable _ModelTransform modelTransform = gunDisplay.getModelTransform();

                    // 应用定位组的变换（位移和旋转，不包括缩放）
                    _applyPositioningTransform(poseStack, transformType, gunModelObject, modelTransform != null ? modelTransform.getScale() : null);

                    // 应用 display 数据中的缩放
                    applyScaleTransform(poseStack, transformType, modelTransform != null ? modelTransform.getScale() : null);

                    // 渲染枪械模型
                    RenderType renderType = ClientRenderUtils.RenderType_.entityCutout(modelTextureLocation);
                    ClientRenderHelper.FirstPersonArmHelper.setFirstPersonArmCollector_(bufferSource);
                    try {
                        gunModelObject.render(poseStack, transformType, renderType, light, overlay, gunItem);
                    } finally {
                        ClientRenderHelper.FirstPersonArmHelper.setFirstPersonArmCollector_(null);
                    }
                }
            }
            poseStack.popPose();
        } else {
            poseStack.pushPose(); {
                // 没有这个 gunLocation，渲染个错误材质提醒别人
                _renderSlotTexture(poseStack, bufferSource, light, overlay, ClientRenderUtils.getMissingTextureLocation());
            }
            poseStack.popPose();
        }
    }

    private static void _applyPositioningTransform(PoseStack poseStack,
                                                   ItemDisplayContext transformType,
                                                   GunModelObject model,
                                                   @Nullable _ModelTransformScale scale) {
        switch (transformType) {
            case FIXED -> _applyPositioningNodeTransform(poseStack, model.getFixedOriginPath(), scale != null ? scale.getFixedScale() : null);
            case GROUND -> _applyPositioningNodeTransform(poseStack, model.getGroundOriginPath(), scale != null ? scale.getGroundScale() : null);
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> _applyPositioningNodeTransform(poseStack, model.getThirdPersonHandOriginPath(), scale != null ? scale.getThirdPersonScale() : null);
        }
    }
    private static void _applyPositioningNodeTransform(PoseStack poseStack,
                                                       @Nullable List<BedrockPart> nodePath,
                                                       float @Nullable [] scale) {
        if (nodePath == null) return;

        if (scale == null) {
            scale = new float[]{1, 1, 1};
        }

        // 应用定位组的反向位移、旋转，使定位组的位置就是渲染中心
        poseStack.translate(0, 1.5, 0);
        for (int i = nodePath.size() - 1; i >= 0; i--) {
            BedrockPart t = nodePath.get(i);
            poseStack.mulPose(Axis.XN.rotation(t.xRot));
            poseStack.mulPose(Axis.YN.rotation(t.yRot));
            poseStack.mulPose(Axis.ZN.rotation(t.zRot));
            if (t.getParent() != null) {
                poseStack.translate(-t.x * scale[0] / 16.0F, -t.y * scale[1] / 16.0F, -t.z * scale[2] / 16.0F);
            } else {
                poseStack.translate(-t.x * scale[0] / 16.0F, (1.5F - t.y / 16.0F) * scale[1], -t.z * scale[2] / 16.0F);
            }
        }
        poseStack.translate(0, -1.5, 0);
    }

    private static void applyScaleTransform(PoseStack poseStack,
                                            ItemDisplayContext transformType,
                                            @Nullable _ModelTransformScale scale) {
        if (scale == null) return;

        float[] vector3f = null;
        switch (transformType) {
            case FIXED -> vector3f = scale.getFixedScale();
            case GROUND -> vector3f = scale.getGroundScale();
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> vector3f = scale.getThirdPersonScale();
        }
        if (vector3f != null) {
            poseStack.translate(0, 1.5, 0);
            poseStack.scale(vector3f[0], vector3f[1], vector3f[2]);
            poseStack.translate(0, -1.5, 0);
        }
    }

    private static void _renderSlotTexture(PoseStack poseStack,
                                           MultiBufferSource bufferSource,
                                           int packedLight, int packedOverlay,
                                           ResourceLocation texture) {
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));

        {
            VertexConsumer buffer = bufferSource.getBuffer(ClientRenderUtils.RenderType_.entityTranslucent(texture));
            SLOT_GUN_MODEL.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
