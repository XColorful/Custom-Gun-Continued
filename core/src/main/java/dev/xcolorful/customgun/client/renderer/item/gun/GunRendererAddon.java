/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.item.gun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xcolorful.customgun.client.animation.screen.RefitScreenTransformState;
import dev.xcolorful.customgun.client.api.entity.shooter.ILocalShooterGetter;
import dev.xcolorful.customgun.client.api.event.render.ItemInHandBobEvent;
import dev.xcolorful.customgun.client.api.renderer.KeepingItemRenderer;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.model.AttachmentModelObject;
import dev.xcolorful.customgun.client.model.GunModelObject;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.renderer.item.GunItemRenderer;
import dev.xcolorful.customgun.client.renderer.model.MuzzleFlashRender;
import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import dev.xcolorful.customgun.core.api.event.CustomEventType;
import dev.xcolorful.customgun.core.api.event.ICustomEvent;
import dev.xcolorful.customgun.core.api.event.ICustomEventHandler;
import dev.xcolorful.customgun.core.api.event.gun.GunFireEvent;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentNBTAccessor;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.entity.projectile.GunProjectile;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterAim;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import dev.xcolorful.customgun.core.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * 负责第一人称的枪械模型额外效果的渲染
 * <ul>
 *     <li>主体部分见 {@link GunItemRenderer}</li>
 *     <li>暂时想不出比{@link GunRendererAddon}更合适的名字</li>
 * </ul>
 */
public class GunRendererAddon implements ICustomEventHandler {
    private static class GunRendererAddonHolder {
        private static final GunRendererAddon INSTANCE = new GunRendererAddon();
    }
    public static GunRendererAddon get() {
        return GunRendererAddonHolder.INSTANCE;
    }
    protected GunRendererAddon() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(CustomEventType eventType, ICustomEvent event) {
        switch (eventType) {
            case GUN_FIRE_EVENT -> {
                onGunFire((GunFireEvent) event);
            }
            case ITEM_IN_HAND_BOB_VIEW_EVENT -> {
                onItemInHandBobView((ItemInHandBobEvent.View) event);
            }
            default -> {
                onReceiveWrongEvent(eventType);
            }
        }
    }
    
    // 用于生成瞄准动作的运动曲线，使动作看起来更平滑
    private static final MathUtil.SecondOrderDynamics AIMING_DYNAMICS = new MathUtil.SecondOrderDynamics(1.2f, 1.2f, 0.5f, 0);
    private static MathUtil.SecondOrderDynamics SWITCH_VIEW_DYNAMICS;

    // 用于打开改装界面时枪械运动的平滑
    private static final MathUtil.SecondOrderDynamics REFIT_OPENING_DYNAMICS = new MathUtil.SecondOrderDynamics(1f, 1.2f, 0.5f, 0);

    // 用于跳跃延滞动画的平滑
    private static final MathUtil.SecondOrderDynamics JUMPING_DYNAMICS = new MathUtil.SecondOrderDynamics(0.28f, 1f, 0.65f, 0);
    private static final float JUMPING_Y_SWAY = -2f;
    private static final float JUMPING_SWAY_TIME = 0.3f;
    private static final float LANDING_SWAY_TIME = 0.15f;

    // 用于枪械后座的程序动画
    private static final MathUtil.SmoothRandomNoise SHOOT_X_SWAY_NOISE = new MathUtil.SmoothRandomNoise(-0.2f, 0.2f, 400);
    private static final MathUtil.SmoothRandomNoise SHOOT_Y_ROTATION_NOISE = new MathUtil.SmoothRandomNoise(-0.0136f, 0.0136f, 100);
    private static final float SHOOT_Y_SWAY = -0.1f;
    private static final float SHOOT_ANIMATION_TIME = 0.3f;

    private float jumpingSwayProgress = 0;
    private boolean lastOnGround = false;
    private long jumpingTimeStamp = -1;
    private long lastGunFireTimestamp = -1;
    private Matrix4f oldAimingViewMatrix;
    private float oldScopeViewIndex;
    private int currentScopeViewIndex = -1;

