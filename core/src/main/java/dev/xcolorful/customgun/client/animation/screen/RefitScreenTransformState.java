/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.screen;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.event.IPrepareRenderFrameEvent;
import dev.xcolorful.customgun.client.api.gui.screen.refit.IGunRefitScreen;
import dev.xcolorful.customgun.client.util.ClientGuiUtils;
import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEvent;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * 装配界面{@link IGunRefitScreen}的动画插值
 * <ul>
 *     <li>这个类维护的{@link #getCurrentTransformType}已经作为单例状态给{@link IGunRefitScreen}用了</li>
 * </ul>
 */
public class RefitScreenTransformState implements IEventHandler {
    private static class RenderTransformHolder {
        private static final RefitScreenTransformState INSTANCE = new RefitScreenTransformState();
    }
    public static RefitScreenTransformState get() {
        return RenderTransformHolder.INSTANCE;
    }
    protected RefitScreenTransformState() {}

    static { // 首次调用时自动监听
        CustomGun.getEventRegister().register(get(), EventType.PREPARE_RENDER_FRAME_EVENT, EventPriority.NORMAL, false);
    }

    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        if (eventType == EventType.PREPARE_RENDER_FRAME_EVENT) {
            onPrepareRenderFrame((IPrepareRenderFrameEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    @ApiStatus.Internal
    public static long REFIT_SCREEN_TRANSFORM_TIME_MS = (long) (0.25f * 1000);

    private float screenTransformProgress = 1;
    private long screenTransformTimestamp = -1;
    private @NotNull AttachmentCategory oldTransformType = AttachmentCategory.NONE;
    private @NotNull AttachmentCategory currentTransformType = AttachmentCategory.NONE;

    private float screenOpeningProgress = 0;
    private long screenOpeningTimestamp = -1;

    public void reset() {
        screenTransformProgress = 1;
        screenTransformTimestamp = System.currentTimeMillis();
        oldTransformType = AttachmentCategory.NONE;
        currentTransformType = AttachmentCategory.NONE;
    }

    private void onPrepareRenderFrame(IPrepareRenderFrameEvent event) {
        long currentTimeMillis = System.currentTimeMillis();

        Minecraft mc = Minecraft.getInstance();

        { // tick opening progress
            // 初始化
            if (this.screenOpeningTimestamp == -1) this.screenOpeningTimestamp = currentTimeMillis;

            if (ClientGuiUtils.getCurrentScreen(mc) instanceof IGunRefitScreen) {
                // 当前是装配screen
                if (this.screenOpeningProgress < 1) {
                    this.screenOpeningProgress += (float) (currentTimeMillis - this.screenOpeningTimestamp) / REFIT_SCREEN_TRANSFORM_TIME_MS;
                }
                if (this.screenOpeningProgress > 0.99) this.screenOpeningProgress = 1;
            } else {
                if (this.screenOpeningProgress > 0) {
                    this.screenOpeningProgress -= (float) (currentTimeMillis - this.screenOpeningTimestamp) / REFIT_SCREEN_TRANSFORM_TIME_MS;
                }
                if (this.screenOpeningProgress < 0.01) this.screenOpeningProgress = 0;
            }
        }
        this.screenOpeningTimestamp = currentTimeMillis;

        { // tick transform progress
            // 初始化
            if (this.screenTransformTimestamp == -1) this.screenTransformTimestamp = currentTimeMillis;

            if (this.screenTransformProgress < 1) {
                this.screenTransformProgress += (float) (currentTimeMillis - this.screenTransformTimestamp) / REFIT_SCREEN_TRANSFORM_TIME_MS;
            }
            if (this.screenTransformProgress > 0.99) this.screenTransformProgress = 1;
        }
        this.screenTransformTimestamp = currentTimeMillis;
    }

    public boolean changeRefitScreenView(@NotNull AttachmentCategory attachmentType) {
        if (this.screenTransformProgress != 1 || this.screenOpeningProgress != 1) {
            return false;
        }
        this.oldTransformType = this.currentTransformType;
        this.currentTransformType = attachmentType;
        this.screenTransformProgress = 0;
        this.screenTransformTimestamp = System.currentTimeMillis();
        return true;
    }

    // --------Getter--------

    public float getTransformProgress() {
        return this.screenTransformProgress;
    }
    public @NotNull AttachmentCategory getOldTransformType() {
        return this.oldTransformType;
    }
    public @NotNull AttachmentCategory getCurrentTransformType() {
        return this.currentTransformType;
    }
    public float getOpeningProgress() {
        return this.screenOpeningProgress;
    }
}
