package dev.xcolorful.customgun.client.api.renderer.item;

import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * <ul>
 *     自 26.1 起
 *     <li>{@code SpecialModelRenderer#submit} 不再接收 {@link ItemDisplayContext}</li>
 *     <li>由 {@code ItemStackRenderStateMixin} 在 {@code ItemStackRenderState.submit} 时把当前显示上下文暂存到 ThreadLocal</li>
 *     <li>供 {@code NeoBEWLR} 取回后桥接给 {@code renderByItem}</li>
 * </ul>
 */
@ApiStatus.AvailableSince("26.1")
@ApiStatus.Internal
public class ItemDisplayContextTracker {

    private ItemDisplayContextTracker() {}

    private static final ThreadLocal<ItemDisplayContext> CURRENT = new ThreadLocal<>();

    public static void push(ItemDisplayContext context) {
        CURRENT.set(context);
    }

    public static void pop() {
        CURRENT.remove();
    }

    public static ItemDisplayContext current() {
        ItemDisplayContext context = CURRENT.get();
        return context != null ? context : ItemDisplayContext.NONE;
    }
}
