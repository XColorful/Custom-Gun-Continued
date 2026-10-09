package dev.xcolorful.customgun.core.resource.data;

import dev.xcolorful.customgun.core.api.resource.FileExtensionType;
import dev.xcolorful.customgun.core.api.resource.data.DataFolderName;
import dev.xcolorful.customgun.core.api.resource.data.DataFolderType;
import dev.xcolorful.customgun.core.api.script.LuaGunLogicLib;
import dev.xcolorful.customgun.core.api.script.LuaLibrary;
import dev.xcolorful.customgun.core.resource.ResourceFileManager;
import dev.xcolorful.customgun.core.resource.data.script.DataScript;
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
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.jse.JseStringLib;

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

    /**
     * 严格档禁用的全局
     */
    private static final String[] UNSAFE_GLOBALS = {
            "collectgarbage",
            "dofile",
            "getmetatable",
            "load",
            "loadfile",
            "print"
    };

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
        GLOBALS = secureStandardGlobals(LIBRARIES);
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
            GLOBALS = secureStandardGlobals(LIBRARIES);
        }
        return GLOBALS;
    }

    public static String getModuleName(Identifier resourceLocation) {
        return resourceLocation.getNamespace() + "_" + resourceLocation.getPath();
    }

    @ApiStatus.Internal
    public static Globals secureStandardGlobals(List<LuaLibrary> LIBRARIES) {
        return secureGlobals(LIBRARIES, false);
    }

    /**
     * 严格档：仅表达式所需的最小环境
     * <ul>
     *     <li>不加载 PackageLib / LoadState，也不提供 require、dofile、loadfile、load、print</li>
     *     <li>可供一般脚本计算复用 (如 {@link dev.xcolorful.customgun.core.util.ScriptUtils})</li>
     * </ul>
     */
    @ApiStatus.Internal
    public static Globals secureExpressionGlobals() {
        return secureGlobals(List.of(), true);
    }

    private static Globals secureGlobals(List<LuaLibrary> libraries, boolean strict) {
        Globals g = new Globals();
        if (strict) {
            g.load(new BaseLib());
        } else {
            g.load(new JseBaseLib());
            g.load(new PackageLib());
        }
        g.load(new Bit32Lib());
        g.load(new TableLib());
        g.load(new JseStringLib());
        g.load(new JseMathLib());
        if (strict) {
            for (String name : UNSAFE_GLOBALS) {
                g.set(name, LuaValue.NIL);
            }
        } else {
            LoadState.install(g);
            libraries.forEach(luaLibrary -> luaLibrary.install(g));
        }
        LuaC.install(g);
        return g;
    }
}