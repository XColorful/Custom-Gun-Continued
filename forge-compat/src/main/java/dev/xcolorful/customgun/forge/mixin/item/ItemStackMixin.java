package dev.xcolorful.customgun.forge.mixin.item;

import dev.xcolorful.customgun.core.api.item.IAmmo;
import dev.xcolorful.customgun.core.api.item.ammo.IAmmoGetter;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * <ul>
 *     <li>Forge 1.21.1 移除了 {@code IForgeItem#getMaxStackSize(ItemStack)}</li>
 *     <li>而 {@link ItemStack#getMaxStackSize()} 改为直接读取 {@link net.minecraft.core.component.DataComponents#MAX_STACK_SIZE} 组件，不再经过 Item</li>
 *     <li>NeoForge 通过 {@code IItemExtension#getMaxStackSize(ItemStack)} 保留了该间接调用</li>
 *     <li>故本 mixin 仅在 Forge 侧补齐</li>
 * </ul>
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void cgc$getMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        @Nullable IAmmo iAmmo = IAmmoGetter.fromItemStack(self);
        if (iAmmo != null) cir.setReturnValue(iAmmo.getAmmoMaxStackSize(self));
    }
}
