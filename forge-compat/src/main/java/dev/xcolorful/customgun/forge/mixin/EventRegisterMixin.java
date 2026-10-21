package dev.xcolorful.customgun.forge.mixin;

import dev.xcolorful.customgun.core.event.EventRegister;
import dev.xcolorful.customgun.forge.event.ForgeEventRegister;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EventRegister.class, remap = false)
public class EventRegisterMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void customgunforge$initializeForgeEventRegister(CallbackInfo ci) {
        EventRegister.initialize(ForgeEventRegister.INSTANCE);
    }
}
