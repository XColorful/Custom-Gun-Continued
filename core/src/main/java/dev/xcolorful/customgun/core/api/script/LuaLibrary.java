package dev.xcolorful.customgun.core.api.script;

import org.luaj.vm2.LuaValue;

public interface LuaLibrary {

    void install(LuaValue chunk);
}
