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

package dev.xcolorful.customgun.forgeclient;

import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.input.IKeyMapping;
import dev.xcolorful.customgun.client.api.minecraft.access.IClientAccessTransformer;
import dev.xcolorful.customgun.client.api.minecraft.item.IClientItemExtensionProvider;
import dev.xcolorful.customgun.client.api.minecraft.stencil.IStencilOperator;
import dev.xcolorful.customgun.core.api.event.IEventRegister;
import dev.xcolorful.customgun.forgeclient.event.ForgeClientEventRegister;
import dev.xcolorful.customgun.forgeclient.input.ForgeKeyMapping;
import dev.xcolorful.customgun.forgeclient.item.ForgeClientItemExtensionProvider;
import dev.xcolorful.customgun.forgeclient.minecraft.access.ForgeClientAccessTransformer;
import dev.xcolorful.customgun.forgeclient.minecraft.stencil.ForgeStencilOperator;

public class CustomGunForgeClient {

    protected static boolean initialized;
    public static IEventRegister eventRegister;
    public static IKeyMapping.Creator keyMappingCreator;
    public static IClientAccessTransformer accessTransformer;
    public static IClientItemExtensionProvider clientItemExtensionProvider;
    public static IStencilOperator stencilOperator;

    public static void init() {
        if (initialized) return;
        CustomGunForgeClient.eventRegister = new ForgeClientEventRegister();
        CustomGunForgeClient.keyMappingCreator = new ForgeKeyMapping.Creator();
        CustomGunForgeClient.accessTransformer = new ForgeClientAccessTransformer();
        CustomGunForgeClient.clientItemExtensionProvider = new ForgeClientItemExtensionProvider();
        CustomGunForgeClient.stencilOperator = new ForgeStencilOperator();

        CustomGunClient.init(CustomGunForgeClient.keyMappingCreator,
                CustomGunForgeClient.accessTransformer,
                CustomGunForgeClient.clientItemExtensionProvider,
                CustomGunForgeClient.stencilOperator);
        initialized = true;
    }
}
