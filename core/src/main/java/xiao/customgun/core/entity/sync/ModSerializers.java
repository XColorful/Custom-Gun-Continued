/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.entity.sync.core.IDataSerializer;

public class ModSerializers {
    public static final IDataSerializer<ReloadState> RELOAD_STATE = new IDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf buf, ReloadState value) {
            buf.writeInt(value.getStateType().ordinal());
            buf.writeLong(value.getCountDown());
        }

        @Override
        public ReloadState read(FriendlyByteBuf buf) {
            ReloadState reloadState = new ReloadState();
            reloadState.setStateType(ReloadState.StateType.values()[buf.readInt()]);
            reloadState.setCountDown(buf.readLong());
            return reloadState;
        }

        @Override
        public Tag write(ReloadState value) {
            CompoundTag compound = new CompoundTag();
            compound.putString("StateType", value.getStateType().toString());
            compound.putLong("CountDown", value.getCountDown());
            return compound;
        }

        @Override
        public ReloadState read(Tag nbt) {
            CompoundTag compound = (CompoundTag) nbt;
            try {
                ReloadState.StateType stateType = ReloadState.StateType.valueOf(compound.getString("StateType"));
                long countDown = compound.getLong("CountDown");
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
