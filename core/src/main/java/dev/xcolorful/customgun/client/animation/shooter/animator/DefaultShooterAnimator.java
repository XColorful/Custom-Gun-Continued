package dev.xcolorful.customgun.client.animation.shooter.animator;

import dev.xcolorful.customgun.client.api.animation.shooter.IShooterAnimator;
import dev.xcolorful.customgun.client.api.item.gun.IShooterAnimationCategory;
import dev.xcolorful.customgun.client.api.item.gun.ShooterAnimationCategory;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;

public class DefaultShooterAnimator implements IShooterAnimator {
    public static final DefaultShooterAnimator INSTANCE = new DefaultShooterAnimator();

    protected DefaultShooterAnimator() {
    }

    @Override
    public String getAnimatorName() {
        return "DefaultShooterAnimator";
    }

    @Override
    public IShooterAnimationCategory getAnimationCategory() {
        return ShooterAnimationCategory.DEFAULT;
    }

    @Override
    public void animateShooter(ModelPart head, ModelPart body, ModelPart leftArm, ModelPart rightArm,
                               ILivingShooter iLivingShooter, LivingEntity livingShooter,
                               GunDisplayInstance gunDisplayInstance) {
        // 按优先级排动画状态；mainArmAnimated / offArmAnimated 记录该手是否已被更高优先级的状态消费
        boolean mainArmAnimated = false;
        boolean offArmAnimated = false;

        boolean mainArmIsRight = livingShooter.getMainArm() == HumanoidArm.RIGHT;
        ModelPart mainArm = mainArmIsRight ? rightArm : leftArm;
        ModelPart offArm = mainArmIsRight ? leftArm : rightArm;

        /*
        趴下（Pose.SWIMMING）时原版会把 head.xRot 钉在 -PI/4，见 HumanoidModel#setupAnim 的 swimAmount 分支
        因此它既不再是视角俯仰，也不能当空闲姿态用
         */
        boolean prone = livingShooter.isVisuallySwimming();
        // 趴下时改用实体真实视角俯仰；夹取交给 MouseHandlerMixin
        float viewPitch = prone ? livingShooter.getXRot() * ((float) Math.PI / 180.0F) : head.xRot;

        // 1. 瞄准动画
        {
            // TODO 以后加到 GunDisplay 配置
            boolean mainArmAlwaysAim = false;
            boolean offArmAlwaysAim = false;
            boolean mainArmPoseWhenIdle = true && !prone;
            boolean offArmPoseWhenIdle = true && !prone;

            float aimProgress = iLivingShooter.cgc$getSynAimingProgress();

            // 始终瞄准时该手进度直接取满
            float mainArmAimingProgress = mainArmAlwaysAim ? 1.0f : aimProgress;
            float offArmAimingProgress = offArmAlwaysAim ? 1.0f : aimProgress;

            // 1.1 始终持枪手瞄准：直接覆盖成对准视线的姿态，不参与插值
            if (mainArmAlwaysAim) {
                this._animateShooterAimingMainArm(head, mainArm, prone, viewPitch);
                mainArmAnimated = true;
            }

            // 1.2 瞄准动画：双手各自独立插值，互不影响
            if (!mainArmAnimated && (mainArmPoseWhenIdle || mainArmAimingProgress > 0.001f)) {
                this._animateShooterAimingCrossbowMainArm(head, mainArm, mainArmIsRight,
                        prone, viewPitch,
                        mainArmPoseWhenIdle, mainArmAimingProgress);
                mainArmAnimated = true;
            }
            // 趴下时副手永远不做动画：原版爬行时也没有副手动画
            if (!prone && !offArmAnimated && (offArmPoseWhenIdle || offArmAimingProgress > 0.001f)) {
                this._animateShooterAimingCrossbowOffArm(head, offArm, mainArmIsRight,
                        offArmPoseWhenIdle, offArmAimingProgress);
                offArmAnimated = true;
            }
        }
    }

    // --------瞄准姿态：逐手--------

