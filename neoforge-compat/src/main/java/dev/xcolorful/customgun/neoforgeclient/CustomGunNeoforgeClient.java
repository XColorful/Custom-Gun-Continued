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
    public static IEventRegister eventRegister = NeoClientEventRegister.INSTANCE;
    public static IKeyMapping.Creator keyMappingCreator = NeoKeyMapping.Creator.INSTANCE;
    public static IClientAccessTransformer accessTransformer = NeoClientAccessTransformer.INSTANCE;
    public static IClientItemExtensionProvider clientItemExtensionProvider = NeoClientItemExtensionProvider.INSTANCE;
    public static IStencilOperator stencilOperator = NeoStencilOperator.INSTANCE;

    public static void init() {
        if (initialized) return;
//        CustomGunNeoforgeClient.eventRegister = NeoClientEventRegister.INSTANCE;
//        CustomGunNeoforgeClient.keyMappingCreator = NeoKeyMapping.Creator.INSTANCE;
//        CustomGunNeoforgeClient.accessTransformer = NeoClientAccessTransformer.INSTANCE;
//        CustomGunNeoforgeClient.clientItemExtensionProvider = NeoClientItemExtensionProvider.INSTANCE;
//        CustomGunNeoforgeClient.stencilOperator = NeoStencilOperator.INSTANCE;

        CustomGunClient.init(CustomGunNeoforgeClient.keyMappingCreator,
                CustomGunNeoforgeClient.accessTransformer,
                CustomGunNeoforgeClient.clientItemExtensionProvider,
                CustomGunNeoforgeClient.stencilOperator);
        initialized = true;
    }
}