    private void onGunFire(GunFireEvent event) {
        if (event.getLogicalSide().isServer()) return;

        @Nullable LivingEntity livingShooter = event.getLivingShooter();
        if (livingShooter == null) return;
        
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (!livingShooter.equals(localPlayer)) return;
        
        @Nullable IGun iGun = event.getIGun();
        if (iGun == null) return;
        
        ItemStack gunItem = event.getGunItem();
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return;

        // 记录开火时间戳，用于后坐力程序动画
        long currentTimeMillis = System.currentTimeMillis();
        this.lastGunFireTimestamp = currentTimeMillis;

        // 记录枪口火焰数据
        MuzzleFlashRender.onShoot(currentTimeMillis);
    }

    /**
     * 当主手拿着枪械物品的时候，取消应用在它上面的 viewBobbing，以便应用自定义的跑步/走路动画
     */
    private void onItemInHandBobView(ItemInHandBobEvent.View event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack gunItem = KeepingItemRenderer.cgc$getRenderer().cgc$getCurrentItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        event.setCanceled(true);
    }

    public void applyFirstPersonGunTransform(PoseStack poseStack, float partialTicks, GunModelObject gunModelObject, LocalPlayer player, ItemStack gunItem) {
        // 配合运动曲线，计算改装枪口的打开进度
        float refitScreenOpeningProgress = REFIT_OPENING_DYNAMICS.update_tick_get(RefitScreenTransformState.get().getOpeningProgress());

        // 配合运动曲线，计算瞄准进度
        float aimingProgress = AIMING_DYNAMICS.update_tick_get(ILocalShooterGetter.fromLocalPlayer(player).cgc$getRenderAimingProgress(partialTicks));

        { // 应用枪械动态，如后坐力、持枪跳跃等
            _applyShootSwayAndRotation(gunModelObject, aimingProgress);
            _applyJumpingSway(gunModelObject, partialTicks);
        }

        // 应用各种摄像机定位组的变换（默认持枪、瞄准、改装界面等）
        _applyFirstPersonPositioningTransform(poseStack, gunModelObject, gunItem, aimingProgress, refitScreenOpeningProgress);

        // 应用动画约束变换
        _applyAnimationConstraintTransform(poseStack, gunModelObject, aimingProgress * (1 - refitScreenOpeningProgress));
    }
    private void _applyShootSwayAndRotation(GunModelObject gunModelObject, float aimingProgress) {
        @Nullable BedrockPart rootNode = gunModelObject.getRootNode();
        if (rootNode == null) return;

        long currentTimeMillis = System.currentTimeMillis();
        float progress = 1 - (currentTimeMillis - lastGunFireTimestamp) / (SHOOT_ANIMATION_TIME * 1000);
        if (progress < 0) {
            progress = 0;
        }
        progress = (float) MathUtil.Easing.easeOutCubic(progress);
        rootNode.offsetX += SHOOT_X_SWAY_NOISE.update_tick_get(currentTimeMillis) / 16 * progress * (1 - aimingProgress);

        // 基岩版模型 y 轴上下颠倒，sway 值取相反数
        rootNode.offsetY += -SHOOT_Y_SWAY / 16 * progress * (1 - aimingProgress);
        rootNode.additionalQuaternion.mul(Axis.YP.rotation(SHOOT_Y_ROTATION_NOISE.update_tick_get(currentTimeMillis) * progress));
    }
    private void _applyJumpingSway(GunModelObject model, float partialTicks) {
        long currentTimeMillis = System.currentTimeMillis();

        // 初始化
        if (jumpingTimeStamp == -1) jumpingTimeStamp = currentTimeMillis;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            double posY = Mth.lerp(partialTicks, Minecraft.getInstance().player.yOld, Minecraft.getInstance().player.getY());
            float velocityY = (float) (posY - Minecraft.getInstance().player.yOld) / partialTicks;
            if (player.onGround()) {
                if (!lastOnGround) {
                    jumpingSwayProgress = velocityY / -0.1f;
                    if (jumpingSwayProgress > 1) {
                        jumpingSwayProgress = 1;
                    }
                    lastOnGround = true;
                } else {
                    jumpingSwayProgress -= (currentTimeMillis - jumpingTimeStamp) / (LANDING_SWAY_TIME * 1000);
                    if (jumpingSwayProgress < 0) {
                        jumpingSwayProgress = 0;
                    }
                }
            } else {
                if (lastOnGround) {
                    // 0.42 是玩家自然起跳的速度
                    jumpingSwayProgress = velocityY / 0.42f;
                    if (jumpingSwayProgress > 1) {
                        jumpingSwayProgress = 1;
                    }
                    lastOnGround = false;
                } else {
                    jumpingSwayProgress -= (currentTimeMillis - jumpingTimeStamp) / (JUMPING_SWAY_TIME * 1000);
                    if (jumpingSwayProgress < 0) {
                        jumpingSwayProgress = 0;
                    }
                }
            }
        }
        jumpingTimeStamp = currentTimeMillis;

