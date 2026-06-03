/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.instance.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.item.gun.FireModeType;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.data.gun._BulletData;
import xiao.customgun.core.resource.data.data.gun._RecoilData;
import xiao.customgun.core.resource.data.data.gun.recoil._RecoilEntryData;
import xiao.customgun.core.resource.data.index.GunIndex;
import xiao.customgun.core.resource.data.script.DataScript;
import xiao.customgun.core.resource.instance.PojoInstance;

import java.util.List;
import java.util.Map;

public class GunIndexInstance extends PojoInstance<GunIndex> {

    private GunData gunDataCache;

    private @Nullable LuaTable script = null;
    private @Nullable LuaTable scriptParamCache = null;

    private GunIndexInstance(@NotNull GunIndex pojo) {
        super(pojo);
    }

    public static @Nullable GunIndexInstance fromPojo(GunIndex pojo) {
        if (pojo == null) return null;
        GunIndexInstance instance = new GunIndexInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        this.gunDataCache = ResourceApi.getGunData(this.getPojo().getDataLocation());
        if (this.gunDataCache == null) {
            CustomGun.LOGGER.debug("GunIndexInstance: GunData {} not found", this.getPojo().getDataLocation());
            return false;
        } else if (!this.gunDataCache.isValid()) {
            CustomGun.LOGGER.debug("GunIndexInstance: GunData {} not valid", this.getPojo().getDataLocation());
            return false;
        }

        var scriptLocation = this.gunDataCache.getScriptLocation();
        if (scriptLocation != null) {
            DataScript dataScript = ResourceApi.getDataScript(scriptLocation);
            if (dataScript == null) CustomGun.LOGGER.debug("GunIndexInstance: DataScript {} not found", scriptLocation);
            else if (!dataScript.isValid()) CustomGun.LOGGER.debug("GunIndexInstance: DataScript {} not valid", scriptLocation);
            else this.script = dataScript.getResultTable();
        }
        Map<String, Object> scriptParams = this.gunDataCache.getScriptParam();
        if (scriptParams != null) {
            this.scriptParamCache = new LuaTable();
            for (Map.Entry<String, Object> entry : scriptParams.entrySet()) {
                this.scriptParamCache.set(entry.getKey(), CoerceJavaToLua.coerce(entry.getValue()));
            }
        }

        return true;
    }
    private static final int ERR_MAG_SIZE = 1;
    private static final int ERR_RPM = 1 << 1;
    private static final int ERR_FIRE_MODE_TYPES = 1 << 2;
    private static final int ERR_RECOIL_DATA = 1 << 3;
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;
        if (!resetCache()) return false;

        int errorMask = 0;
        // GunIndex
        if (this.getPojo().getSlotSort() > 65536) CustomGun.LOGGER.warn("GunIndexInstance: GunIndex slotSort {} > 65536", this.getPojo().getSlotSort());
        // GunData
        errorMask |= this.gunDataCache.getDefaultMagSize() < 1 ? ERR_MAG_SIZE : 0;
        errorMask |= this.gunDataCache.getRpm() < 1 ? ERR_RPM : 0;
        errorMask |= checkFireModeTypes(this.gunDataCache) ? ERR_FIRE_MODE_TYPES : 0;
        errorMask |= checkRecoilData(this.gunDataCache) ? ERR_RECOIL_DATA : 0;
        if (errorMask != 0) {
            this.logAllErrors(errorMask);
            return false;
        }

        return true;
    }
    @Override protected void logAllErrors(int errorMask) {
        StringBuilder sb = new StringBuilder("GunIndexInstance: GunData ").append(this.getPojo().getDataLocation()).append(" is invalid because:");
        if ((errorMask & ERR_MAG_SIZE) != 0) sb.append("\n\t- defaultMagSize < 1");
        if ((errorMask & ERR_RPM) != 0) sb.append("\n\t- rpm < 1");
        if ((errorMask & ERR_FIRE_MODE_TYPES) != 0) sb.append("\n\t- fireModeTypes is empty");
        if ((errorMask & ERR_RECOIL_DATA) != 0) sb.append("\n\t- recoilData is invalid");
        CustomGun.LOGGER.debug(sb.toString());
    }
    public static boolean checkFireModeTypes(@NotNull GunData pojo) {
        List<FireModeType> fireModeTypes = pojo.getFireModeTypes();
        if (fireModeTypes.isEmpty()) return true;
        for (int i = 0; i < fireModeTypes.size(); i++) {
            if (fireModeTypes.get(i) != FireModeType.DEFAULT) return false;
        }
        return true;
    }
    public static boolean checkRecoilData(@NotNull GunData pojo) {
        _RecoilData recoilData = pojo.getRecoilData();
        {
            List<_RecoilEntryData> pitch = recoilData.getPitchRecoils();
            if (pitch.isEmpty()) return true;
            _RecoilEntryData prev = pitch.get(0);
            float[] r = prev.getRange();
            if (r.length < 2 || r[0] > r[1]) return true;
            for (int i = 1; i < pitch.size(); i++) {
                _RecoilEntryData cur = pitch.get(i);
                if (cur.getTime() < prev.getTime()) return true;
                float[] curR = cur.getRange();
                if (curR.length < 2 || curR[0] > curR[1]) return true;
                prev = cur;
            }
        }
        {
            List<_RecoilEntryData> yaw = recoilData.getYawRecoils();
            if (yaw.isEmpty()) return true;
            _RecoilEntryData prev = yaw.get(0);
            float[] r = prev.getRange();
            if (r.length < 2 || r[0] > r[1]) return true;
            for (int i = 1; i < yaw.size(); i++) {
                _RecoilEntryData cur = yaw.get(i);
                if (cur.getTime() < prev.getTime()) return true;
                float[] curR = cur.getRange();
                if (curR.length < 2 || curR[0] > curR[1]) return true;
                prev = cur;
            }
        }
        return false;
    }

    // --------Getter--------

    public GunData getGunData() {
        return this.gunDataCache;
    }
    public @Nullable LuaTable getScript() {
        return this.script;
    }
    public @Nullable LuaTable getScriptParams() {
        return this.scriptParamCache;
    }

    // --------Deprecated--------

    @Deprecated public int getSort() {
        return this.getPojo().getSlotSort();
    }
    @Deprecated public _BulletData getBulletData() {
        return this.gunDataCache.getBulletData();
    }
    @Deprecated public String getType() {
        return this.getPojo().getGunCategory().toString();
    }
}