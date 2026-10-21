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
    public static IEventRegister eventRegister = ForgeClientEventRegister.INSTANCE;
    public static IKeyMapping.Creator keyMappingCreator = ForgeKeyMapping.Creator.INSTANCE;
    public static IClientAccessTransformer accessTransformer = ForgeClientAccessTransformer.INSTANCE;
    public static IClientItemExtensionProvider clientItemExtensionProvider = ForgeClientItemExtensionProvider.INSTANCE;
    public static IStencilOperator stencilOperator = ForgeStencilOperator.INSTANCE;

    public static void init() {
        if (initialized) return;
//        CustomGunForgeClient.eventRegister = ForgeClientEventRegister.INSTANCE;
//        CustomGunForgeClient.keyMappingCreator = ForgeKeyMapping.Creator.INSTANCE;
//        CustomGunForgeClient.accessTransformer = ForgeClientAccessTransformer.INSTANCE;
//        CustomGunForgeClient.clientItemExtensionProvider = ForgeClientItemExtensionProvider.INSTANCE;
//        CustomGunForgeClient.stencilOperator = ForgeStencilOperator.INSTANCE;

        CustomGunClient.init(CustomGunForgeClient.keyMappingCreator,
                CustomGunForgeClient.accessTransformer,
                CustomGunForgeClient.clientItemExtensionProvider,
                CustomGunForgeClient.stencilOperator);
        initialized = true;
    }
}
