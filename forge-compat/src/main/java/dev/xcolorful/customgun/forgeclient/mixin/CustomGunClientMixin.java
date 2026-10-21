package dev.xcolorful.customgun.forgeclient.mixin;

import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.input.IKeyMapping;
import dev.xcolorful.customgun.client.api.minecraft.access.IClientAccessTransformer;
import dev.xcolorful.customgun.client.api.minecraft.item.IClientItemExtensionProvider;
import dev.xcolorful.customgun.client.api.minecraft.stencil.IStencilOperator;
import dev.xcolorful.customgun.forgeclient.input.ForgeKeyMapping;
import dev.xcolorful.customgun.forgeclient.item.ForgeClientItemExtensionProvider;
import dev.xcolorful.customgun.forgeclient.minecraft.access.ForgeClientAccessTransformer;
import dev.xcolorful.customgun.forgeclient.minecraft.stencil.ForgeStencilOperator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CustomGunClient.class, remap = false)
public class CustomGunClientMixin {

    @Inject(method = "getKeyMappingCreator", at = @At("HEAD"), cancellable = true)
    private static void customgunforge$getKeyMappingCreator(CallbackInfoReturnable<IKeyMapping.Creator> cir) {
        cir.setReturnValue(ForgeKeyMapping.Creator.INSTANCE);
    }

    @Inject(method = "getAccessTransformer", at = @At("HEAD"), cancellable = true)
    private static void customgunforge$getAccessTransformer(CallbackInfoReturnable<IClientAccessTransformer> cir) {
        cir.setReturnValue(ForgeClientAccessTransformer.INSTANCE);
    }

    @Inject(method = "getClientItemExtensionProvider", at = @At("HEAD"), cancellable = true)
    private static void customgunforge$getClientItemExtensionProvider(CallbackInfoReturnable<IClientItemExtensionProvider> cir) {
        cir.setReturnValue(ForgeClientItemExtensionProvider.INSTANCE);
    }

    @Inject(method = "getStencilOperator", at = @At("HEAD"), cancellable = true)
    private static void customgunforge$getStencilOperator(CallbackInfoReturnable<IStencilOperator> cir) {
        cir.setReturnValue(ForgeStencilOperator.INSTANCE);
    }
}
