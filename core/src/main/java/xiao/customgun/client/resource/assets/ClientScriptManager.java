/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets;

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
import xiao.customgun.client.api.resource.assets.AssetsFolderType;
import xiao.customgun.client.api.script.LuaAnimationLib;
import xiao.customgun.client.api.script.LuaGunAnimationLib;
import xiao.customgun.client.resource.assets.script.AssetsScript;
import xiao.customgun.core.api.resource.FileExtensionType;
import xiao.customgun.core.api.resource.assets.AssetsFolderName;
import xiao.customgun.core.api.script.LuaLibrary;
import xiao.customgun.core.resource.ResourceFileManager;
import xiao.customgun.core.resource.data.ScriptManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 同 {@link ScriptManager}
 */
public final class ClientScriptManager extends ResourceFileManager<AssetsScript> {

    /**
     * Lua 虚拟机全局上下文沙箱
     */
    private static Globals GLOBALS;

    /**
     * 内置的 Lua 常量库扩展列表
     */
    private static final List<LuaLibrary> LIBRARIES = List.of(new LuaAnimationLib(), new LuaGunAnimationLib());

    @ApiStatus.Internal
    public ClientScriptManager() {
        super(PackType.CLIENT_RESOURCES, Arrays.asList(AssetsFolderType.SCRIPT.getFolderName(), AssetsFolderName.SCRIPT_OLD1),
                FileExtensionType.LUA.getExtensionNameWithDot(),
                AssetsScript::fromStream);
        this.setValidateAtRead(false);
        this.setValidateAtApply(true);
    }

    @Override
    protected @NotNull Map<Identifier, AssetsScript> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        GLOBALS = secureStandardGlobals();
        return super.prepare(resourceManager, profiler);
    }

    @Override
    protected void onPrepareFile(Map<Identifier, AssetsScript> map, Identifier fileLocation, AssetsScript file) {
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
