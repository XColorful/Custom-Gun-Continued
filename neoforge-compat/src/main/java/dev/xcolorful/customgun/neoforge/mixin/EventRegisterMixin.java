package dev.xcolorful.customgun.neoforge.mixin;

import dev.xcolorful.customgun.core.event.EventRegister;
import dev.xcolorful.customgun.neoforge.event.NeoEventRegister;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EventRegister.class, remap = false)
public class EventRegisterMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void customgunneoforge$initializeNeoforgeEventRegister(CallbackInfo ci) {
        EventRegister.initialize(NeoEventRegister.INSTANCE);
    }
}
