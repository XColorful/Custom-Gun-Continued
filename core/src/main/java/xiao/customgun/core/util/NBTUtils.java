/*
 * 基于 BattleRoyale GameIdHelper 相同的封装目的
 * 只不过 BattleRoyale 已经打通版本差异而没有必要专门做一个NBTUtils (只需要操作 GameId，所以没地方用)
 */

package xiao.customgun.core.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.minecraft.IMcRegistry;

public class NBTUtils {

    /**
     * 缓存字段，避免每次都重新拿
     * 其他模组不应该在模组主类初始化时调用
     */
    public static final IMcRegistry mcRegistry = CustomGun.getMcRegistry();

    /**
     * 获取 DataComponents.CUSTOM_DATA
     */
    public static @Nullable CustomData getCustomData(@Nullable ItemStack itemStack) {
        if (itemStack == null) return null;
        else return itemStack.getOrDefault(DataComponents.CUSTOM_DATA, null);
    }
    public static @NotNull CustomData getOrCreateCustomData(@NotNull ItemStack itemStack) {
        return itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()));
    }
    /**
     * 写入 DataComponents.CUSTOM_DATA
     */
    public static void setCustomData(@NotNull ItemStack itemStack, CustomData customData) {
        itemStack.set(DataComponents.CUSTOM_DATA, customData);
    }
    /**
     * 将 CompoundTag 写入 DataComponents.CUSTOM_DATA
     */
    public static void setCustomDataTag(@NotNull ItemStack itemStack, @Nullable CompoundTag nbt) {
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt != null ? nbt : new CompoundTag()));
    }

    @ApiStatus.AvailableSince("1.21.1")
    public static @NotNull CompoundTag getCustomDataTag(@NotNull CustomData customData) {
        return customData.copyTag();
    }

    // --------ItemStack--------
    // (便利接口) 从CUSTOM_DATA根目录获取数据
    // Setter接口包含存储

    public static @Nullable String getString(@Nullable ItemStack itemStack, String key) {
        @Nullable var customData = getCustomData(itemStack);
        return customData != null ? getString(getCustomDataTag(customData), key) : null;
    }
    public static void setString(@NotNull ItemStack itemStack, String key, @Nullable String value) {
        var customData = getOrCreateCustomData(itemStack);
        @NotNull CompoundTag customDataTag = getCustomDataTag(customData);
        setString(customDataTag, key, value);
        setCustomDataTag(itemStack, customDataTag);
    }

    public static @Nullable ResourceLocation getResourceLocation(@Nullable ItemStack itemStack, String key) {
        @Nullable var customData = getCustomData(itemStack);
        return customData != null ? getResourceLocation(getCustomDataTag(customData), key) : null;
    }
    public static void setResourceLocation(@NotNull ItemStack itemStack, String key, @Nullable ResourceLocation value) {
        var customData = getOrCreateCustomData(itemStack);
        @NotNull CompoundTag customDataTag = getCustomDataTag(customData);
        setResourceLocation(customDataTag, key, value);
        setCustomDataTag(itemStack, customDataTag);
    }

    public static float getFloat(@Nullable ItemStack itemStack, String key) {
        @Nullable var customData = getCustomData(itemStack);
        return customData != null ? getFloat(getCustomDataTag(customData), key) : 0;
    }
    public static void setFloat(@NotNull ItemStack itemStack, String key, float value) {
        var customData = getOrCreateCustomData(itemStack);
        @NotNull CompoundTag customDataTag = getCustomDataTag(customData);
        setFloat(customDataTag, key, value);
        setCustomDataTag(itemStack, customDataTag);
    }

    public static int getInt(@Nullable ItemStack itemStack, String key) {
        @Nullable var customData = getCustomData(itemStack);
        return customData != null ? getInt(getCustomDataTag(customData), key) : 0;
    }
    public static void setInt(@NotNull ItemStack itemStack, String key, int value) {
        var customData = getOrCreateCustomData(itemStack);
        @NotNull CompoundTag customDataTag = getCustomDataTag(customData);
        setInt(customDataTag, key, value);
        setCustomDataTag(itemStack, customDataTag);
    }

    public static boolean getBoolean(@Nullable ItemStack itemStack, String key) {
        @Nullable var customData = getCustomData(itemStack);
        return customData != null && getBoolean(getCustomDataTag(customData), key);
    }
    public static void setBoolean(@NotNull ItemStack itemStack, String key, boolean value) {
        var customData = getOrCreateCustomData(itemStack);
        @NotNull CompoundTag customDataTag = getCustomDataTag(customData);
        setBoolean(customDataTag, key, value);
        setCustomDataTag(itemStack, customDataTag);
    }

    public static @Nullable CompoundTag getCompoundTag(@Nullable ItemStack itemStack, String key) {
        @Nullable var customData = getCustomData(itemStack);
        return customData != null ? getCompoundTag(getCustomDataTag(customData), key) : null;
    }
    public static void setCompoundTag(@NotNull ItemStack itemStack, String key, @Nullable CompoundTag value) {
        var customData = getOrCreateCustomData(itemStack);
        @NotNull CompoundTag customDataTag = getCustomDataTag(customData);
        setCompoundTag(customDataTag, key, value);
        setCustomDataTag(itemStack, customDataTag);
    }

    public static boolean hasKey(@NotNull ItemStack itemStack, String key) {
        var customData = getCustomData(itemStack);
        if (customData == null) return false;
        return hasKey(getCustomDataTag(customData), key);
    }
    public static void removeKey(@NotNull ItemStack itemStack, String key) {
        var customData = getCustomData(itemStack);
        if (customData == null) return;
        CompoundTag customDataTag = getCustomDataTag(customData);
        removeKey(customDataTag, key);
        setCustomDataTag(itemStack, customDataTag);
    }

    // --------CompoundTag--------
    // 直接操作NBT

    public static @Nullable String getString(@Nullable CompoundTag nbt, String key) {
        if (nbt == null) return null;
        else if (nbt.contains(key)) return nbt.getString(key).orElse(null);
        else return null;
    }
    public static void setString(@Nullable CompoundTag nbt, String key, @Nullable String value) {
        if (nbt == null) return;
        else if (value == null) removeKey(nbt, key);
        else nbt.putString(key, value);
    }

    public static @Nullable ResourceLocation getResourceLocation(@Nullable CompoundTag nbt, String key) {
        if (nbt == null) return null;
        else if (nbt.contains(key)) return mcRegistry.createResourceLocation(getString(nbt, key));
        else return null;
    }
    public static void setResourceLocation(@Nullable CompoundTag nbt, String key, @Nullable ResourceLocation value) {
        if (nbt == null) return;
        else if (value == null) removeKey(nbt, key);
        else nbt.putString(key, value.toString());
    }

    public static float getFloat(@Nullable CompoundTag nbt, String key) {
        if (nbt == null) return 0;
        else if (nbt.contains(key)) return nbt.getFloat(key).orElse(0F);
        else return 0;
    }
    public static void setFloat(@Nullable CompoundTag nbt, String key, float value) {
        if (nbt == null) return;
        else nbt.putFloat(key, value);
    }

    public static int getInt(@Nullable CompoundTag nbt, String key) {
        if (nbt == null) return 0;
        else if (nbt.contains(key)) return nbt.getInt(key).orElse(0);
        else return 0;
    }
    public static void setInt(@Nullable CompoundTag nbt, String key, int value) {
        if (nbt == null) return;
        else nbt.putInt(key, value);
    }

    public static boolean getBoolean(@Nullable CompoundTag nbt, String key) {
        if (nbt == null) return false;
        else if (nbt.contains(key)) return nbt.getBoolean(key).orElse(false);
        else return false;
    }
    public static void setBoolean(@Nullable CompoundTag nbt, String key, boolean value) {
        if (nbt == null) return;
        else nbt.putBoolean(key, value);
    }

    public static @Nullable CompoundTag getCompoundTag(@Nullable CompoundTag nbt, String key) {
        if (nbt == null) return null;
        else if (nbt.contains(key)) return nbt.getCompound(key).orElse(null);
        else return null;
    }
    public static void setCompoundTag(@Nullable CompoundTag nbt, String key, @Nullable CompoundTag value) {
        if (nbt == null) return;
        else if (value == null) removeKey(nbt, key);
        else nbt.put(key, value);
    }

    public static boolean hasKey(@Nullable CompoundTag nbt, String key) {
        if (nbt == null) return false;
        else return nbt.contains(key);
    }
    public static void removeKey(@Nullable CompoundTag nbt, String key) {
        if (nbt != null) nbt.remove(key);
    }
}
