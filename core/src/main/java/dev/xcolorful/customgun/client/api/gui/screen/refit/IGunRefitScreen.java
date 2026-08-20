/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.gui.screen.refit;

import dev.xcolorful.customgun.client.api.gui.screen.IScreen;
import dev.xcolorful.customgun.client.gui.screen.GunRefitScreen;
import net.minecraft.client.gui.screens.Screen;

/**
 * <ul>
 *     <li>原模组的实现跟模组内其他地方强耦合，包含instanceof验证</li>
 *     <li>即扩展模组要么完全自己做，要么必须继承{@link GunRefitScreen}来维持instanceof兼容性，否则不共享一些状态</li>
 *     <li>目前给扩展模组提供了Mixin注入点{@link GunRefitScreen#create()}，返回类型只需要是该接口(更宽松)</li>
 * </ul>
 */
public interface IGunRefitScreen<T extends Screen> extends IScreen<T> {
}
