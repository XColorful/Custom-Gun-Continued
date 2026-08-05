/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.client;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.input.IInputKeyManager;
import dev.xcolorful.customgun.client.api.input.IKeyMapping;
import dev.xcolorful.customgun.client.api.minecraft.access.IClientAccessTransformer;
import dev.xcolorful.customgun.client.event.custom.ClientEventHandlers;
import dev.xcolorful.customgun.client.init.ClientModConfig;
import dev.xcolorful.customgun.client.input.InputKeyManager;
import org.jetbrains.annotations.NotNull;

public class CustomGunClient {

    protected static boolean initialized;
    private static IKeyMapping.Creator keyMappingCreator;
    private static IClientAccessTransformer accessTransformer;

    public static void init(IKeyMapping.Creator keyMappingCreator,
                            IClientAccessTransformer accessTransformer) {
        if (initialized) return;
        CustomGunClient.keyMappingCreator = keyMappingCreator;
        CustomGunClient.accessTransformer = accessTransformer;

        ClientModConfig.init();

        setInputKeyManagerInternal(InputKeyManager.INSTANCE);
        InputKeyManager.init(CustomGun.getMcSide());

        ClientEventHandlers.registerAll(CustomGun.getEventRegister());
        initialized = true;
    }

    public static IKeyMapping.Creator getKeyMappingCreator() {
        return keyMappingCreator;
    }
    public static IClientAccessTransformer getAccessTransformer() {
        return accessTransformer;
    }

    private static IInputKeyManager inputKeyManager;
    public static IInputKeyManager getInputKeyManager() {
        return CustomGunClient.inputKeyManager;
    }
    /**
     * @deprecated 除非需要深度定制, 否则不应该调用
     */
    @Deprecated(forRemoval = false)
    public static void setInputKeyManager(IInputKeyManager inputKeyManager) {
        setInputKeyManagerInternal(inputKeyManager);
    }

    // 跟IInpuKeySubManager同样的机制
    private static void setInputKeyManagerInternal(@NotNull IInputKeyManager inputKeyManager) {
        if (CustomGunClient.inputKeyManager != null) CustomGunClient.inputKeyManager.unregisterEventHandler();
        CustomGunClient.inputKeyManager = inputKeyManager;
        CustomGunClient.inputKeyManager.registerEventHandler();
    }
}