    /**
     * 把一条手臂从起点按瞄准进度插值到终点。四个姿态方法共用
     *
     * @param poseWhenIdle      true 时插值起点取 {@code idleYRot} / {@code idleXRot}，即该手的弩蓄满姿态，因此进度 0 时就是蓄满姿态
     *                          false 时起点取该手当前姿态（原版不瞄准时的手持物品动画），即从无动画插值过来
     * @param animationProgress 该手自己的瞄准进度，取值 0~1
     * @param idleYRot          起点（弩蓄满姿态）的 yRot
     * @param idleXRot          起点（弩蓄满姿态）的 xRot
     * @param idleZRot          起点的 zRot，不改变 zRot 时与 aimZRot 传同一个值
     * @param aimYRot           终点（对准视线）的 yRot，由 {@link #_mainArmAimYRot} 给出
     * @param aimXRot           终点（对准视线）的 xRot，由 {@link #_mainArmAimXRot} 给出
     * @param aimZRot           终点（对准视线）的 zRot，由 {@link #_mainArmAimZRot} 给出
     * @see #_animateShooterAimingMainArm 里列出的、对这些姿态都成立的共同偏离
     */
    public static void _applyArmAimPose(ModelPart arm, boolean poseWhenIdle, float animationProgress,
                                        float idleYRot, float idleXRot, float idleZRot,
                                        float aimYRot, float aimXRot, float aimZRot) {
        float fromYRot = poseWhenIdle ? idleYRot : arm.yRot;
        float fromXRot = poseWhenIdle ? idleXRot : arm.xRot;
        float fromZRot = poseWhenIdle ? idleZRot : arm.zRot;

        arm.yRot = Mth.lerp(animationProgress, fromYRot, aimYRot);
        arm.xRot = Mth.lerp(animationProgress, fromXRot, aimXRot);
        arm.zRot = Mth.lerp(animationProgress, fromZRot, aimZRot);
    }

    /**
     * 持枪手对准视线所需的 yRot
     * <ul>
     *     取值
     *     <li>站立时：就是 {@code head.yRot}</li>
     *     <li>趴下时：{@code 0}，趴下解不靠 yRot 表达偏航</li>
     * </ul>
     *
     * @param viewPitch 视角俯仰，站立时取 {@code head.xRot}，趴下时取实体真实俯仰
     * @see #_mainArmAimZRot 趴下为什么改用 zRot 表达偏航
     */
    public static float _mainArmAimYRot(ModelPart head, boolean prone, float viewPitch) {
        return prone ? 0.0F : head.yRot;
    }

    /**
     * 持枪手对准视线所需的 xRot
     * <ul>
     *     取值
     *     <li>站立时：{@code -PI/2 + viewPitch}，即原版 BOW_AND_ARROW / CROSSBOW_HOLD 的持械手关系</li>
     *     <li>趴下时：{@code viewPitch}，模型坐标架已被 {@code Rx(-90°)} 转过，手臂俯仰直接取视角俯仰</li>
     * </ul>
     *
     * @param viewPitch 视角俯仰，站立时取 {@code head.xRot}，趴下时取实体真实俯仰
     */
    public static float _mainArmAimXRot(ModelPart head, boolean prone, float viewPitch) {
        return prone ? viewPitch : -((float) Math.PI / 2.0F) + viewPitch;
    }

    /**
     * 持枪手对准视线所需的 zRot
     * <ul>
     *     取值
     *     <li>站立时：不变，仍交给原版走路摆动</li>
     *     <li>趴下时：{@code 2 * head.yRot}，趴下解的偏航靠 zRot 表达</li>
     * </ul>
     * <ul>
     *     为什么是 2 倍
     *     <li>令手臂世界朝向等于视线向量，配合 {@code yRot = 0} 解得 {@code zRot - 2 * head.yRot = 0}</li>
     *     <li>系数 2 来自 {@code Rx(-90°)} 把偏航同时带进了 x 与 z 两个世界分量</li>
     *     <li>写成 1 倍会让偏航只转一半，表现为朝向偏斜、视角左右移动时手往反方向走</li>
     * </ul>
     *
     * @param currentZRot 手臂当前的 zRot
     */
    public static float _mainArmAimZRot(ModelPart head, boolean prone, float currentZRot) {
        return prone ? 2.0F * head.yRot : currentZRot;
    }

