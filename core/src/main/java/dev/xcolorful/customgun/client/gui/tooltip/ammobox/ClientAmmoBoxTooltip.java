/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.ammobox;

import dev.xcolorful.customgun.client.api.gui.tooltip.BaseTooltipContext;
import dev.xcolorful.customgun.client.api.gui.tooltip.BaseTooltipView;
import dev.xcolorful.customgun.client.api.item.ammobox.AmmoBoxTooltipMask;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.resource.assets.info.GunpackInfo;
import dev.xcolorful.customgun.core.api.item.IAmmoBox;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.gui.tooltip.ammobox.AmmoBoxTooltip;
import dev.xcolorful.customgun.core.resource.instance.data.AmmoIndexInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class ClientAmmoBoxTooltip implements ClientTooltipComponent {

    public final View view;
    private final Context context;

    public ClientAmmoBoxTooltip(AmmoBoxTooltip ammoBoxTooltip) {
        this.view = new View();
        this.context = new Context(this.view, ammoBoxTooltip);
    }

    // --------ClientTooltipComponent--------

    @Override public int getHeight(Font font) {
        return this.context.getHeight();
    }
    @Override public int getWidth(@NotNull Font font) {
        return this.context.getMaxWidth();
    }
    @Override
    public void renderText(@NotNull Font font,
                           int pX, int pY) {
        int currentX = pX;
        int currentY = pY;

        for (AmmoBoxTooltipMask mask : this.context.visibleParts) {
            mask.getTooltipPart().renderText(this.context,
                    font,
                    currentX, currentY,
                    matrix4f,
                    bufferSource);

            currentY += mask.getTooltipPart().measureHeight(this.context);
        }
    }
    @Override
    public void renderImage(@NotNull Font font,
                            int pX, int pY,
                            int width, int height,
                            @NotNull GuiGraphics guiGraphics) {
        int currentX = pX;
        int currentY = pY;

        for (AmmoBoxTooltipMask mask : this.context.visibleParts) {
            mask.getTooltipPart().renderImage(this.context,
                    font,
                    currentX, currentY,
                    guiGraphics);

            currentY += mask.getTooltipPart().measureHeight(this.context);
        }
    }

    // --------record--------

    @ApiStatus.Internal
    public static final class View extends BaseTooltipView {
        public @Nullable List<FormattedCharSequence> desc;
        public @Nullable Component ammoName;
        public @Nullable Component ammoCount;
        public View() {
        }
    }
    @ApiStatus.Internal
    public static final class Context extends BaseTooltipContext<View> {
        public final AmmoBoxTooltip ammoBoxTooltip;
        public final @NotNull EnumSet<AmmoBoxTooltipMask> visibleParts;
        public @Nullable AmmoIndexInstance ammoIndexInstance;
        public @Nullable GunpackInfo gunpackInfo;
        public boolean showAmmo = false;
        public boolean showCategory = false;
        public boolean showPackInfo = false;
        public boolean showPojoLocation = false;
        public Context(@NotNull View view, AmmoBoxTooltip ammoBoxTooltip) {
            super(view);
            this.ammoBoxTooltip = ammoBoxTooltip;
            IAmmoBox iAmmoBox = ammoBoxTooltip.iAmmoBox();
            ItemStack ammoBoxItem = ammoBoxTooltip.ammoBoxItem();
            this.visibleParts = AmmoBoxTooltipMask.fromBitmap(iAmmoBox.getTooltipMask(ammoBoxItem));
            this.ammoIndexInstance = ResourceApi.getAmmoIndexInstance(ammoBoxTooltip.ammoLocation());
            this.gunpackInfo = ClientResourceApi.getGunpackInfo(ammoBoxTooltip.ammoLocation().getNamespace());
            this.buildView();
        }
        @Override
        protected void buildView() {
            for (AmmoBoxTooltipMask mask : this.visibleParts) {
                mask.getTooltipPart().build(this);
            }
        }
        @Override
        protected int calculateHeight() {
            int height = 0;
            for (AmmoBoxTooltipMask mask : this.visibleParts) {
                height += mask.getTooltipPart().measureHeight(this);
            }
            return height;
        }
    }
}
