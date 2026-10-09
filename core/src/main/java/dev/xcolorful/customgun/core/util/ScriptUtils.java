package dev.xcolorful.customgun.core.util;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.resource.data.ScriptManager;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.script.LuaScriptEngineFactory;

import javax.script.ScriptEngine;
import java.util.Locale;

public class ScriptUtils {

    /**
     * 每个线程独立持有脚本引擎
     * @deprecated 改成直接用 {@link #LUA_GLOBALS}
     */
    @Deprecated(forRemoval = true)
    public static final ThreadLocal<ScriptEngine> LUAJ_ENGINE = ThreadLocal.withInitial(() -> new LuaScriptEngineFactory().getScriptEngine());
    /**
     * 每个线程独立持有脚本沙箱
     */
    public static final ThreadLocal<Globals> LUA_GLOBALS = ThreadLocal.withInitial(ScriptManager::secureExpressionGlobals);

    /**
     * @param base 原始值 r
     * @param value 输入变量/当前计算值 x
     * @param function 函数 f
     * @return y = f(r, x)
     */
    public static float eval(float base, float value, String function) {
        Globals globals = LUA_GLOBALS.get();

        function = function.toLowerCase(Locale.ENGLISH);
        globals.rawset("x", LuaValue.valueOf(value));
        globals.rawset("r", LuaValue.valueOf(base));
        globals.rawset("y", LuaValue.NIL);
        try {
            globals.load(function,
//                    "script" // 默认名称是script
                    "ScriptUtils_eval" // 但标识在ScriptUtils里执行的
            ).call();
        } catch (Exception e) {
            CustomGun.LOGGER.error("ScriptUtils: Error while executing {}: ", function, e);
        }
        LuaValue result = globals.rawget("y");
        return result.isnumber() ? (float) result.todouble() : value;
    }
}