    /**
     * 把持枪手覆盖成对准视线(满瞄准进度)的姿态，供"始终持枪手瞄准"使用，故不进插值
     * <ul>
     *     能去掉原版弩/弓给持械手加的偏航常量的原因
     *     <li>{@link ModelPart#translateAndRotate} 的旋转是 {@code rotationZYX(zRot, yRot, xRot)}，即{@code Rz·Ry·Rx}，{@code Rx} 在最内层</li>
     *     <li>手臂已被 {@code xRot} 压向前方，故 {@code yRot} 是绕竖直轴的水平偏航，手臂会连同挂在其下的物品一起偏</li>
     *     <li>原版那个常量（弓 0.1 rad ≈ 5.7°、弩 0.3 rad ≈ 17.2°）会原样变成枪口的偏航误差——原版弩靠 {@code crossbow.json} 里的{@code display.thirdperson_righthand} 补正物品朝向，枪械模型没有这一层</li>
     *     <li>去掉常量等价于绕竖直轴反向旋转物品，且该等价与前压俯仰无关</li>
     *     <li>而在渲染层补正所需的旋转轴{@code Rx(xRot)⁻¹·Ŷ} 会随俯仰变化，只在水平瞄准时准确</li>
     * </ul>
     * <ul>
     *     目标姿态的推导
     *     <li>世界朝向 = {@code S(-1,-1,1) · Ry(180°-yaw) · [?] · Rz(z)Ry(y)Rx(x) · (0,1,0)}，{@code [?]} 站立时为空、趴下时为 {@code Rx(-90°)}</li>
     *     <li>令其等于视线向量 {@code (-sin(yaw)cos(p), -sin(p), cos(yaw)cos(p))} 即可解出</li>
     *     <li>站立解：{@code xRot = -PI/2 + p}、{@code yRot = head.yRot}、zRot 不动，能还原出原版数值，可作校验</li>
     *     <li>趴下解：{@code xRot = p}、{@code yRot = 0}、{@code zRot = 2 * head.yRot}，见 {@link #_mainArmAimXRot} 等三个方法</li>
     *     <li>趴下不能沿用站立解：那需要把 {@code yRot} 摆到约 ±90°，等于绕手臂长轴滚半圈</li>
     *     <li>滚半圈会让挂在手上的枪翻过来，观感上就是手背朝前</li>
     * </ul>
     * <ul>
     *     其余共同偏离，移植时一并比对
     *     <li>不跟随潜行补偿：原版在 {@code this.crouching} 分支里对双臂 {@code xRot += 0.4F}，跟随则蹲下时枪口下压 0.4 rad（约 23°）</li>
     *     <li>原因：{@link HumanoidModel#createMesh} 里 {@code head} / {@code body} / {@code right_arm} / {@code left_arm} 都挂在 root 下互为兄弟节点</li>
     *     <li>因此原版 {@code body.xRot = 0.5F} 的前倾不会传导到手臂，手臂朝向完全由 {@code arm.xRot} 决定</li>
     *     <li>不跟随时 {@code arm.xRot = -PI/2 + head.xRot}，手臂始终垂直于视线，与是否蹲下无关</li>
     *     <li>原版加它是为了让垂手跟随前倾的躯干，与"举枪"目的相悖</li>
     * </ul>
     * <ul>
     *     未复现走路摆动
     *     <li>原版 {@code setupAnim} 末尾对双臂调用 {@link AnimationUtils#bobModelPart}</li>
     *     <li>原因：{@code bobModelPart} 需要 {@code ageInTicks}，当前接口拿不到，影响约 0.05 rad</li>
     * </ul>
     * <ul>
     *     常量已内联
     *     <li>这些姿态按手拆分，而原版 {@link AnimationUtils#animateCrossbowHold} 会同时写两只手，无法再整体调用</li>
     *     <li>因此不再自动跟随版本，需按各方法 javadoc 的原版位置比对</li>
     * </ul>
     */
    public static void _animateShooterAimingMainArm(ModelPart head, ModelPart arm, boolean prone, float viewPitch) {
        arm.yRot = _mainArmAimYRot(head, prone, viewPitch);
        arm.xRot = _mainArmAimXRot(head, prone, viewPitch);
        arm.zRot = _mainArmAimZRot(head, prone, arm.zRot);
    }

    /**
     * 弓 ({@link BowItem}) 的持械手姿态：从原版弓蓄满姿态插值到对准视线
     * <ul>
     *     起点（原版弓蓄满姿态）
     *     <li>取原版 {@link HumanoidModel#poseRightArm} / {@link HumanoidModel#poseLeftArm} 的 {@link HumanoidModel.ArmPose#BOW_AND_ARROW} 分支之持械手</li>
     *     <li>值：{@code yRot = ∓0.1 + head.yRot}，{@code xRot = -PI/2 + head.xRot}</li>
     *     <li>原版由 {@link PlayerRenderer#getArmPose} 在拉弓时选中该分支</li>
     * </ul>
     * <ul>
     *     终点
     *     <li>{@link #_animateShooterAimingMainArm}，趴下时换一组解</li>
     * </ul>
     */
    public static void _animateShooterAimingBowMainArm(ModelPart head, ModelPart arm, boolean mainArmIsRight,
                                                       boolean prone, float viewPitch,
                                                       boolean poseWhenIdle, float animationProgress) {
        _applyArmAimPose(arm, poseWhenIdle, animationProgress,
                (mainArmIsRight ? -0.1F : 0.1F) + head.yRot,
                -((float) Math.PI / 2.0F) + head.xRot,
                arm.zRot,
                _mainArmAimYRot(head, prone, viewPitch),
                _mainArmAimXRot(head, prone, viewPitch),
                _mainArmAimZRot(head, prone, arm.zRot));
    }

