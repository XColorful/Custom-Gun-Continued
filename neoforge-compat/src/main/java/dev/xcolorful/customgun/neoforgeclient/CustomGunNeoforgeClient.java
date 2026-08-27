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

package dev.xcolorful.customgun.neoforgeclient;

import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.input.IKeyMapping;
import dev.xcolorful.customgun.client.api.minecraft.access.IClientAccessTransformer;
import dev.xcolorful.customgun.client.api.minecraft.item.IClientItemExtensionProvider;
import dev.xcolorful.customgun.client.api.minecraft.stencil.IStencilOperator;
import dev.xcolorful.customgun.core.api.event.IEventRegister;
import dev.xcolorful.customgun.neoforgeclient.event.NeoClientEventRegister;
import dev.xcolorful.customgun.neoforgeclient.input.NeoKeyMapping;
import dev.xcolorful.customgun.neoforgeclient.item.NeoClientItemExtensionProvider;
import dev.xcolorful.customgun.neoforgeclient.minecraft.access.NeoClientAccessTransformer;
import dev.xcolorful.customgun.neoforgeclient.minecraft.stencil.NeoStencilOperator;

public class CustomGunNeoforgeClient {

    protected static boolean initialized;
    public static IEventRegister eventRegister;
    public static IKeyMapping.Creator keyMappingCreator;
    public static IClientAccessTransformer accessTransformer;
    public static IClientItemExtensionProvider clientItemExtensionProvider;
    public static IStencilOperator stencilOperator;

    public static void init() {
        if (initialized) return;
        CustomGunNeoforgeClient.eventRegister = new NeoClientEventRegister();
        CustomGunNeoforgeClient.keyMappingCreator = new NeoKeyMapping.Creator();
        CustomGunNeoforgeClient.accessTransformer = new NeoClientAccessTransformer();
        CustomGunNeoforgeClient.clientItemExtensionProvider = new NeoClientItemExtensionProvider();
        CustomGunNeoforgeClient.stencilOperator = new NeoStencilOperator();

        CustomGunClient.init(CustomGunNeoforgeClient.keyMappingCreator,
                CustomGunNeoforgeClient.accessTransformer,
                CustomGunNeoforgeClient.clientItemExtensionProvider,
                CustomGunNeoforgeClient.stencilOperator);
        initialized = true;
    }
}
