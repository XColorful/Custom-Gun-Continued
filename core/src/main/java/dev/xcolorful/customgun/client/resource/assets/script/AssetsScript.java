/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets.script;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.resource.assets.ClientScriptManager;
import dev.xcolorful.customgun.core.resource.ResourceFile;
import dev.xcolorful.customgun.core.resource.data.script.DataScript;
import net.minecraft.resources.Identifier;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 同 {@link DataScript}
 */
public final class AssetsScript extends ResourceFile<AssetsScript> {

    /**
     * 经流式词法分析流编译后生成的匿名函数代码块 (Chunk)
     */
    private LuaValue chunk;
    /**
     * 执行 Chunk 后导出的公共环境数据表 (Table)
     */
    private LuaTable resultTable;

    private static final AssetsScript PARSER = new AssetsScript();
    public static AssetsScript fromStream(InputStream inputStream, Identifier fileLocation) throws IOException {
        return PARSER.fromInputStream(inputStream, fileLocation);
    }

    @Override
    protected AssetsScript fromInputStream(InputStream inputStream, Identifier fileLocation) throws IOException {
        AssetsScript file = new AssetsScript();
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            var globals = ClientScriptManager.getGlobals();
            if (globals != null) {
                // 将真实的资源路径作为 chunkname 喂给 Luaj，让报错堆栈定位到具体文件
                String chunkDebugName = "@" + fileLocation.getNamespace() + "/" + fileLocation.getPath();
                file.chunk = globals.load(reader, chunkDebugName);
            }
        } catch (Exception e) {
            throw new IOException("Failed to compile lua script source at: " + fileLocation, e);
        }
        return file;
    }

    public static void toFile(OutputStream outputStream, AssetsScript file) throws UnsupportedOperationException, IOException {
        if (file != null) file.toFile(outputStream);
    }
    @Override
    public void toFile(OutputStream outputStream) throws UnsupportedOperationException, IOException {
        throw new UnsupportedOperationException("Serialization of compiled Lua chunks is not supported.");
    }

    @Override
    protected void validateFile() {
        boolean n1 = (this.chunk == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        // 单线程安全期触发业务激活，避免 require() 相互依赖加载
        this.executeAndCache();

        boolean n2 = (this.resultTable == null);
        if (n2) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public LuaValue getChunk() {
        return chunk;
    }
    public LuaTable getResultTable() {
        return resultTable;
    }

    public void setChunk(LuaValue chunk) {
        this.chunk = chunk;
    }
    public void setResultTable(LuaTable resultTable) {
        this.resultTable = resultTable;
    }

    // --------Script Data--------

    /**
     * 执行编译好的 chunk 并拿到导出的 LuaTable
     */
    public void executeAndCache() {
        if (this.chunk != null && this.resultTable == null) {
            try {
                this.resultTable = this.chunk.call().checktable(1);
            } catch (Exception e) {
                CustomGun.LOGGER.error("Failed to execute script chunk", e);
            }
        }
    }

    // --------Back compatibility--------
}