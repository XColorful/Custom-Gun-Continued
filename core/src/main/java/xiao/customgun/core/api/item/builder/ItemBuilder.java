package xiao.customgun.core.api.item.builder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.util.NBTUtils;

public class ItemBuilder<T extends ItemBuilder<T>> {

    protected final ItemStack itemStack;

    protected ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
    public static ItemBuilder<?> create(ItemLike item) {
        ItemStack itemStack = new ItemStack(item);
        return new ItemBuilder<>(itemStack);
    }

    @SuppressWarnings("unchecked")
    public T setCustomDataTag(@Nullable CompoundTag nbt) {
        NBTUtils.setCustomDataTag(this.itemStack, nbt);
        return (T) this;
    }

    /**
     * 不复制 ItemStack
     */
    public final ItemStack build() {
        return this.itemStack;
    }
    public final ItemStack buildCopy() {
        return this.build().copy();
    }
}
