package dev.xcolorful.customgun.client.api.gui.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class BaseTooltipContext<T extends BaseTooltipView> {

    /*
    原版是能在玩家悬浮在物品tooltip，且查看期间切换创造/非创造的时候动态更新，并且还需要鼠标位置跟改成创造模式后的物品栏位置重合才能做到
    鼠标移开后会重新创建(刷新)tooltip
    目前没调查渲染线程实时读是否会触发CME
     */
    public final boolean isAdvanced;
    public final boolean isCreative;

    public final @NotNull T view;
    private int maxWidth = 0;
    public BaseTooltipContext(@NotNull T view) {
        {
            Minecraft mc = Minecraft.getInstance();

            this.isAdvanced = mc.options.advancedItemTooltips;

            LocalPlayer localPlayer = mc.player;
            this.isCreative = localPlayer != null && localPlayer.isCreative();
        }

        this.view = view;
    }

    protected abstract void buildView();

    protected abstract int calculateHeight();

    // --------Getter & Setter--------

    public final int getHeight() {
        return this.calculateHeight();
    }
    public final int getMaxWidth() {
        return this.maxWidth;
    }

    public final void widenMaxWidth(int maxWidth) {
        this.maxWidth = Math.max(this.maxWidth, maxWidth);
    }
}
