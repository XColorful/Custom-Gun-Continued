/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.jse.JseStringLib;
import xiao.customgun.core.api.resource.FileExtensionType;
import xiao.customgun.core.api.resource.data.DataFolderName;
import xiao.customgun.core.api.resource.data.DataFolderType;
import xiao.customgun.core.api.script.LuaGunLogicLib;
import xiao.customgun.core.api.script.LuaLibrary;
import xiao.customgun.core.resource.ResourceFileManager;
import xiao.customgun.core.resource.data.script.DataScript;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ScriptManager extends ResourceFileManager<DataScript> {

    /**
     * Lua 虚拟机全局上下文沙箱
     */
    private static Globals GLOBALS;

    /**
     * 内置的 Lua 常量库扩展列表
     */
    private static final List<LuaLibrary> LIBRARIES = List.of(new LuaGunLogicLib());

    @ApiStatus.Internal
    public ScriptManager() {
        super(PackType.SERVER_DATA, Arrays.asList(DataFolderType.SCRIPT.getFolderName(), DataFolderName.SCRIPT_OLD1),
                FileExtensionType.LUA.getExtensionNameWithDot(),
                DataScript::fromStream);
        this.setValidateAtRead(false);
        this.setValidateAtApply(true);
    }

    @Override
    protected @NotNull Map<Identifier, DataScript> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        GLOBALS = secureStandardGlobals();
        return super.prepare(resourceManager, profiler);
    }

    @Override
    protected void onPrepareFile(Map<Identifier, DataScript> map, Identifier fileLocation, DataScript file) {
        super.onPrepareFile(map, fileLocation, file);

        String moduleName = getModuleName(fileLocation);
        GLOBALS.get("package").get("preload").set(moduleName, new LuaFunction() {
            @Override
            public LuaValue call(LuaValue modname, LuaValue env) {
                file.executeAndCache();
                return file.getResultTable() != null ? file.getResultTable() : LuaValue.NIL;
            }
        });
    }

    // --------Script Manager--------

    public static Globals getGlobals() {
        if (GLOBALS == null) {
            GLOBALS = secureStandardGlobals();
        }
        return GLOBALS;
    }

    public static String getModuleName(Identifier resourceLocation) {
        return resourceLocation.getNamespace() + "_" + resourceLocation.getPath();
    }

    private static Globals secureStandardGlobals() {
        Globals g = new Globals();
        g.load(new JseBaseLib());
        g.load(new PackageLib());
        g.load(new Bit32Lib());
        g.load(new TableLib());
        g.load(new JseStringLib());
        g.load(new JseMathLib());
        LoadState.install(g);
        LuaC.install(g);
        LIBRARIES.forEach(luaLibrary -> luaLibrary.install(g));
        return g;
    }
}