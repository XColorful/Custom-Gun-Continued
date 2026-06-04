/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.sync;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.util.NetworkUtils;

import java.util.UUID;

/**
 * Framework provided serializers used for creating a {@link SyncedDataKey}. This covers all
 * primitive types and common objects. You can create your custom serializer by implementing
 * {@link IDataSerializer}.
 * <p>
 * Author: MrCrayfish
 * Open source at <a href="https://github.com/MrCrayfish/Framework">Github</a> under LGPL License.
 */
public class Serializers {
    public static final IDataSerializer<Boolean> BOOLEAN = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, Boolean value) {
            buffer.writeBoolean(value);
        }
        @Override public Boolean read(FriendlyByteBuf buffer) {
            return buffer.readBoolean();
        }
        @Override public Tag write(HolderLookup.Provider provider, Boolean value) {
            return ByteTag.valueOf(value);
        }
        @Override public Boolean read(HolderLookup.Provider provider, Tag tag) {
            return ((ByteTag) tag).byteValue() != 0;
        }
    };

    public static final IDataSerializer<Byte> BYTE = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, Byte value) {
            buffer.writeByte(value);
        }
        @Override public Byte read(FriendlyByteBuf buffer) {
            return buffer.readByte();
        }
        @Override public Tag write(HolderLookup.Provider provider, Byte value) {
            return ByteTag.valueOf(value);
        }
        @Override public Byte read(HolderLookup.Provider provider, Tag tag) {
            return ((ByteTag) tag).byteValue();
        }
    };

    public static final IDataSerializer<Short> SHORT = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, Short value) {
            buffer.writeShort(value);
        }
        @Override public Short read(FriendlyByteBuf buffer) {
            return buffer.readShort();
        }
        @Override public Tag write(HolderLookup.Provider provider, Short value) {
            return ShortTag.valueOf(value);
        }
        @Override public Short read(HolderLookup.Provider provider, Tag tag) {
            return ((ShortTag) tag).shortValue();
        }
    };

    public static final IDataSerializer<Integer> INTEGER = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, Integer value) {
            buffer.writeVarInt(value);
        }
        @Override public Integer read(FriendlyByteBuf buffer) {
            return buffer.readVarInt();
        }
        @Override public Tag write(HolderLookup.Provider provider, Integer value) {
            return IntTag.valueOf(value);
        }
        @Override public Integer read(HolderLookup.Provider provider, Tag tag) {
            return ((IntTag) tag).intValue();
        }
    };

    public static final IDataSerializer<Long> LONG = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, Long value) {
            buffer.writeLong(value);
        }
        @Override public Long read(FriendlyByteBuf buffer) {
            return buffer.readLong();
        }
        @Override public Tag write(HolderLookup.Provider provider, Long value) {
            return LongTag.valueOf(value);
        }
        @Override public Long read(HolderLookup.Provider provider, Tag tag) {
            return ((LongTag) tag).longValue();
        }
    };

    public static final IDataSerializer<Float> FLOAT = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, Float value) {
            buffer.writeFloat(value);
        }
        @Override public Float read(FriendlyByteBuf buffer) {
            return buffer.readFloat();
        }
        @Override public Tag write(HolderLookup.Provider provider, Float value) {
            return FloatTag.valueOf(value);
        }
        @Override public Float read(HolderLookup.Provider provider, Tag tag) {
            return ((FloatTag) tag).floatValue();
        }
    };

    public static final IDataSerializer<Double> DOUBLE = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, Double value) {
            buffer.writeDouble(value);
        }
        @Override public Double read(FriendlyByteBuf buffer) {
            return buffer.readDouble();
        }
        @Override public Tag write(HolderLookup.Provider provider, Double value) {
            return DoubleTag.valueOf(value);
        }
        @Override public Double read(HolderLookup.Provider provider, Tag tag) {
            return ((DoubleTag) tag).doubleValue();
        }
    };

    public static final IDataSerializer<Character> CHARACTER = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, Character value) {
            buffer.writeChar(value);
        }
        @Override public Character read(FriendlyByteBuf buffer) {
            return buffer.readChar();
        }
        @Override public Tag write(HolderLookup.Provider provider, Character value) {
            return IntTag.valueOf(value);
        }
        @Override public Character read(HolderLookup.Provider provider, Tag tag) {
            return (char) ((IntTag) tag).intValue();
        }
    };

    public static final IDataSerializer<String> STRING = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, String value) {
            NetworkUtils.writeUtf(buffer, value);
        }
        @Override public String read(FriendlyByteBuf buffer) {
            return NetworkUtils.readUtf(buffer);
        }
        @Override public Tag write(HolderLookup.Provider provider, String value) {
            return StringTag.valueOf(value);
        }
        @Override public String read(HolderLookup.Provider provider, Tag tag) {
            return tag.asString().orElse("");
        }
    };

    public static final IDataSerializer<CompoundTag> TAG_COMPOUND = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, CompoundTag value) {
            buffer.writeNbt(value);
        }
        @Override public CompoundTag read(FriendlyByteBuf buffer) {
            return buffer.readNbt();
        }
        @Override public Tag write(HolderLookup.Provider provider, CompoundTag value) {
            return value;
        }
        @Override public CompoundTag read(HolderLookup.Provider provider, Tag tag) {
            return (CompoundTag) tag;
        }
    };

    public static final IDataSerializer<BlockPos> BLOCK_POS = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, BlockPos value) {
            buffer.writeBlockPos(value);
        }
        @Override public BlockPos read(FriendlyByteBuf buffer) {
            return buffer.readBlockPos();
        }
        @Override public Tag write(HolderLookup.Provider provider, BlockPos value) {
            return LongTag.valueOf(value.asLong());
        }
        @Override public BlockPos read(HolderLookup.Provider provider, Tag tag) {
            return BlockPos.of(((LongTag) tag).longValue());
        }
    };

    public static final IDataSerializer<UUID> UUID = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, UUID value) {
            buffer.writeUUID(value);
        }
        @Override public UUID read(FriendlyByteBuf buffer) {
            return buffer.readUUID();
        }
        @Override public Tag write(HolderLookup.Provider provider, UUID value) {
            CompoundTag compound = new CompoundTag();
            compound.putLong("Most", value.getMostSignificantBits());
            compound.putLong("Least", value.getLeastSignificantBits());
            return compound;
        }
        @Override public UUID read(HolderLookup.Provider provider, Tag tag) {
            CompoundTag compound = (CompoundTag) tag;
            return new UUID(compound.getLongOr("Most", 0), compound.getLongOr("Least", 0));
        }
    };

    public static final IDataSerializer<ItemStack> ITEM_STACK = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, ItemStack value) {
            NetworkUtils.writeItem(buffer, value);
        }
        @Override public ItemStack read(FriendlyByteBuf buffer) {
            return NetworkUtils.readItem(buffer);
        }
        @Override public Tag write(HolderLookup.Provider provider, ItemStack value) {
            RegistryOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);
            return ItemStack.OPTIONAL_CODEC.encodeStart(ops, value)
                    .getOrThrow(p -> new IllegalArgumentException("Failed to encode ItemStack: " + p));
        }
        @Override public ItemStack read(HolderLookup.Provider provider, Tag tag) {
            RegistryOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);
            return ItemStack.OPTIONAL_CODEC.parse(ops, tag)
                    .getOrThrow(p -> new IllegalArgumentException("Failed to parse ItemStack: " + p));
        }
    };

    public static final IDataSerializer<Identifier> RESOURCE_LOCATION = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, Identifier value) {
            NetworkUtils.writeResourceLocation(buffer, value);
        }
        @Override public Identifier read(FriendlyByteBuf buffer) {
            return NetworkUtils.readResourceLocation(buffer);
        }
        @Override public Tag write(HolderLookup.Provider provider, Identifier value) {
            return StringTag.valueOf(value.toString());
        }
        @Override public Identifier read(HolderLookup.Provider provider, Tag tag) {
            return CustomGun.getMcRegistry().createResourceLocation(tag.asString().orElse(""));
        }
    };

    public static final IDataSerializer<ReloadState> RELOAD_STATE = new IDataSerializer<>() {
        @Override public void write(FriendlyByteBuf buffer, ReloadState value) {
            buffer.writeInt(value.getStateType().ordinal());
            buffer.writeLong(value.getCountDown());
        }
        @Override public ReloadState read(FriendlyByteBuf buffer) {
            ReloadState reloadState = new ReloadState();
            reloadState.setStateType(ReloadState.StateType.values()[buffer.readInt()]);
            reloadState.setCountDown(buffer.readLong());
            return reloadState;
        }
        @Override public Tag write(HolderLookup.Provider provider, ReloadState value) {
            CompoundTag compound = new CompoundTag();
            compound.putString("StateType", value.getStateType().toString());
            compound.putLong("CountDown", value.getCountDown());
            return compound;
        }
        @Override public ReloadState read(HolderLookup.Provider provider, Tag nbt) {
            CompoundTag compound = (CompoundTag) nbt;
            try {
                ReloadState.StateType stateType = ReloadState.StateType.valueOf(compound.getString("StateType").orElse(""));
                long countDown = compound.getLongOr("CountDown", 0);
                ReloadState reloadState = new ReloadState();
                reloadState.setStateType(stateType);
                reloadState.setCountDown(countDown);
                return reloadState;
            } catch (IllegalArgumentException ignore) {
            }
            return new ReloadState();
        }
    };
}
