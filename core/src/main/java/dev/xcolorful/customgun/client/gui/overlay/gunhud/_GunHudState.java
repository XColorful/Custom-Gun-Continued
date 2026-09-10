package dev.xcolorful.customgun.client.gui.overlay.gunhud;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class _GunHudState {


    private volatile boolean messageConsumed = true;
    private volatile @Nullable Component pendingMessage;

    protected  _GunHudState() {}

    public void reset() {
        this.messageConsumed = true;
        this.pendingMessage = null;
    }

    public static final String SPECIAL_PREFIX = "  ";
    public static final String SPECIAL_SUFFIX = "  ";
    /**
     * <ul>
     *     貌似MC始终渲染不可见字符:
     *     <li>零宽空格 \u200B 会渲染</li>
     *     <li>word joiner \u2060 会渲染</li>
     * </ul>
     * 判断方式改成前后都是两个空格，正常应该没有action bar会故意这么搞
     */
    public boolean isOverwritableMessage(@Nullable Component message) {
        if (message == null || message == this.pendingMessage) return true;

        String s = message.getString();
        return s.startsWith(SPECIAL_PREFIX) && s.endsWith(SPECIAL_SUFFIX);
    }

    // --------Getter & Setter--------

    public boolean isMessageConsumed() {
        return this.messageConsumed;
    }
    public @Nullable Component getPendingMessage() {
        return this.pendingMessage;
    }

    public void setConsumed() {
        this.messageConsumed = true;
    }
    public void setPendingMessage(@Nullable Component message) {
        if (message == null) {
            this.pendingMessage = null;
            return;
        }

        this.pendingMessage = Component.literal(SPECIAL_PREFIX)
                .append(message)
                .append(Component.literal(SPECIAL_SUFFIX));
        this.messageConsumed = false;
    }
}
