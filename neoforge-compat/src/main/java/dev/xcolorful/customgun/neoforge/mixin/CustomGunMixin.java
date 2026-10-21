package dev.xcolorful.customgun.neoforge.mixin;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.ISideExecutor;
import dev.xcolorful.customgun.core.api.init.registry.IRegistrarFactory;
import dev.xcolorful.customgun.core.api.minecraft.ICapabilityProvider;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.minecraft.access.ICoreAccessTransformer;
import dev.xcolorful.customgun.neoforge.common.NeoSideExecutor;
import dev.xcolorful.customgun.neoforge.init.registry.NeoRegistrarFactory;
import dev.xcolorful.customgun.neoforge.minecraft.NeoCapabilityProvider;
import dev.xcolorful.customgun.neoforge.minecraft.NeoRegistry;
import dev.xcolorful.customgun.neoforge.minecraft.access.NeoAccessTransformer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CustomGun.class, remap = false)
public class CustomGunMixin {

    @Inject(method = "getSideExecutor", at = @At("HEAD"), cancellable = true) // cancellable会同时取消原方法执行
    private static void customgunneoforge$getSideExecutor(CallbackInfoReturnable<ISideExecutor> cir) {
        cir.setReturnValue(NeoSideExecutor.INSTANCE);
    }

    @Inject(method = "getRegistrarFactory", at = @At("HEAD"), cancellable = true)
    private static void customgunneoforge$getRegistrarFactory(CallbackInfoReturnable<IRegistrarFactory> cir) {
        cir.setReturnValue(NeoRegistrarFactory.INSTANCE);
    }

    @Inject(method = "getMcRegistry", at = @At("HEAD"), cancellable = true)
    private static void customgunneoforge$getMcRegistry(CallbackInfoReturnable<IMcRegistry> cir) {
        cir.setReturnValue(NeoRegistry.INSTANCE);
    }

    @Inject(method = "getCapabilityProvider", at = @At("HEAD"), cancellable = true)
    private static void customgunneoforge$getCapabilityProvider(CallbackInfoReturnable<ICapabilityProvider> cir) {
        cir.setReturnValue(NeoCapabilityProvider.INSTANCE);
    }

    @Inject(method = "getAccessTransformer", at = @At("HEAD"), cancellable = true)
    private static void customgunneoforge$getAccessTransformer(CallbackInfoReturnable<ICoreAccessTransformer> cir) {
        cir.setReturnValue(NeoAccessTransformer.INSTANCE);
    }
}