        float ySway = JUMPING_DYNAMICS.update_tick_get(JUMPING_Y_SWAY * jumpingSwayProgress);
        @Nullable BedrockPart rootNode = model.getRootNode();
        if (rootNode != null) {
            // 基岩版模型 y 轴上下颠倒，sway 值取相反数
            rootNode.offsetY += -ySway / 16;
        }
    }

    /**
     * 应用瞄具摄像机定位组、机瞄摄像机定位组和 Idle 摄像机定位组的变换
     * <br>
     * 会在几个摄像机定位之间插值
     */
    private void _applyFirstPersonPositioningTransform(PoseStack poseStack,
                                                       GunModelObject gunModelObject,
                                                       ItemStack gunItem,
                                                       float aimingProgress, float refitScreenOpeningProgress) {
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        Matrix4f transformMatrix = new Matrix4f();
        transformMatrix.identity();

        // 应用瞄准定位
        @Nullable List<BedrockPart> idleNodePath = gunModelObject.getIdleSightPath();
        @Nullable List<BedrockPart> aimingNodePath = null;
        var scopeLocation = iGun.getAttachmentLocation(gunItem, AttachmentCategory.SCOPE);
        if (ResourceTag.NULL_LOCATION.equals(scopeLocation)) {
            scopeLocation = iGun.getBuiltinAttachmentLocation(gunItem, AttachmentCategory.SCOPE);
        }

        @Nullable CompoundTag scopeCustomDataTag = iGun.getAttachmentCustomDataTag(gunItem, AttachmentCategory.SCOPE);
        int scopeViewIndex = AttachmentNBTAccessor.INSTANCE.getScopeViewIndex(scopeCustomDataTag);
        if (scopeViewIndex < 0) {
            if (PlannedRefactor.MOVE_SCOPE_VIEW_INDEX_TO_CORE) {}
            scopeViewIndex = 0;
        }

        if (ResourceTag.NULL_LOCATION.equals(scopeLocation)) {
            // 未安装瞄具，使用机瞄定位组
            aimingNodePath = gunModelObject.getIronSightPath();
        } else {
            // 安装瞄具，组合瞄具定位组和瞄具视野定位组
            @Nullable List<BedrockPart> scopeNodePath = gunModelObject.getScopePosPath();
            if (scopeNodePath != null) {
                aimingNodePath = new ArrayList<>(scopeNodePath);
                @Nullable ClientAttachmentIndexInstance clientAttachmentIndexInstance = ClientResourceApi.getClientAttachmentIndexInstance(scopeLocation);
                if (clientAttachmentIndexInstance != null) {
                    /**
                     * scopeViewIndex会超过 pojo 列表长度的原因见{@link LivingShooterAim#_doZoom}
                     */
                    { // scopeViewIndex更正
                        AttachmentDisplay attachmentDisplay = clientAttachmentIndexInstance.getAttachmentDisplay();
                        int @Nullable [] scopeViewIndexList = attachmentDisplay.getScopeViewIndex();
                        if (scopeViewIndexList != null) {
                            /**
                             * {@link ClientAttachmentIndexInstance#checkScopeViewIndex}不保证列表长度不为0
                             */
                            scopeViewIndex = scopeViewIndexList.length != 0 ? scopeViewIndexList[scopeViewIndex % scopeViewIndexList.length] : 0;
                        }
                    }

                    { // 添加scope view path到aim path
                        @Nullable AttachmentModelObject attachmentModel = clientAttachmentIndexInstance.getAttachmentModel();
                        if (attachmentModel != null) {
                            @Nullable List<BedrockPart> scopeViewPath = attachmentModel.getScopeViewPath(currentScopeViewIndex == -1 ? scopeViewIndex : currentScopeViewIndex);
                            if (scopeViewPath != null) {
                                aimingNodePath.addAll(scopeViewPath);
                            }
                        }
                    }
                }
            }
        }

        Matrix4f aimingViewMatrix = _getPositioningNodeInverse(aimingNodePath);

        // 执行两个 scope view 之间的插值
        if (currentScopeViewIndex == -1) {
            currentScopeViewIndex = scopeViewIndex;
            oldScopeViewIndex = scopeViewIndex;
            oldAimingViewMatrix = aimingViewMatrix;
            SWITCH_VIEW_DYNAMICS = new MathUtil.SecondOrderDynamics(0.35f, 1.2f, 0.3f, scopeViewIndex);
        }

        float view_interpret = SWITCH_VIEW_DYNAMICS.update_tick_get(scopeViewIndex);
        float span = currentScopeViewIndex - oldScopeViewIndex;
        float switchingProgress = Math.abs(span) < 0.05 ? 1 : (view_interpret - oldScopeViewIndex) / span;
        MathUtil.applyMatrixLerp(aimingViewMatrix, oldAimingViewMatrix, aimingViewMatrix, 1 - switchingProgress);
        if (currentScopeViewIndex != scopeViewIndex) {
            oldAimingViewMatrix = aimingViewMatrix;
            oldScopeViewIndex = view_interpret;
            currentScopeViewIndex = scopeViewIndex;
        }

        // 应用瞄准变换
        MathUtil.applyMatrixLerp(transformMatrix, _getPositioningNodeInverse(idleNodePath), transformMatrix, (1 - refitScreenOpeningProgress));
        MathUtil.applyMatrixLerp(transformMatrix, aimingViewMatrix, transformMatrix, (1 - refitScreenOpeningProgress) * aimingProgress);

        // 应用改装界面开启时的定位
        float refitTransformProgress = (float) MathUtil.Easing.easeOutCubic(RefitScreenTransformState.get().getTransformProgress());
        AttachmentCategory oldType = RefitScreenTransformState.get().getOldTransformType();
        AttachmentCategory currentType = RefitScreenTransformState.get().getCurrentTransformType();
        List<BedrockPart> fromNode = gunModelObject.getRefitAttachmentViewPath(oldType);
        List<BedrockPart> toNode = gunModelObject.getRefitAttachmentViewPath(currentType);
        MathUtil.applyMatrixLerp(transformMatrix, _getPositioningNodeInverse(fromNode), transformMatrix, refitScreenOpeningProgress);
        MathUtil.applyMatrixLerp(transformMatrix, _getPositioningNodeInverse(toNode), transformMatrix, refitScreenOpeningProgress * refitTransformProgress);

        // 应用变换到 PoseStack
        poseStack.translate(0, 1.5f, 0);
        poseStack.mulPoseMatrix(transformMatrix);
        poseStack.translate(0, -1.5f, 0);
    }

    /**
     * 应用动画约束变换
     * @param weight 控制约束变换的权重，用于插值
     */
    private void _applyAnimationConstraintTransform(PoseStack poseStack, GunModelObject gunModel, float weight) {
        List<BedrockPart> nodePath = gunModel.getConstraintPath();
        if (nodePath == null) {
            return;
        }
        if (gunModel.getConstraintObject() == null) {
            return;
        }

        // 获取动画约束点的变换信息
        Vector3f originTranslation = new Vector3f();
        Vector3f animatedTranslation = new Vector3f();
        Vector3f rotation = new Vector3f();
        Vector3f translationICA = gunModel.getConstraintObject().translationConstraint;
        Vector3f rotationICA = gunModel.getConstraintObject().rotationConstraint;
        getAnimationConstraintTransform(nodePath, originTranslation, animatedTranslation, rotation);

        // 配合约束系数，计算约束位移需要的反向位移
        Vector3f inverseTranslation = new Vector3f(originTranslation);
        inverseTranslation.sub(animatedTranslation);
        inverseTranslation.mulDirection(poseStack.last().pose());
        inverseTranslation.mul(translationICA.x() - 1, translationICA.y() - 1, 1 - translationICA.z()); // 基岩版模型的旋转导致 xy 轴要反过来

        // 计算约束旋转需要的反向旋转。因需要插值，获取的是欧拉角
        Vector3f inverseRotation = new Vector3f(rotation);
        inverseRotation.mul(rotationICA.x() - 1, rotationICA.y() - 1, rotationICA.z() - 1);

        // 约束旋转
        poseStack.translate(animatedTranslation.x(), animatedTranslation.y() + 1.5f, animatedTranslation.z());
        poseStack.mulPose(Axis.XP.rotation(inverseRotation.x() * weight));
        poseStack.mulPose(Axis.YP.rotation(inverseRotation.y() * weight));
        poseStack.mulPose(Axis.ZP.rotation(inverseRotation.z() * weight));
        poseStack.translate(-animatedTranslation.x(), -animatedTranslation.y() - 1.5f, -animatedTranslation.z());

        // 约束位移
        Matrix4f poseMatrix = poseStack.last().pose();
        poseMatrix.m30(poseMatrix.m30() - inverseTranslation.x() * weight);
        poseMatrix.m31(poseMatrix.m31() - inverseTranslation.y() * weight);
        poseMatrix.m32(poseMatrix.m32() + inverseTranslation.z() * weight);
    }

    /**
     * 获取动画约束点的变换数据
     * @param originTranslation   用于输出约束点的原坐标
     * @param animatedTranslation 用于输出约束点经过动画变换之后的坐标
     * @param rotation            用于输出约束点的旋转
     */
    private static void getAnimationConstraintTransform(List<BedrockPart> nodePath,
                                                        @NotNull Vector3f originTranslation,
                                                        @NotNull Vector3f animatedTranslation,
                                                        @NotNull Vector3f rotation) {
        if (nodePath == null) return;

        // 约束点动画变换矩阵
        Matrix4f animeMatrix = new Matrix4f();

        // 约束点初始变换矩阵
        Matrix4f originMatrix = new Matrix4f();
        animeMatrix.identity();
        originMatrix.identity();
        BedrockPart constrainNode = nodePath.get(nodePath.size() - 1);

        for (int i = 0; i < nodePath.size(); i++) {
            BedrockPart part = nodePath.get(i);

            // 乘动画位移
            if (part != constrainNode) {
                animeMatrix.translate(part.offsetX, part.offsetY, part.offsetZ);
            }

            // 乘组位移
            if (part.getParent() != null) {
                animeMatrix.translate(part.x / 16.0F, part.y / 16.0F, part.z / 16.0F);
            } else {
                animeMatrix.translate(part.x / 16.0F, (part.y / 16.0F - 1.5F), part.z / 16.0F);
            }

            // 乘动画旋转
            if (part != constrainNode) {
                animeMatrix.rotate(part.additionalQuaternion);
            }

            // 乘组旋转
            animeMatrix.rotate(Axis.ZP.rotation(part.zRot));
            animeMatrix.rotate(Axis.YP.rotation(part.yRot));
            animeMatrix.rotate(Axis.XP.rotation(part.xRot));

            // 乘组位移
            if (part.getParent() != null) {
                originMatrix.translate(part.x / 16.0F, part.y / 16.0F, part.z / 16.0F);
            } else {
                originMatrix.translate(part.x / 16.0F, (part.y / 16.0F - 1.5F), part.z / 16.0F);
            }

            // 乘组旋转
            originMatrix.rotate(Axis.ZP.rotation(part.zRot));
            originMatrix.rotate(Axis.YP.rotation(part.yRot));
            originMatrix.rotate(Axis.XP.rotation(part.xRot));

        }

        // 把变换数据写入输出
        animeMatrix.getTranslation(animatedTranslation);
        originMatrix.getTranslation(originTranslation);
        Vector3f animatedRotation = MathUtil.getEulerAngles(animeMatrix);
        Vector3f originRotation = MathUtil.getEulerAngles(originMatrix);
        animatedRotation.sub(originRotation);
        rotation.set(animatedRotation.x(), animatedRotation.y(), animatedRotation.z());
    }

    /**
     * 获取摄像机定位组的反相矩阵
     */
    private static @NotNull Matrix4f _getPositioningNodeInverse(@Nullable List<BedrockPart> nodePath) {
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

    // --------Deprecated--------

    /**
     * 疑似被遗忘
     */
    @Deprecated private static boolean bulletFromPlayer(Entity entity) {
        if (entity instanceof GunProjectile entityBullet) {
            return entityBullet.getOwner() instanceof LocalPlayer;
        }
        return false;
    }
}
