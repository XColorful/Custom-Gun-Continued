/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.util;

import dev.xcolorful.customgun.CustomGun;
import org.luaj.vm2.script.LuaScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Locale;

public class ScriptUtils {
    /**
     * 每个线程独立持有脚本引擎
     */
    public static final ThreadLocal<ScriptEngine> LUAJ_ENGINE = ThreadLocal.withInitial(() -> new LuaScriptEngineFactory().getScriptEngine());

    /**
     * @param base 原始值 r
     * @param value 输入变量/当前计算值 x
     * @param function 函数 f
     * @return y = f(r, x)
     */
    public static float eval(float base, float value, String function) {
        ScriptEngine scriptEngine = LUAJ_ENGINE.get();

        function = function.toLowerCase(Locale.ENGLISH);
        scriptEngine.put("x", value);
        scriptEngine.put("r", base);
        scriptEngine.put("y", null);
        try {
            scriptEngine.eval(function);
        } catch (ScriptException e) {
            CustomGun.LOGGER.error("ScriptUtils: Error while executing {}: ", function, e);
        }
        if (scriptEngine.get("y") instanceof Number number) {
            return number.floatValue();
        }
        return value;
    }
}
