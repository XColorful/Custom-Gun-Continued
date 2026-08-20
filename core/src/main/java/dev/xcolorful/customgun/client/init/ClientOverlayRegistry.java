package dev.xcolorful.customgun.client.init;

import dev.xcolorful.customgun.client.api.event.IRenderGuiEvent;

/**
 * @deprecated
 * <ul>
 *     <li>注册forge gui不能随时取消(需要手动维护boolean)，不够灵活</li>
 *     <li>{@code net.minecraftforge.client.gui.overlay.IGuiOverlay}相当于再次做一个{@link IRenderGuiEvent}等价的平台抽象</li>
 *     <li>{@code net.minecraftforge.client.gui.overlay.ForgeGui#render}先处理原版+模组注册GUI，注册仅提供排序功能</li>
 * </ul>
 */
@Deprecated(forRemoval = true)
public class ClientOverlayRegistry {

    private static ClientOverlayRegistry INSTANCE = new ClientOverlayRegistry();
    public static ClientOverlayRegistry get() {
        return INSTANCE;
    }
    private ClientOverlayRegistry() {}

    public void registerGuiOverlays() {
    }
}
