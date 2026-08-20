/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.screen;

import dev.xcolorful.customgun.client.animation.screen.RefitScreenTransformState;
import dev.xcolorful.customgun.client.api.gui.GuiSize;
import dev.xcolorful.customgun.client.api.gui.screen.refit.IGunRefitScreen;
import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.api.sound.attachment.AttachmentSoundType;
import dev.xcolorful.customgun.client.gui.screen.refit.*;
import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.client.resource.assets.display._LaserDisplay;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import dev.xcolorful.customgun.client.sound.SoundPlayManager;
import dev.xcolorful.customgun.client.util.ClientGuiUtils;
import dev.xcolorful.customgun.client.util.ClientMcUtils;
import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.network.message.ClientMessageLaserColor;
import dev.xcolorful.customgun.core.network.message.ClientMessageRefitGun;
import dev.xcolorful.customgun.core.network.message.ClientMessageUnloadAttachment;
import dev.xcolorful.customgun.core.util.InventoryUtils;
import dev.xcolorful.customgun.core.util.SendUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 目前扩展性设计仅一个mixin注入点
 */
public class GunRefitScreen extends NoBackgroundScreen implements IGunRefitScreen<GunRefitScreen> {

    /**
     * 屏幕右侧留出的距离 (1920x1080p下120像素)
     */
    public static final int screenXMargin = 120 / GuiSize._sizeToPixelRatio;
    /**
     * 屏幕上方留出的距离 (1920x1080p下40像素)
     */
    public static final int screenYMargin = 40 / GuiSize._sizeToPixelRatio;

    public static final int INVENTORY_SLOT_COUNT = 9;

    protected GunRefitScreen() {
        super(Component.literal("Gun Refit Screen"));
        RefitScreenTransformState.get().reset();
    }
    public static IGunRefitScreen<?> create() {
        // mixin注入点
        if (PlannedRefactor.ON_CREATE_SCREEN) return new GunRefitScreen();
        return new GunRefitScreen();
    }

