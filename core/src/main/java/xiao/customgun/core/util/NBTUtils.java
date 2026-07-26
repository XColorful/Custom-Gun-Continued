/*
 * 基于 BattleRoyale GameIdHelper 相同的封装目的
 * 只不过 BattleRoyale 已经打通版本差异而没有必要专门做一个NBTUtils (只需要操作 GameId，所以没地方用)
 */

package xiao.customgun.core.util;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.minecraft.IMcRegistry;

import java.io.StringReader;
import java.io.StringWriter;

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

    /**
     * 获取 根目录ForgeData/NeoForgeData
     * 若移植Fabric则修改此处, 而不是引入接口放fabric-compat实现
     */
    public static @Nullable CompoundTag getCustomData(@Nullable Entity entity) {
        if (entity == null) return null;
        else return entity.getPersistentData();
    }
    public static @NotNull CompoundTag getOrCreateCustomData(@NotNull Entity entity) {
        return entity.getPersistentData();
    }
    /**
     * 写入 根目录ForgeData/NeoForgeData
     */
    @Deprecated
    public static void setCustomData(@NotNull Entity entity, CompoundTag customData) {
        setCustomDataTag(entity, customData);
    }
    /**
     * 将 CompoundTag 写入 根目录ForgeData/NeoForgeData1
     */
    public static void setCustomDataTag(@NotNull Entity entity, @Nullable CompoundTag customDataTag) {
        CompoundTag nbt = entity.getPersistentData();
        if (nbt == customDataTag) return; // 同一个引用, 不然↓就把键删完了
        for (String key : java.util.List.copyOf(nbt.keySet())) {
            nbt.remove(key);
        }
        if (customDataTag != null) {
            for (String key : customDataTag.keySet()) {
                nbt.put(key, customDataTag.get(key));
            }
        }
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

    public static @Nullable Identifier getResourceLocation(@Nullable ItemStack itemStack, String key) {
        @Nullable var customData = getCustomData(itemStack);
        return customData != null ? getResourceLocation(getCustomDataTag(customData), key) : null;
    }
    public static void setResourceLocation(@NotNull ItemStack itemStack, String key, @Nullable Identifier value) {
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

    public static @Nullable Vec3 getVec3(@Nullable ItemStack itemStack, String key) {
        @Nullable var customData = getCustomData(itemStack);
        return customData != null ? getVec3(getCustomDataTag(customData), key) : null;
    }
    public static void setVec3(@NotNull ItemStack itemStack, String key, @Nullable Vec3 value) {
        var customData = getOrCreateCustomData(itemStack);
        @NotNull CompoundTag customDataTag = getCustomDataTag(customData);
        setVec3(customDataTag, key, value);
        setCustomDataTag(itemStack, customDataTag);
    }

    public static @Nullable <T> T getStringJson(@Nullable ItemStack itemStack, String key, JsonUtils.ReadFunction<T> function) {
        @Nullable var customData = getCustomData(itemStack);
        return customData != null ? getStringJson(getCustomDataTag(customData), key, function) : null;
    }
    public static <T> void setStringJson(@NotNull ItemStack itemStack, String key, T value, JsonUtils.WriteAction<T> function) {
        var customData = getOrCreateCustomData(itemStack);
        @NotNull CompoundTag customDataTag = getCustomDataTag(customData);
        setStringJson(customDataTag, key, value, function);
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

    // --------Entity--------
    // (便利接口) 从根目录ForgeData/NeoForgeData获取数据
    // Setter接口包含存储

    public static @Nullable String getString(@Nullable Entity entity, String key) {
        @Nullable CompoundTag customDataTag =
                getCustomData(entity);
        return customDataTag != null ? getString(customDataTag, key) : null;
    }
    public static void setString(@NotNull Entity entity, String key, @Nullable String value) {
        @NotNull CompoundTag customDataTag =
                getOrCreateCustomData(entity);
        setString(customDataTag, key, value);
//        setCustomDataTag(entity, customDataTag);
    }

    public static @Nullable Identifier getResourceLocation(@Nullable Entity entity, String key) {
        @Nullable CompoundTag customDataTag =
                getCustomData(entity);
        return customDataTag != null ? getResourceLocation(customDataTag, key) : null;
    }
    public static void setResourceLocation(@NotNull Entity entity, String key, @Nullable Identifier value) {
        @NotNull CompoundTag customDataTag =
                getOrCreateCustomData(entity);
        setResourceLocation(customDataTag, key, value);
//        setCustomDataTag(entity, customDataTag);
    }

    public static float getFloat(@Nullable Entity entity, String key) {
        @Nullable CompoundTag customDataTag =
                getCustomData(entity);
        return customDataTag != null ? getFloat(customDataTag, key) : 0;
    }
    public static void setFloat(@NotNull Entity entity, String key, float value) {
        @NotNull CompoundTag customDataTag =
                getOrCreateCustomData(entity);
        setFloat(customDataTag, key, value);
//        setCustomDataTag(entity, customDataTag);
    }

    public static int getInt(@Nullable Entity entity, String key) {
        @Nullable CompoundTag customDataTag =
                getCustomData(entity);
        return customDataTag != null ? getInt(customDataTag, key) : 0;
    }
    public static void setInt(@NotNull Entity entity, String key, int value) {
        @NotNull CompoundTag customDataTag =
                getOrCreateCustomData(entity);
        setInt(customDataTag, key, value);
//        setCustomDataTag(entity, customDataTag);
    }

    public static boolean getBoolean(@Nullable Entity entity, String key) {
        @Nullable CompoundTag customDataTag =
                getCustomData(entity);
        return customDataTag != null && getBoolean(customDataTag, key);
    }
    public static void setBoolean(@NotNull Entity entity, String key, boolean value) {
        @NotNull CompoundTag customDataTag =
                getOrCreateCustomData(entity);
        setBoolean(customDataTag, key, value);
//        setCustomDataTag(entity, customDataTag);
    }

    public static @Nullable Vec3 getVec3(@Nullable Entity entity, String key) {
        @Nullable CompoundTag customDataTag =
                getCustomData(entity);
        return customDataTag != null ? getVec3(customDataTag, key) : null;
    }
    public static void setVec3(@NotNull Entity entity, String key, @Nullable Vec3 value) {
        @NotNull CompoundTag customDataTag =
                getOrCreateCustomData(entity);
        setVec3(customDataTag, key, value);
//        setCustomDataTag(entity, customDataTag);
    }

    public static @Nullable CompoundTag getCompoundTag(@Nullable Entity entity, String key) {
        @Nullable CompoundTag customDataTag =
                getCustomData(entity);
        return customDataTag != null ? getCompoundTag(customDataTag, key) : null;
    }
    public static void setCompoundTag(@NotNull Entity entity, String key, @Nullable CompoundTag value) {
        @NotNull CompoundTag customDataTag =
                getOrCreateCustomData(entity);
        setCompoundTag(customDataTag, key, value);
//        setCustomDataTag(entity, customDataTag);
    }

    public static @Nullable <T> T getStringJson(@Nullable Entity entity, String key, JsonUtils.ReadFunction<T> function) {
        @Nullable CompoundTag customDataTag =
                getCustomData(entity);
        return customDataTag != null ? getStringJson(customDataTag, key, function) : null;
    }
    public static <T> void setStringJson(@NotNull Entity entity, String key, T value, JsonUtils.WriteAction<T> function) {
        @NotNull CompoundTag customDataTag =
                getOrCreateCustomData(entity);
        setStringJson(customDataTag, key, value, function);
    }

    public static boolean hasKey(@NotNull Entity entity, String key) {
        @Nullable CompoundTag customDataTag =
                getCustomData(entity);
        if (customDataTag == null) return false;
        return hasKey(customDataTag, key);
    }
    public static void removeKey(@NotNull Entity entity, String key) {
        @Nullable CompoundTag customDataTag =
                getCustomData(entity);
        if (customDataTag == null) return;
        removeKey(customDataTag, key);
//        setCustomDataTag(entity, customDataTag);
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

    public static @Nullable Identifier getResourceLocation(@Nullable CompoundTag nbt, String key) {
        if (nbt == null) return null;
        else if (nbt.contains(key)) return mcRegistry.createResourceLocation(getString(nbt, key));
        else return null;
    }
    public static void setResourceLocation(@Nullable CompoundTag nbt, String key, @Nullable Identifier value) {
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

    public static @Nullable Vec3 getVec3(@Nullable CompoundTag nbt, String key) {
        return Vec3Utils.fromString(getString(nbt, key));
    }
    public static void setVec3(@Nullable CompoundTag nbt, String key, @Nullable Vec3 value) {
        setString(nbt, key, Vec3Utils.toString(value));
    }

    public static @Nullable <T> T getStringJson(@Nullable CompoundTag nbt, String key, JsonUtils.ReadFunction<T> function) {
        String jsonString = getString(nbt, key);
        if (jsonString == null) return null;
        else {
            try {
                JsonReader reader = new JsonReader(new StringReader(jsonString));
                return function.apply(reader);
            } catch (Exception e) {
                return null;
            }
        }
    }
    public static <T> void setStringJson(@Nullable CompoundTag nbt, String key, @Nullable T value, JsonUtils.WriteAction<T> function) {
        if (nbt == null) return;
        else if (value == null) removeKey(nbt, key);
        else {
            try {
                StringWriter stringWriter = new StringWriter();
                JsonWriter writer = new JsonWriter(stringWriter);
                function.accept(writer, value);
                writer.close();

                setString(nbt, key, stringWriter.toString());
            } catch (Exception e) {
                removeKey(nbt, key);
            }
        }
    }

    public static boolean hasKey(@Nullable CompoundTag nbt, String key) {
        if (nbt == null) return false;
        else return nbt.contains(key);
    }
    public static void removeKey(@Nullable CompoundTag nbt, String key) {
        if (nbt != null) nbt.remove(key);
    }

    public static class Parser {
        public static @Nullable CompoundTag fromString(String nbt) {
            if (nbt == null || nbt.isEmpty()) return null;

            try {
                return TagParser.parseCompoundFully(nbt);
            } catch (Exception e) {
                return null;
            }
        }
        /**
         * {@link CompoundTag#CODEC parse}
         */
        public static @Nullable CompoundTag fromJson(JsonElement json) {
            return fromString(json.toString());
        }
    }

    /**
     * 对 {@code ValueInput} {@code ValueOutput} 的封装, 适用于Entity和Block的硬盘IO
     * @since 1.21.6
     */
    @ApiStatus.AvailableSince("1.21.6")
    public static class Value {

        public static @Nullable String getString(@Nullable ValueInput input, String key) {
            if (input == null) return null;
            return input.getString(key).orElse(null);
        }
        public static void setString(@Nullable ValueOutput output, String key, @Nullable String value) {
            if (output == null) return;
            if (value == null) output.discard(key);
            else output.putString(key, value);
        }

        public static @Nullable Identifier getResourceLocation(@Nullable ValueInput input, String key) {
            if (input == null) return null;
            return input.getString(key).map(mcRegistry::createResourceLocation).orElse(null);
        }
        public static void setResourceLocation(@Nullable ValueOutput output, String key, @Nullable Identifier value) {
            if (output == null) return;
            if (value == null) output.discard(key);
            else output.putString(key, value.toString());
        }

        public static float getFloat(@Nullable ValueInput input, String key) {
            if (input == null) return 0;
            return input.getFloatOr(key, 0F);
        }
        public static void setFloat(@Nullable ValueOutput output, String key, float value) {
            if (output == null) return;
            output.putFloat(key, value);
        }

        public static int getInt(@Nullable ValueInput input, String key) {
            if (input == null) return 0;
            return input.getIntOr(key, 0);
        }
        public static void setInt(@Nullable ValueOutput output, String key, int value) {
            if (output == null) return;
            output.putInt(key, value);
        }

        public static boolean getBoolean(@Nullable ValueInput input, String key) {
            if (input == null) return false;
            return input.getBooleanOr(key, false);
        }
        public static void setBoolean(@Nullable ValueOutput output, String key, boolean value) {
            if (output == null) return;
            output.putBoolean(key, value);
        }

        public static @Nullable CompoundTag getCompoundTag(@Nullable ValueInput input, String key) {
            if (input == null) return null;
            return input.read(key, CompoundTag.CODEC).orElse(null);
        }
        public static void setCompoundTag(@Nullable ValueOutput output, String key, @Nullable CompoundTag value) {
            if (output == null) return;
            if (value == null) output.discard(key);
            else output.store(key, CompoundTag.CODEC, value);
        }

        public static @Nullable Vec3 getVec3(@Nullable ValueInput input, String key) {
            return Vec3Utils.fromString(getString(input, key));
        }
        public static void setVec3(@Nullable ValueOutput output, String key, @Nullable Vec3 value) {
            setString(output, key, Vec3Utils.toString(value));
        }

        public static @Nullable <T> T getStringJson(@Nullable ValueInput input, String key, JsonUtils.ReadFunction<T> function) {
            String jsonString = getString(input, key);
            if (jsonString == null) return null;
            else {
                try {
                    JsonReader reader = new JsonReader(new StringReader(jsonString));
                    return function.apply(reader);
                } catch (Exception e) {
                    return null;
                }
            }
        }
        public static <T> void setStringJson(@Nullable ValueOutput output, String key, @Nullable T value, JsonUtils.WriteAction<T> function) {
            if (output == null) return;
            else if (value == null) removeKey(output, key);
            else {
                try {
                    StringWriter stringWriter = new StringWriter();
                    JsonWriter writer = new JsonWriter(stringWriter);
                    function.accept(writer, value);
                    writer.close();

                    setString(output, key, stringWriter.toString());
                } catch (Exception e) {
                    removeKey(output, key);
                }
            }
        }

        public static @Nullable ValueInput getChildInput(@Nullable ValueInput input, String key) {
            if (input == null) return null;
            return input.child(key).orElse(null);
        }
        public static @Nullable ValueOutput getChildOutput(@Nullable ValueOutput output, String key) {
            if (output == null) return null;
            return output.child(key);
        }

        public static boolean hasKey(@Nullable ValueInput input, String key) {
            if (input == null) return false;
            return input.keySet().contains(key);
        }
        public static void removeKey(@Nullable ValueOutput output, String key) {
            if (output != null) output.discard(key);
        }
    }
}
