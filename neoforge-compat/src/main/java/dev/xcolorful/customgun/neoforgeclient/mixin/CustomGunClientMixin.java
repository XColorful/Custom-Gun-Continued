package dev.xcolorful.customgun.neoforgeclient.mixin;

import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.input.IKeyMapping;
import dev.xcolorful.customgun.client.api.minecraft.access.IClientAccessTransformer;
import dev.xcolorful.customgun.client.api.minecraft.item.IClientItemExtensionProvider;
import dev.xcolorful.customgun.client.api.minecraft.stencil.IStencilOperator;
import dev.xcolorful.customgun.neoforgeclient.input.NeoKeyMapping;
import dev.xcolorful.customgun.neoforgeclient.item.NeoClientItemExtensionProvider;
import dev.xcolorful.customgun.neoforgeclient.minecraft.access.NeoClientAccessTransformer;
import dev.xcolorful.customgun.neoforgeclient.minecraft.stencil.NeoStencilOperator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CustomGunClient.class, remap = false)
public class CustomGunClientMixin {

    @Inject(method = "getKeyMappingCreator", at = @At("HEAD"), cancellable = true)
    private static void customgunneoforge$getKeyMappingCreator(CallbackInfoReturnable<IKeyMapping.Creator> cir) {
        cir.setReturnValue(NeoKeyMapping.Creator.INSTANCE);
    }

    @Inject(method = "getAccessTransformer", at = @At("HEAD"), cancellable = true)
    private static void customgunneoforge$getAccessTransformer(CallbackInfoReturnable<IClientAccessTransformer> cir) {
        cir.setReturnValue(NeoClientAccessTransformer.INSTANCE);
    }

    @Inject(method = "getClientItemExtensionProvider", at = @At("HEAD"), cancellable = true)
    private static void customgunneoforge$getClientItemExtensionProvider(CallbackInfoReturnable<IClientItemExtensionProvider> cir) {
        cir.setReturnValue(NeoClientItemExtensionProvider.INSTANCE);
    }

    @Inject(method = "getStencilOperator", at = @At("HEAD"), cancellable = true)
    private static void customgunneoforge$getStencilOperator(CallbackInfoReturnable<IStencilOperator> cir) {
        cir.setReturnValue(NeoStencilOperator.INSTANCE);
    }
}
