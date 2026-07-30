/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.gun.script;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.util.NBTUtils;

/**
 * 一个简单的NBT包装，用于在Lua中访问NBT数据。<br/>
 * 暂时只支持基本数据类型的读写，不支持数组等复杂数据类型。
 */
@Deprecated
public class _LuaNbtAccessor {

    private final @Nullable ItemStack itemStack;
    private @Nullable CompoundTag nbt;
    private _LuaNbtAccessor(ItemStack itemStack, CompoundTag nbt) {
        this.itemStack = itemStack;
        this.nbt = nbt;
    }
    private _LuaNbtAccessor(ItemStack itemStack) {
        this(itemStack, null);
    }
    private _LuaNbtAccessor(CompoundTag nbt) {
        this(null, nbt);
    }
    public static _LuaNbtAccessor of(ItemStack itemStack) {
        return new _LuaNbtAccessor(itemStack);
    }
    public static _LuaNbtAccessor of(CompoundTag nbt) {
        return new _LuaNbtAccessor(nbt);
    }
    @ApiStatus.Internal
    public CompoundTag nbt() {
        var customData = NBTUtils.getCustomData(this.itemStack);
        if (customData == null) return null;
        return NBTUtils.getCustomDataTag(customData);
    }

    public boolean contains(String key) {
        if (this.itemStack != null) return NBTUtils.hasKey(this.itemStack, key);
        else return NBTUtils.hasKey(this.nbt, key);
    }

    @Deprecated public boolean contains(String key, int type) {
        return contains(key);
    }

    public _LuaNbtAccessor newCompoundTag() {
        return new _LuaNbtAccessor(new CompoundTag());
    }

    public int getInt(String key) {
        if (this.itemStack != null) return NBTUtils.getInt(this.itemStack, key);
        else return NBTUtils.getInt(this.nbt, key);
    }

    public double getDouble(String key) {
        if (this.itemStack != null) return NBTUtils.getDouble(this.itemStack, key);
        else return NBTUtils.getDouble(this.nbt, key);
    }

    public float getFloat(String key) {
        if (this.itemStack != null) return NBTUtils.getFloat(this.itemStack, key);
        else return NBTUtils.getFloat(this.nbt, key);
    }

    public long getLong(String key) {
        if (this.itemStack != null) return NBTUtils.getLong(this.itemStack, key);
        else return NBTUtils.getLong(this.nbt, key);
    }

    public String getString(String key) {
        if (this.itemStack != null) return NBTUtils.getString(this.itemStack, key);
        else return NBTUtils.getString(this.nbt, key);
    }

    public boolean getBoolean(String key) {
        if (this.itemStack != null) return NBTUtils.getBoolean(this.itemStack, key);
        else return NBTUtils.getBoolean(this.nbt, key);
    }

    public _LuaNbtAccessor getCompound(String key) {
        if (this.itemStack != null) return of(NBTUtils.getCompoundTag(this.itemStack, key));
        else return of(NBTUtils.getCompoundTag(this.nbt, key));
    }

    public void putInt(String key, int value) {
        if (this.itemStack != null) NBTUtils.setInt(this.itemStack, key, value);
        else NBTUtils.setInt(this.nbt, key, value);
    }

    public void putDouble(String key, double value) {
        if (this.itemStack != null) NBTUtils.setDouble(this.itemStack, key, value);
        else NBTUtils.setDouble(this.nbt, key, value);
    }

    public void putFloat(String key, float value) {
        if (this.itemStack != null) NBTUtils.setFloat(this.itemStack, key, value);
        else NBTUtils.setFloat(this.nbt, key, value);
    }

    public void putLong(String key, long value) {
        if (this.itemStack != null) NBTUtils.setLong(this.itemStack, key, value);
        else NBTUtils.setLong(this.nbt, key, value);
    }

    public void putString(String key, String value) {
        if (this.itemStack != null) NBTUtils.setString(this.itemStack, key, value);
        else NBTUtils.setString(this.nbt, key, value);
    }

    public void putBoolean(String key, boolean value) {
        if (this.itemStack != null) NBTUtils.setBoolean(this.itemStack, key, value);
        else NBTUtils.setBoolean(this.nbt, key, value);
    }

    /**
     * 向当前的NbtCompound中添加一个新的Compound
     *
     * @param key   键
     * @param value 在脚本中请使用{@link _LuaNbtAccessor#newCompoundTag()}创建一个新的LuaNbtAccessor对象
     */
    public void putCompound(String key, _LuaNbtAccessor value) {
        if (this.itemStack != null) {
            NBTUtils.setCompoundTag(this.itemStack, key, value.nbt);
        } else if (this.nbt != null) {
            NBTUtils.setCompoundTag(this.nbt, key, value.nbt);
        } else {
            this.nbt = new CompoundTag();
            NBTUtils.setCompoundTag(this.nbt, key, value.nbt);
        }
    }
}
