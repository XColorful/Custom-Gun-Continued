package dev.xcolorful.customgun.forge.mixin;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.ISideExecutor;
import dev.xcolorful.customgun.core.api.init.registry.IRegistrarFactory;
import dev.xcolorful.customgun.core.api.minecraft.ICapabilityProvider;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.minecraft.access.ICoreAccessTransformer;
import dev.xcolorful.customgun.forge.common.ForgeSideExecutor;
import dev.xcolorful.customgun.forge.init.registry.ForgeRegistrarFactory;
import dev.xcolorful.customgun.forge.minecraft.ForgeCapabilityProvider;
import dev.xcolorful.customgun.forge.minecraft.ForgeRegistry;
import dev.xcolorful.customgun.forge.minecraft.access.ForgeAccessTransformer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CustomGun.class, remap = false)
public class CustomGunMixin {

    @Inject(method = "getSideExecutor", at = @At("HEAD"), cancellable = true) // cancellable会同时取消原方法执行
    private static void customgunforge$getSideExecutor(CallbackInfoReturnable<ISideExecutor> cir) {
        cir.setReturnValue(ForgeSideExecutor.INSTANCE);
    }

    @Inject(method = "getRegistrarFactory", at = @At("HEAD"), cancellable = true)
    private static void customgunforge$getRegistrarFactory(CallbackInfoReturnable<IRegistrarFactory> cir) {
        cir.setReturnValue(ForgeRegistrarFactory.INSTANCE);
    }

    @Inject(method = "getMcRegistry", at = @At("HEAD"), cancellable = true)
    private static void customgunforge$getMcRegistry(CallbackInfoReturnable<IMcRegistry> cir) {
        cir.setReturnValue(ForgeRegistry.INSTANCE);
    }

    @Inject(method = "getCapabilityProvider", at = @At("HEAD"), cancellable = true)
    private static void customgunforge$getCapabilityProvider(CallbackInfoReturnable<ICapabilityProvider> cir) {
        cir.setReturnValue(ForgeCapabilityProvider.INSTANCE);
    }

    @Inject(method = "getAccessTransformer", at = @At("HEAD"), cancellable = true)
    private static void customgunforge$getAccessTransformer(CallbackInfoReturnable<ICoreAccessTransformer> cir) {
        cir.setReturnValue(ForgeAccessTransformer.INSTANCE);
    }
}
