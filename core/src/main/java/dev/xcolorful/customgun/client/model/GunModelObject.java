/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.animation.listener.model.ModelAdditionalMagazineListener;
import dev.xcolorful.customgun.client.api.animation.ObjectAnimationChannel;
import dev.xcolorful.customgun.client.api.animation.listener.IAnimationListener;
import dev.xcolorful.customgun.client.api.model.IGunModelObjectRenderer;
import dev.xcolorful.customgun.client.api.model.IModelTextConsumer;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.renderer.model.ShellRender;
import dev.xcolorful.customgun.client.renderer.model.TextRender;
import dev.xcolorful.customgun.client.resource.assets.display._ModelNodeTextDisplay;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.MagazineCategory;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry.NodeName;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public final class GunModelObject extends AnimatedModelObject implements IGunModelObjectRenderer, IModelTextConsumer {

    /**
     * 在改装界面，点击各个配件时的摄像机视角
     */
    final EnumMap<AttachmentCategory, List<BedrockPart>> refitAttachmentViewPath = new EnumMap<>(AttachmentCategory.class);
    /**
     * 当前安装的配件物品
     */
    final EnumMap<AttachmentCategory, ItemStack> currentAttachmentItem = new EnumMap<>(AttachmentCategory.class);
    /**
     * 需要渲染的适配器
     */
    final HashSet<String> adapterToRender = new HashSet<>();
    /**
     * 抛壳渲染
     */
    final ArrayList<ShellRender> shellRenders = new ArrayList<>();

    /**
     * 第一人称机瞄摄像机定位组的路径
     */
    @Nullable List<BedrockPart> ironSightPath;
    /*
     * 第一人称idle状态摄像机定位组的路径
     * @deprecated 跟父类重复了
     */
//    private @Nullable List<BedrockPart> idleSightPath;
    /**
     * 第三人称手部物品渲染原点定位组的路径
     */
    @Nullable List<BedrockPart> thirdPersonHandOriginPath;
    /**
     * 展示框渲染原点定位组的路径
     */
    @Nullable List<BedrockPart> fixedOriginPath;
    /**
     * 地面实体渲染原点定位组的路径
     */
    @Nullable List<BedrockPart> groundOriginPath;
    /**
     * 瞄具配件定位组的路径。其他配件不需要存路径，只需要替换渲染。但是瞄具定位组需要用来辅助第一人称瞄准的摄像机定位。
     */
    @Nullable List<BedrockPart> scopePosPath;
    /**
     * 枪口火焰定位组
     */
    @Nullable List<BedrockPart> muzzleFlashPosPath;
    /*
     * 根组
     * @deprecated 跟父类重复了
     */
//    private @Nullable BedrockPart root;
    /**
     * 弹匣定位组
     */
    @Nullable BedrockPart magazineNode;
    /**
     * 换弹时第二个弹匣定位组
     */
    @Nullable BedrockPart additionalMagazineNode;
    /**
     * 激光
     */
    @Nullable List<BedrockPart> laserBeamPaths;

    private boolean renderHand = true;
    boolean renderMount; // 瞄具导轨/机枪脚架?
    ItemStack currentGunItem;
    MagazineCategory currentMagazineCategory = MagazineCategory.NONE;

    GunModelObject(@NotNull BedrockModel pojo) {
        super(pojo);
    }

    public static @Nullable GunModelObject fromPojo(BedrockModel pojo) {
        if (pojo == null) return null;
        GunModelObject instance = new GunModelObject(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        if (!super.resetCache()) return false;

        // 弹匣
        _GunLoader.constructMagazineNode(this);

        // functional
        _GunLoader.constructFunctionalRenderer(this);

        // 缓存其他定位组
        _GunLoader.constructOtherPath(this);

        // 缓存改装 UI 下各个配件的特写视角定位组
        _GunLoader.constructRefitAttachmentViewPath(this);

        // 缓存抛壳窗
        _GunLoader.constructShellOriginNodes(this);

        // 准备各个配件的渲染
        _GunLoader.constructAllAttachmentRender(this);

        // 配件转接口渲染
        _GunLoader.constructAttachmentAdapterNodeRender(this);

        return true;
    }

    @Override
    public void cleanAnimationTransform() {
        super.cleanAnimationTransform();
        if (this.additionalMagazineNode != null) {
            this.additionalMagazineNode.visible = false;
        }
    }

    // --------IModelObjectRender--------

    void super_render(PoseStack matrixStack,
                      ItemDisplayContext transformType,
                      RenderType renderType,
                      int light, int overlay) {
        super.render(matrixStack,
                transformType,
                renderType,
                light, overlay);
    }

    // --------IAnimationListenerSupplier--------

    @Override
    public @Nullable IAnimationListener supplyListeners(String nodeName, ObjectAnimationChannel.ChannelType type) {
        @Nullable IAnimationListener listener = super.supplyListeners(nodeName, type);
        if (listener == null) return null;

        if (NodeName.MAG_ADDITIONAL.matches(nodeName)) {
            // 额外弹匣只有当动画中有它的关键帧的时候才渲染
            return new ModelAdditionalMagazineListener(listener, this);
        }
        return listener;
    }

    // --------IGunModelObjectRenderer--------

    // ----Getter & Setter----

    @Override public EnumMap<AttachmentCategory, ItemStack> getCurrentAttachmentItem() {
        return this.currentAttachmentItem;
    }
    @Override public ItemStack getCurrentGunItem() {
        return this.currentGunItem;
    }
    @Override public @Nullable BedrockPart getAdditionalMagazineNode() {
        return this.additionalMagazineNode;
    }
    @Override public @Nullable List<BedrockPart> getIronSightPath() {
        return this.ironSightPath;
    }
    @Override public @Nullable List<BedrockPart> getIdleSightPath() {
        return this.ironSightPath;
    }
    @Override public @Nullable List<BedrockPart> getThirdPersonHandOriginPath() {
        return this.thirdPersonHandOriginPath;
    }
    @Override public @Nullable List<BedrockPart> getFixedOriginPath() {
        return this.fixedOriginPath;
    }
    @Override public @Nullable List<BedrockPart> getGroundOriginPath() {
        return this.groundOriginPath;
    }
    @Override public @Nullable List<BedrockPart> getMuzzleFlashPosPath() {
        return this.muzzleFlashPosPath;
    }
    @Override public @Nullable List<BedrockPart> getScopePosPath() {
        return this.scopePosPath;
    }
    @Override public @Nullable List<BedrockPart> getRefitAttachmentViewPath(AttachmentCategory type) {
        return this.refitAttachmentViewPath.get(type);
    }
    @Override public @Nullable ShellRender getShellRender(int index) {
        if (index < 0 || index >= this.shellRenders.size()) {
            return null;
        }
        return this.shellRenders.get(index);
    }
    @Override public @Nullable BedrockPart getRootNode() {
        return this.root;
    }
    @Override public boolean getRenderHand() {
        return this.renderHand;
    }

    @Override public void setRenderHand(boolean renderHand) {
        this.renderHand = renderHand;
    }
    // --------IGunModelObjectRender--------

    @Override
    public void render(PoseStack matrixStack,
                       ItemDisplayContext transformType,
                       RenderType renderType,
                       int light, int overlay,
                       ItemStack gunItem) {
        _GunModelRender.render(this,
                matrixStack,
                transformType,
                renderType,
                light, overlay,
                gunItem);
    }

    // --------IModelTextConsumer--------

    @Override
    public void setTextShowList(Map<String, _ModelNodeTextDisplay> modelNodeTextDisplay) {
        modelNodeTextDisplay.forEach((name, _pojo) -> {
            this.setFunctionalRenderer(name, bedrockPart -> new TextRender(this, _pojo, this.currentGunItem));
        });
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true) private @Nullable IModelComponentRenderer ammoHiddenRender(BedrockPart bedrockPart, Predicate<IGun> predicate) {
        return null;
    }
    @Deprecated(forRemoval = true) private @Nullable IModelComponentRenderer scopeHiddenRender(BedrockPart bedrockPart, Predicate<ItemStack> predicate) {
        return null;
    }
    @Deprecated(forRemoval = true) private @Nullable IModelComponentRenderer extendedMagHiddenRender(BedrockPart bedrockPart, int level) {
        MagazineCategory magazineCategory = MagazineCategory.fromIndex(level);
        return null;
    }
    @Deprecated(forRemoval = true) private @Nullable IModelComponentRenderer renderAdditionalMagazine(BedrockPart bedrockPart) {
        return null;
    }
    @Deprecated(forRemoval = true) private @Nullable IModelComponentRenderer handguardTacticalRender(BedrockPart bedrockPart) {
        return null;
    }
    @Deprecated(forRemoval = true) private @Nullable IModelComponentRenderer handguardDefaultRender(BedrockPart bedrockPart) {
        return null;
    }
    @Deprecated(forRemoval = true) private void cacheOtherPath() {
        _GunLoader.constructOtherPath(this);
    }
    @Deprecated(forRemoval = true) private void cacheRefitAttachmentViewPath() {
        _GunLoader.constructRefitAttachmentViewPath(this);
    }
    @Deprecated(forRemoval = true) private void cacheShellOriginNodes() {
        _GunLoader.constructShellOriginNodes(this);
    }
    @Deprecated(forRemoval = true) private void allAttachmentRender() {
        _GunLoader.constructAllAttachmentRender(this);
    }
    @Deprecated(forRemoval = true) private boolean checkShowMuzzle(BedrockPart bedrockPart, ItemStack attachmentItem) {
        return false;
    }
    @Deprecated(forRemoval = true) private @Nullable IModelComponentRenderer attachmentAdapterNodeRender(BedrockPart bedrockPart) {
        return null;
    }
}
