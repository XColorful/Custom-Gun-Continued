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

        // 1. 瞄准动画
        {
            // TODO 以后加到 GunDisplay 配置
            boolean mainArmAlwaysAim = false;
            boolean offArmAlwaysAim = false;
            boolean mainArmPoseWhenIdle = true;
            boolean offArmPoseWhenIdle = true;

            float aimProgress = iLivingShooter.cgc$getSynAimingProgress();

            // 始终瞄准时该手进度直接取满
            float mainArmAimingProgress = mainArmAlwaysAim ? 1.0f : aimProgress;
            float offArmAimingProgress = offArmAlwaysAim ? 1.0f : aimProgress;

            // 1.1 始终持枪手瞄准：最高优先级，直接覆盖成对准视线的姿态，不参与插值
            if (mainArmAlwaysAim) {
                this.animateShooterAimingMainArm(head, mainArm);
                mainArmAnimated = true;
            }

            // 1.2 瞄准动画：双手各自独立插值，互不影响
            // 不能只看进度：进度为 0 时若配置了空闲时保持姿态，同样要应用，此时插值起点是弩蓄满姿态
            if (!mainArmAnimated && (mainArmPoseWhenIdle || mainArmAimingProgress > 0.001f)) {
                this.animateShooterAimingCrossbowMainArm(head, mainArm, mainArmIsRight,
                        mainArmPoseWhenIdle, mainArmAimingProgress);
                mainArmAnimated = true;
            }
            if (!offArmAnimated && (offArmPoseWhenIdle || offArmAimingProgress > 0.001f)) {
                this.animateShooterAimingCrossbowOffArm(head, offArm, mainArmIsRight,
                        offArmPoseWhenIdle, offArmAimingProgress);
                offArmAnimated = true;
            }
        }
    }

    // --------瞄准姿态：逐手--------

    /**
     * 把一条手臂从起点按瞄准进度插值到终点。四个姿态方法共用。
     *
     * @param poseWhenIdle      true 时插值起点取 {@code idleYRot} / {@code idleXRot}，即该手的弩蓄满姿态，
     *                          因此进度 0 时就是蓄满姿态；false 时起点取该手当前姿态
     *                          （原版不瞄准时的手持物品动画），即从无动画插值过来。
     * @param animationProgress 该手自己的瞄准进度，取值 0~1
     * @param idleYRot          起点（弩蓄满姿态）的 yRot
     * @param idleXRot          起点（弩蓄满姿态）的 xRot
     * @param aimYRot           终点（对准视线）的 yRot
     * @param aimXRot           终点（对准视线）的 xRot
     * @see #animateShooterAimingMainArm 里列出的、对这些姿态都成立的共同偏离
     */
    private static void applyArmAimPose(ModelPart arm, boolean poseWhenIdle, float animationProgress,
                                        float idleYRot, float idleXRot, float aimYRot, float aimXRot) {
        float fromYRot = poseWhenIdle ? idleYRot : arm.yRot;
        float fromXRot = poseWhenIdle ? idleXRot : arm.xRot;

        arm.yRot = Mth.lerp(animationProgress, fromYRot, aimYRot);
        arm.xRot = Mth.lerp(animationProgress, fromXRot, aimXRot);
    }

    /**
     * 把持枪手覆盖成对准视线的姿态，供"始终持枪手瞄准"使用，故不进插值。
     * <p>
     * 这也是各持械手姿态在进度满时收敛到的终点姿态。之所以能去掉原版弩/弓给持械手加的偏航常量：
     * {@link ModelPart#translateAndRotate} 的旋转是 {@code rotationZYX(zRot, yRot, xRot)}，即
     * {@code Rz·Ry·Rx}，{@code Rx} 在最内层。手臂已被 {@code xRot} 压向前方，故 {@code yRot} 是绕竖直轴的
     * 水平偏航，手臂会连同挂在其下的物品一起偏。原版那个常量（弓 0.1 rad ≈ 5.7°、弩 0.3 rad ≈ 17.2°）
     * 会原样变成枪口的偏航误差——原版弩靠 {@code crossbow.json} 里的
     * {@code display.thirdperson_righthand} 补正物品朝向，枪械模型没有这一层。与其在渲染层补，不如从源头不加：
     * 去掉常量等价于绕竖直轴反向旋转物品，且该等价与前压俯仰无关；而在渲染层补正所需的旋转轴
     * {@code Rx(xRot)⁻¹·Ŷ} 会随俯仰变化，只在水平瞄准时准确。
     * <p>
     * 其余共同偏离，移植时一并比对：
     * <ul>
     *     <li><b>不跟随潜行补偿</b>：{@link HumanoidModel#createMesh} 里 {@code head} / {@code body} /
     *         {@code right_arm} / {@code left_arm} 都挂在 root 下互为兄弟节点，故原版
     *         {@code body.xRot = 0.5F} 的前倾不会传导到手臂，手臂朝向完全由 {@code arm.xRot} 决定。
     *         原版在 {@code this.crouching} 分支里对双臂 {@code xRot += 0.4F}，不跟随即
     *         {@code arm.xRot = -PI/2 + head.xRot}，手臂始终垂直于视线，与是否蹲下无关；跟随则蹲下时
     *         枪口下压 0.4 rad（约 23°）。原版加它是为了让垂手跟随前倾的躯干，与"举枪"目的相悖。</li>
     *     <li><b>未复现走路摆动</b>：原版 {@code setupAnim} 末尾对双臂调用
     *         {@link AnimationUtils#bobModelPart}，需要 {@code ageInTicks}，当前接口拿不到。
     *         影响约 0.05 rad。</li>
     *     <li><b>常量已内联</b>：这些姿态按手拆分，而原版 {@link AnimationUtils#animateCrossbowHold}
     *         会同时写两只手，无法再整体调用，故不再自动跟随版本，需按各方法 javadoc 的原版位置比对。</li>
     * </ul>
     */
    private void animateShooterAimingMainArm(ModelPart head, ModelPart arm) {
        arm.yRot = head.yRot;
        arm.xRot = -((float) Math.PI / 2.0F) + head.xRot;
    }

    /**
     * 弓 ({@link BowItem}) 的持械手姿态：从原版弓蓄满姿态插值到对准视线。
     * <p>
     * 起点取原版 {@link HumanoidModel#poseRightArm} / {@link HumanoidModel#poseLeftArm} 的
     * {@link HumanoidModel.ArmPose#BOW_AND_ARROW} 分支之持械手 {@code yRot = ∓0.1 + head.yRot}、
     * {@code xRot = -PI/2 + head.xRot}（原版由 {@link PlayerRenderer#getArmPose} 在拉弓时选中）；
     * 终点为 {@link #animateShooterAimingMainArm}。
     */
    private void animateShooterAimingBowMainArm(ModelPart head, ModelPart arm, boolean mainArmIsRight,
                                                boolean poseWhenIdle, float animationProgress) {
        applyArmAimPose(arm, poseWhenIdle, animationProgress,
                (mainArmIsRight ? -0.1F : 0.1F) + head.yRot,
                -((float) Math.PI / 2.0F) + head.xRot,
                head.yRot,
                -((float) Math.PI / 2.0F) + head.xRot);
    }

    /**
     * 弩 ({@link CrossbowItem}) 的持械手姿态：从原版弩蓄满姿态插值到对准视线。
     * <p>
     * 起点取原版 {@link AnimationUtils#animateCrossbowHold} 的持械手 {@code yRot = ∓0.3 + head.yRot}、
     * {@code xRot = -PI/2 + head.xRot + 0.1}（{@link HumanoidModel.ArmPose#CROSSBOW_HOLD}，
     * 原版由 {@link PlayerRenderer#getArmPose} 在弩已蓄满且玩家未挥手时选中）；
     * 终点为 {@link #animateShooterAimingMainArm}。
     * <p>
     * 与弓的持械手相比，弩多一项 {@code xRot} 的 {@code +0.1} 俯仰偏移，且 {@code ∓0.3} 偏航远大于弓的
     * {@code ∓0.1}，两者都在插值中同 {@code yRot} 一起收敛。
     */
    private void animateShooterAimingCrossbowMainArm(ModelPart head, ModelPart arm, boolean mainArmIsRight,
                                                     boolean poseWhenIdle, float animationProgress) {
        applyArmAimPose(arm, poseWhenIdle, animationProgress,
                (mainArmIsRight ? -0.3F : 0.3F) + head.yRot,
                -((float) Math.PI / 2.0F) + head.xRot + 0.1F,
                head.yRot,
                -((float) Math.PI / 2.0F) + head.xRot);
    }

    /**
     * 弓的副手姿态，起止相同，即不随瞄准进度变化。
     * <p>
     * 原版 {@code BOW_AND_ARROW} 分支之副手：{@code yRot = ±0.1 + head.yRot ± 0.4}，
     * {@code xRot = -PI/2 + head.xRot}。副手不挂枪械，未做偏离，也不收敛到视线朝向。
     */
    private void animateShooterAimingBowOffArm(ModelPart head, ModelPart arm, boolean mainArmIsRight,
                                               boolean poseWhenIdle, float animationProgress) {
        float yRot = (mainArmIsRight ? 0.1F : -0.1F) + (mainArmIsRight ? 0.4F : -0.4F) + head.yRot;
        float xRot = -((float) Math.PI / 2.0F) + head.xRot;

        applyArmAimPose(arm, poseWhenIdle, animationProgress, yRot, xRot, yRot, xRot);
    }

    /**
     * 弩的副手姿态，起止相同，即不随瞄准进度变化。
     * <p>
     * 原版 {@link AnimationUtils#animateCrossbowHold} 之副手：{@code yRot = ±0.6 + head.yRot}，
     * {@code xRot = -1.5 + head.xRot}。副手不挂枪械，未做偏离，也不收敛到视线朝向。
     */
    private void animateShooterAimingCrossbowOffArm(ModelPart head, ModelPart arm, boolean mainArmIsRight,
                                                    boolean poseWhenIdle, float animationProgress) {
        float yRot = (mainArmIsRight ? 0.6F : -0.6F) + head.yRot;
        float xRot = -1.5F + head.xRot;

        applyArmAimPose(arm, poseWhenIdle, animationProgress, yRot, xRot, yRot, xRot);
    }
}