    // --------Screen--------

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float pPartialTick) {
        super.render(graphics, mouseX, mouseY, pPartialTick);

        for (GuiEventListener child : this.children()) {
            if (child instanceof IStackTooltip guiComponent) {
                guiComponent.renderTooltip(stack -> ClientGuiUtils.renderTooltip(graphics, font, mouseX, mouseY, stack));
            }
        }
    }

    @Override
    public void init() {
        this.clearWidgets();

        Minecraft mc = getMinecraft();
        LocalPlayer player = mc.player;
        if (player == null) return;

        ItemStack gunItem = player.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            // 没枪就直接退出界面，延迟到下一tick处理以避免在 init 生命周期中重入切换 Screen
            ClientMcUtils.schedule(mc, () -> {
                if (ClientGuiUtils.getCurrentScreen(mc) instanceof IGunRefitScreen<?>) ClientGuiUtils.setCurrentScreen(mc, null);
            });
            return;
        }

        this._resetCache(iGun, gunItem);

        // 配件槽位
        this._addAttachmentCategoryButtons(player, iGun, gunItem);

        // 可选配件列表
        this._addInventoryAttachmentButtons(player, iGun, gunItem);
    }

    private static final AttachmentCategory[] ATTACHMENT_CATEGORIES = AttachmentCategory.values();
    private void _addAttachmentCategoryButtons(LocalPlayer localPlayer,
                                               IGun iGun, ItemStack gunItem) {
        int currentX = this.width - screenXMargin
                // 从屏幕左边开始绘制往右绘制，使currentX数值正增
                - CustomTexture.SLOT.getWidth() * (ATTACHMENT_CATEGORIES.length - 2);
        int currentY = screenYMargin;
        Inventory inventory = localPlayer.getInventory();
        int selectedSlot = InventoryUtils.getSelectedSlot(inventory);

        for (int i = 0; i < ATTACHMENT_CATEGORIES.length; i++) {
            AttachmentCategory attachmentCategory = ATTACHMENT_CATEGORIES[i];
            AttachmentCategory currentCategory = RefitScreenTransformState.get().getCurrentTransformType();

            // 特殊处理
            switch (attachmentCategory) {
                case NONE -> {
                    if (this.isLaserCustomizedColorEnabled) {
                        // 添加镭射颜色选择器
//                        HSVSliderGroup hsvSliderGroup = new HSVSliderGroup(this.width, this.height, 120, 16, inventory, inventory.selected, AttachmentCategory.NONE);
//                        this.addRenderableWidget(hsvSliderGroup.getHueSlider());
//                        this.addRenderableWidget(hsvSliderGroup.getSaturationSlider());
                    }
                    continue;
                }
            }

            boolean hasAttachmentLock = iGun.hasAttachmentLock(gunItem);
            boolean isCategoryEnabled = iGun.isAttachmentEnabled(gunItem, attachmentCategory);
            boolean isSelected = !hasAttachmentLock && isCategoryEnabled && currentCategory == attachmentCategory;
            AttachmentCategorySlot button = new AttachmentCategorySlot(currentX, currentY, attachmentCategory, selectedSlot, inventory,
                    isSelected, hasAttachmentLock, isCategoryEnabled,
                    this::_onAttachmentCategoryButtonPress);

            // 选中的attachment button
            if (isSelected) {
                ItemStack attachmentItem = button.getAttachmentItem();
                if (!attachmentItem.isEmpty()) {
                    // 添加拆卸配件按钮
                    this.addRenderableWidget(new RefitUnloadButton(currentX, currentY + CustomTexture.SLOT.getHeight(),
                            (_button) -> this._onUnloadButtonPress(localPlayer, inventory, button)));

                    // 添加配件镭射
                    this._addAttachmentLaser(attachmentItem);
                }
            }

            this.addRenderableWidget(button);
            currentX += CustomTexture.SLOT.getWidth();
        }
    }
    private void _onAttachmentCategoryButtonPress(Button _button) {
        AttachmentCategorySlot button = (AttachmentCategorySlot) _button;
        AttachmentCategory buttonType = button.getCategory();

        if (
                // 有配件锁
                button.hasAttachmentLock()
                // 这个槽位不允许安装配件
                || !button.isCategoryEnabled()
                // 点击的是当前选中的槽位
                || buttonType != AttachmentCategory.NONE && RefitScreenTransformState.get().getCurrentTransformType() == buttonType
        ) {
            // 如果这个槽位不允许安装配件，则默认退回概览，不选中槽位
            if (RefitScreenTransformState.get().changeRefitScreenView(AttachmentCategory.NONE)) {
                this.resetScreen();
            }
            return;
        }

        // 切换选中的槽位
        if (RefitScreenTransformState.get().changeRefitScreenView(buttonType)) {
            this.resetScreen();
        }
    }
    private void _onUnloadButtonPress(LocalPlayer localPlayer, Inventory inventory,
                                      AttachmentCategorySlot button) {
        ItemStack attachmentItem = button.getAttachmentItem();
        if (attachmentItem.isEmpty()) return;

        int freeSlot = inventory.getFreeSlot();
        if (freeSlot == -1) {
            // TODO 客户端配置：是否在背包空间不足时仍然卸载
            return;
        }

        SoundPlayManager.get().playerRefitSound(attachmentItem, localPlayer, AttachmentSoundType.UNINSTALL_SOUND);

        int selectSlot = InventoryUtils.getSelectedSlot(inventory);
        SendUtils.sendMessageToServer(new ClientMessageUnloadAttachment(selectSlot, RefitScreenTransformState.get().getCurrentTransformType()));
    }
    private void _addAttachmentLaser(ItemStack attachmentItem) {
        @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
        if (iAttachment == null) return;

        var attachmentLocation = iAttachment.getAttachmentLocation(attachmentItem);
        @Nullable ClientAttachmentIndexInstance clientAttachmentIndexInstance = ClientResourceApi.getClientAttachmentIndexInstance(attachmentLocation);
        if (clientAttachmentIndexInstance == null) return;

        AttachmentDisplay attachmentDisplay = clientAttachmentIndexInstance.getAttachmentDisplay();
        @Nullable _LaserDisplay laserDisplay = attachmentDisplay.getLaserDisplay();
        if (laserDisplay == null) return;

        if (!laserDisplay.isEnableCustomizedColor()) return;

//        HSVSliderGroup hsvSliderGroup = new HSVSliderGroup(this.width, this.height, 120, 16, inventory, inventory.selected, AttachmentCategory.NONE);
//        this.addRenderableWidget(hsvSliderGroup.getHueSlider());
//        this.addRenderableWidget(hsvSliderGroup.getSaturationSlider());
    }

    /**
     * 保留状态，不随{@link #resetScreen()}重置
     */
    private int currentPageIndex = 0;
    private void _addInventoryAttachmentButtons(LocalPlayer localPlayer,
                                                IGun iGun, ItemStack gunItem) {
        // 没选中配件类型就不添加
        AttachmentCategory currentCategory = RefitScreenTransformState.get().getCurrentTransformType();
        if (currentCategory == AttachmentCategory.NONE) return;

        // 枪械没启用该配件类型
        if (!iGun.isAttachmentEnabled(gunItem, currentCategory)) return;

        int startX = this.width - screenXMargin
                // 从屏幕左边开始绘制往右绘制，使currentX数值正增
                - CustomTexture.SLOT.getWidth() * (ATTACHMENT_CATEGORIES.length - 2);
        int startY = screenYMargin
                + CustomTexture.SLOT.getHeight()
                + (RefitUnloadButton.unloadButtonYMargin + CustomTexture.UNLOAD.getHeight() / 2 + RefitUnloadButton.unloadButtonYMargin)
                + CustomTexture.TURN_PAGE.getHeight() / 2;
        int currentX = startX
                // 使列表跟类别对齐
                + CustomTexture.SLOT.getWidth() * currentCategory.ordinal();
        int currentY = startY;

        int count = 0;
        // (lastPageEnd, currentPageEnd]
        int lastPageEnd = this.currentPageIndex * INVENTORY_SLOT_COUNT;
        int currentPageEnd = lastPageEnd + INVENTORY_SLOT_COUNT;
        boolean hasNextPage = false;

        Inventory inventory = localPlayer.getInventory();
        List<Integer> targetSlots = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack attachmentItem = inventory.getItem(i);
            @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
            if (iAttachment == null) continue;

            // 不为当前选中的配件类型
            if (iAttachment.getAttachmentCategory(attachmentItem) != currentCategory) continue;

            // 枪械不能安装该配件
            if (!iGun.canInstallAttachment(gunItem, attachmentItem)) continue;

            count++;
            if (count <= lastPageEnd) continue;
            else if (count > currentPageEnd) {
                hasNextPage = true;
                break;
            }

            targetSlots.add(i);
        }

        // currentPageIndex状态检查
        if (this.currentPageIndex > 0 && count <= lastPageEnd) {
            // currentPageIndex是外部不更新的状态，可能存在打开界面时背包物品被清空，导致界面异常(需要重进界面来刷新)
            this.currentPageIndex = 0;
            this._addInventoryAttachmentButtons(localPlayer, iGun, gunItem);
            return; // 此时尚未添加组件，只重新执行当前函数即可，不需要整个Screen reset
        }

        // 背包物品列表
        for (int i = 0; i < targetSlots.size(); i++) {
            int slotIndex = targetSlots.get(i);
            this.addRenderableWidget(new InventoryAttachmentSlot(currentX, currentY, slotIndex, inventory, (_button) -> this._onInstallButtonPress(_button, localPlayer, inventory)));
            currentY += CustomTexture.SLOT.getHeight();
        }

        { // 附加按钮
            // 上一页
            if (this.currentPageIndex > 0) {
                boolean isPreviousPage = true;
                this.addRenderableWidget(new RefitTurnPageButton(currentX, startY - CustomTexture.TURN_PAGE.getHeight() / 2, isPreviousPage, b -> {
                    this.currentPageIndex--;
                    this.resetScreen();
                }));
            }

            // 下一页
            if (hasNextPage) {
                boolean isPreviousPage = false;
                this.addRenderableWidget(new RefitTurnPageButton(currentX, currentY, isPreviousPage, b -> {
                    this.currentPageIndex++;
                    this.resetScreen();
                }));
            }
        }
    }
    private void _onInstallButtonPress(Button _button,
                                       LocalPlayer localPlayer, Inventory inventory) {
        InventoryAttachmentSlot button = (InventoryAttachmentSlot) _button;
        int slotIndex = button.getSlotIndex();

        ItemStack attachmentItem = inventory.getItem(slotIndex);
        SoundPlayManager.get().playerRefitSound(attachmentItem, localPlayer, AttachmentSoundType.INSTALL_SOUND);

        int selectSlot = InventoryUtils.getSelectedSlot(inventory);
        SendUtils.sendMessageToServer(new ClientMessageRefitGun(slotIndex, selectSlot, RefitScreenTransformState.get().getCurrentTransformType()));
    }

    @Override
    public void onClose() {
        this._sendLaserMessage();

        super.onClose();
    }
    private void _sendLaserMessage() {
        LocalPlayer localPlayer = getMinecraft().player;
        if (localPlayer == null) return;

        ItemStack gunItem = localPlayer.getMainHandItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        int selectSlot = InventoryUtils.getSelectedSlot(localPlayer.getInventory());
        for (GuiEventListener child : this.children()) {
            if (!(child instanceof HSVSliderGroup hsvSliderGroup)) continue;

            if (!hsvSliderGroup.isDirty()) continue;

            SendUtils.sendMessageToServer(new ClientMessageLaserColor(gunItem, selectSlot));
        }
    }

    @Override public boolean isPauseScreen() {
        return false;
    }

    // --------IScreen--------

    @Override
    public GunRefitScreen asScreen() {
        return this;
    }

    @Override
    public void resetScreen() {
        this.init();
    }

    @Override
    public void closeScreen() {
        this.onClose();
    }

    // --------Cache--------
    // tacz跟渲染/gui有关的，涉及读ResourceApi的，倾向于是零缓存的设计(免思考打法)，这是待重构的
    // 只不过这里没太大用处

    private boolean isLaserCustomizedColorEnabled = false;
    private void _resetCache(IGun iGun, ItemStack gunItem) {
        this.isLaserCustomizedColorEnabled = false; {
            @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
            if (gunDisplayInstance != null) {
                GunDisplay gunDisplay = gunDisplayInstance.getPojo();
                @Nullable _LaserDisplay laserDisplay = gunDisplay.getLaserDisplay();
                if (laserDisplay != null) {
                    this.isLaserCustomizedColorEnabled = laserDisplay.isEnableCustomizedColor();
                }
            }
        }
    }
    public boolean isLaserCustomizedColorEnabled() {
        return this.isLaserCustomizedColorEnabled;
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true) public int getSlotsTexturesWidth() {
        return CustomTexture.ATTACHMENT_CATEGORIES.getWidth();
    }
}
