package dev.xcolorful.customgun.client.api.gui.screen;

import net.minecraft.client.gui.screens.Screen;

/**
 * <ul>
 *     以下内容没地方写，但不应作为“模组开发老人”的常识，所以列出
 *     <li>MC Screen的Y轴正方向是往下的，每换一行就正增y，往右就正增x</li>
 *     <li>大x是指在屏幕右边，减小x是往屏幕左边</li>
 *     <li>{@link Screen#width}是最大x坐标</li>
 * </ul>
 */
public interface IScreen<T extends Screen> {

    T asScreen();

    void resetScreen();

    void closeScreen();
}