    /**
     * 弩 ({@link CrossbowItem}) 的持械手姿态：从原版弩蓄满姿态插值到对准视线
     * <ul>
     *     起点（原版弩蓄满姿态）
     *     <li>取原版 {@link AnimationUtils#animateCrossbowHold} 的持械手</li>
     *     <li>值：{@code yRot = ∓0.3 + head.yRot}，{@code xRot = -PI/2 + head.xRot + 0.1}</li>
     *     <li>{@link HumanoidModel.ArmPose#CROSSBOW_HOLD}，原版由 {@link PlayerRenderer#getArmPose} 在弩已蓄满且玩家未挥手时选中</li>
     * </ul>
     * <ul>
     *     终点
     *     <li>{@link #_animateShooterAimingMainArm}，趴下时换一组解</li>
     * </ul>
     * <ul>
     *     与弓的持械手相比
     *     <li>多一项 {@code xRot} 的 {@code +0.1} 俯仰偏移</li>
     *     <li>{@code ∓0.3} 偏航远大于弓的 {@code ∓0.1}</li>
     *     <li>俯仰偏移与偏航常量都在插值中一同收敛</li>
     * </ul>
     */
    public static void _animateShooterAimingCrossbowMainArm(ModelPart head, ModelPart arm, boolean mainArmIsRight,
                                                            boolean prone, float viewPitch,
                                                            boolean poseWhenIdle, float animationProgress) {
        _applyArmAimPose(arm, poseWhenIdle, animationProgress,
                (mainArmIsRight ? -0.3F : 0.3F) + head.yRot,
                -((float) Math.PI / 2.0F) + head.xRot + 0.1F,
                arm.zRot,
                _mainArmAimYRot(head, prone, viewPitch),
                _mainArmAimXRot(head, prone, viewPitch),
                _mainArmAimZRot(head, prone, arm.zRot));
    }

    /**
     * 弓的副手姿态
     * <ul>
     *     <li>姿态出处：原版 {@code BOW_AND_ARROW} 分支之副手：{@code yRot = ±0.1 + head.yRot ± 0.4}，{@code xRot = -PI/2 + head.xRot}</li>
     *     <li>副手不挂枪械，未做偏离，也不收敛到视线朝向</li>
     *     <li>插值：起止相同，即不随瞄准进度变化</li>
     *     <li>启用条件：趴下时不会启用：{@code head.xRot} 此时被原版钉住，不能当姿态用</li>
     * </ul>
     */
    public static void _animateShooterAimingBowOffArm(ModelPart head, ModelPart arm, boolean mainArmIsRight,
                                                      boolean poseWhenIdle, float animationProgress) {
        float yRot = (mainArmIsRight ? 0.1F : -0.1F) + (mainArmIsRight ? 0.4F : -0.4F) + head.yRot;
        float xRot = -((float) Math.PI / 2.0F) + head.xRot;

        _applyArmAimPose(arm, poseWhenIdle, animationProgress, yRot, xRot, arm.zRot, yRot, xRot, arm.zRot);
    }

    /**
     * 弩的副手姿态
     * <ul>
     *     <li>姿态出处：原版 {@link AnimationUtils#animateCrossbowHold} 之副手：{@code yRot = ±0.6 + head.yRot}，{@code xRot = -1.5 + head.xRot}</li>
     *     <li>副手不挂枪械，未做偏离，也不收敛到视线朝向</li>
     *     <li>插值：起止相同，即不随瞄准进度变化</li>
     *     <li>启用条件：趴下时不会启用：{@code head.xRot} 此时被原版钉住，不能当姿态用</li>
     * </ul>
     */
    public static void _animateShooterAimingCrossbowOffArm(ModelPart head, ModelPart arm, boolean mainArmIsRight,
                                                           boolean poseWhenIdle, float animationProgress) {
        float yRot = (mainArmIsRight ? 0.6F : -0.6F) + head.yRot;
        float xRot = -1.5F + head.xRot;

        _applyArmAimPose(arm, poseWhenIdle, animationProgress, yRot, xRot, arm.zRot, yRot, xRot, arm.zRot);
    }
}
