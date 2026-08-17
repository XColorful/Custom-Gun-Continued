package dev.xcolorful.customgun.client.gui.tooltip.ammo;

import dev.xcolorful.customgun.client.api.gui.tooltip.BaseTooltipContext;
import dev.xcolorful.customgun.client.api.gui.tooltip.BaseTooltipView;
import dev.xcolorful.customgun.client.api.item.ammo.AmmoTooltipMask;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.resource.assets.info.GunpackInfo;
import dev.xcolorful.customgun.core.api.item.IAmmo;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.gui.tooltip.ammo.AmmoTooltip;
import dev.xcolorful.customgun.core.resource.instance.data.AmmoIndexInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.EnumSet;
import java.util.List;

public class ClientAmmoTooltip implements ClientTooltipComponent {

    public final View view;
    private final Context context;

    public ClientAmmoTooltip(AmmoTooltip ammoTooltip) {
        this.view = new View();
        this.context = new Context(this.view, ammoTooltip);
    }

    // --------ClientTooltipComponent--------

    @Override public int getHeight() {
        return this.context.getHeight();
    }
    @Override public int getWidth(Font font) {
        return this.context.getMaxWidth();
    }
    @Override
    public void renderText(@NotNull Font font,
                           int pX, int pY,
                           @NotNull Matrix4f matrix4f,
                           @NotNull MultiBufferSource.BufferSource bufferSource) {
        int currentY = pY;

        for (AmmoTooltipMask mask : this.context.visibleParts) {
            mask.getTooltipPart().renderText(this.context,
                    font,
                    pX, currentY,
                    matrix4f,
                    bufferSource);

            currentY += mask.getTooltipPart().measureHeight(this.context);
        }
    }
    @Override
    public void renderImage(@NotNull Font font,
                            int pX, int pY,
                            @NotNull GuiGraphics guiGraphics) {
        int currentY = pY;

        for (AmmoTooltipMask mask : this.context.visibleParts) {
            mask.getTooltipPart().renderImage(this.context,
                    font,
                    pX, currentY,
                    guiGraphics);

            currentY += mask.getTooltipPart().measureHeight(this.context);
        }
    }

    // --------record--------

    @ApiStatus.Internal
    public static final class View extends BaseTooltipView {
        public @Nullable List<FormattedCharSequence> desc;
        public @Nullable Component ammoCount;
        public View() {
        }
    }
    @ApiStatus.Internal
    public static final class Context extends BaseTooltipContext<View> {
        public final @NotNull AmmoTooltip ammoTooltip;
        public final @NotNull EnumSet<AmmoTooltipMask> visibleParts;
        public @Nullable AmmoIndexInstance ammoIndexInstance;
        public @Nullable GunpackInfo gunpackInfo;
        public boolean showCategory = false;
        public boolean showPackInfo = false;
        public boolean showPojoLocation = false;
        public Context(@NotNull View view, @NotNull AmmoTooltip ammoTooltip) {
            super(view);
            this.ammoTooltip = ammoTooltip;
            IAmmo iAmmo = ammoTooltip.iAmmo();
            ItemStack ammoItem = ammoTooltip.ammoItem();
            this.visibleParts = AmmoTooltipMask.fromBitmap(iAmmo.getTooltipMask(ammoItem));
            this.ammoIndexInstance = ResourceApi.getAmmoIndexInstance(ammoTooltip.ammoLocation());
            this.gunpackInfo = ClientResourceApi.getGunpackInfo(ammoTooltip.ammoLocation().getNamespace());
            this.buildView();
        }
        @Override
        protected void buildView() {
            for (AmmoTooltipMask mask : this.visibleParts) {
                mask.getTooltipPart().build(this);
            }
        }
        @Override
        protected int calculateHeight() {
            int height = 0;
            for (AmmoTooltipMask mask : this.visibleParts) {
                height += mask.getTooltipPart().measureHeight(this);
            }
            return height;
        }
    }
}
