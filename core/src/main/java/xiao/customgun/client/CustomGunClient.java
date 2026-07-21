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

package xiao.customgun.client;

import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.input.IInputKeyManager;
import xiao.customgun.client.api.input.IKeyMapping;
import xiao.customgun.client.event.custom.ClientEventHandlers;
import xiao.customgun.client.init.ClientModConfig;
import xiao.customgun.client.input.InputKeyManager;

public class CustomGunClient {

    protected static boolean initialized;
    private static IKeyMapping.Creator keyMappingCreator;

    public static void init(IKeyMapping.Creator keyMappingCreator) {
        if (initialized) return;
        CustomGunClient.keyMappingCreator = keyMappingCreator;

        ClientModConfig.init();

        setInputKeyManagerInternal(InputKeyManager.INSTANCE);
        InputKeyManager.init(CustomGun.getMcSide());

        ClientEventHandlers.registerAll(CustomGun.getEventRegister());
        initialized = true;
    }

    public static IKeyMapping.Creator getKeyMappingCreator() {
        return keyMappingCreator;
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
