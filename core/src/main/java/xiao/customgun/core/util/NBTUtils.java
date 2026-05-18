/*
 * 基于 BattleRoyale GameIdHelper 相同的封装目的
 * 只不过 BattleRoyale 已经打通版本差异而没有必要专门做一个NBTUtils (只需要操作 GameId，所以没地方用)
 */

package xiao.customgun.core.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NBTUtils {

    /**
     * 获取 DataComponents.CUSTOM_DATA
     */
    public static @Nullable CompoundTag getCustomData(@Nullable ItemStack itemStack) {
        if (itemStack == null) return null;
        else return itemStack.getTag(); // itemStack.getOrDefault(DataComponents.CUSTOM_DATA, null);
    }
    public static @NotNull CompoundTag getOrCreateCustomData(@NotNull ItemStack itemStack) {
        return itemStack.getOrCreateTag(); // itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag));
    }
    /**
     * 写入 DataComponents.CUSTOM_DATA
     */
    public static void setCustomData(@NotNull ItemStack itemStack, CompoundTag customData) {
        itemStack.setTag(customData); // itemStack.set(DataComponents.CUSTOM_DATA, customData);
    }
    /**
     * 将 CompoundTag 写入 DataComponents.CUSTOM_DATA
     */
    public static void setCustomDataTag(@NotNull ItemStack itemStack, @Nullable CompoundTag nbt) {
        itemStack.setTag(nbt); // itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @ApiStatus.AvailableSince("1.21.1")
    public static @NotNull CompoundTag getCustomDataTag(@NotNull CompoundTag customData) {
        return customData; // customData.copyTag()
    }

    // --------ItemStack--------
    // (便利接口) 从CUSTOM_DATA根目录获取数据
    // Setter接口包含存储

    public static @Nullable String getString(@Nullable ItemStack itemStack, String key) {
        var customData = getCustomData(itemStack);
        return customData != null ? getString(getCustomDataTag(customData), key) : null;
    }
    public static void setString(@NotNull ItemStack itemStack, String key, @Nullable String value) {
        var customData = getOrCreateCustomData(itemStack);
        CompoundTag customDataTag = getCustomDataTag(customData);
        setString(customDataTag, key, value);
        setCustomDataTag(itemStack, customDataTag);
    }

    public static @Nullable CompoundTag getCompoundTag(@Nullable ItemStack itemStack, String key) {
        var customData = getCustomData(itemStack);
        return customData != null ? getCompoundTag(getCustomDataTag(customData), key) : null;
    }
    public static void setCompoundTag(@NotNull ItemStack itemStack, String key, @Nullable CompoundTag value) {
        var customData = getOrCreateCustomData(itemStack);
        CompoundTag customDataTag = getCustomDataTag(customData);
        setCompoundTag(customDataTag, key, value);
        setCustomDataTag(itemStack, customDataTag);
    }

    public static void removeKey(@NotNull ItemStack itemStack, String key) {
        var customData = getOrCreateCustomData(itemStack);
        CompoundTag customDataTag = getCustomDataTag(customData);
        removeKey(customDataTag, key);
        setCustomDataTag(itemStack, customDataTag);
    }

    // --------CompoundTag--------
    // 直接操作NBT

    public static @Nullable String getString(@Nullable CompoundTag nbt, String key) {
        if (nbt == null) return null;
        else if (nbt.contains(key)) return nbt.getString(key);
        else return null;
    }
    public static void setString(@Nullable CompoundTag nbt, String key, @Nullable String value) {
        if (nbt == null) return;
        else if (value == null) removeKey(nbt, key);
        else nbt.putString(key, value);
    }

    public static @Nullable CompoundTag getCompoundTag(@Nullable CompoundTag nbt, String key) {
        if (nbt == null) return null;
        else if (nbt.contains(key)) return nbt.getCompound(key);
        else return null;
    }
    public static void setCompoundTag(@Nullable CompoundTag nbt, String key, @Nullable CompoundTag value) {
        if (nbt == null) return;
        else if (value == null) removeKey(nbt, key);
        else nbt.put(key, value);
    }

    public static void removeKey(@Nullable CompoundTag nbt, String key) {
        if (nbt != null) nbt.remove(key);
    }
}
