/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data.recipefilter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;
import xiao.customgun.core.api.resource.data.recipefilter.RecipeFilterDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class RecipeFilterData extends ResourcePojo<RecipeFilterData> {

    private List<String> whitelistRaw;
    private List<ResourceLocation> whitelistLiteral;
    private List<Pattern> whitelistPattern;

    private List<String> blacklistRaw;
    private List<ResourceLocation> blacklistLiteral;
    private List<Pattern> blacklistPattern;

    private static final RecipeFilterData PARSER = new RecipeFilterData();
    public static RecipeFilterData fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected RecipeFilterData fromJsonReader(JsonReader reader) throws IOException {
        RecipeFilterData pojo = new RecipeFilterData();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case RecipeFilterDataTag.WHITELIST -> pojo.whitelistRaw = JsonUtils.readStringList(reader);
                    case RecipeFilterDataTag.BLACKLIST -> pojo.blacklistRaw = JsonUtils.readStringList(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, RecipeFilterData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeStringList(writer, RecipeFilterDataTag.WHITELIST, this.whitelistRaw);
            JsonUtils.writeStringList(writer, RecipeFilterDataTag.BLACKLIST, this.blacklistRaw);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        // TODO: 在这里处理 String 到 ResourceLocation 或 Pattern 的转换逻辑
        this.setValid(true);
    }

    // --------Getter & Setter--------

    public List<String> getWhitelistRaw() {
        return whitelistRaw;
    }
    public List<ResourceLocation> getWhitelistLiteral() {
        return whitelistLiteral;
    }
    public List<Pattern> getWhitelistPattern() {
        return whitelistPattern;
    }
    public List<String> getBlacklistRaw() {
        return blacklistRaw;
    }
    public List<ResourceLocation> getBlacklistLiteral() {
        return blacklistLiteral;
    }
    public List<Pattern> getBlacklistPattern() {
        return blacklistPattern;
    }

    public void setWhitelistRaw(List<String> whitelistRaw) {
        this.whitelistRaw = whitelistRaw;
    }
    public void setWhitelistLiteral(List<ResourceLocation> whitelistLiteral) {
        this.whitelistLiteral = whitelistLiteral;
    }
    public void setWhitelistPattern(List<Pattern> whitelistPattern) {
        this.whitelistPattern = whitelistPattern;
    }
    public void setBlacklistRaw(List<String> blacklistRaw) {
        this.blacklistRaw = blacklistRaw;
    }
    public void setBlacklistLiteral(List<ResourceLocation> blacklistLiteral) {
        this.blacklistLiteral = blacklistLiteral;
    }
    public void setBlacklistPattern(List<Pattern> blacklistPattern) {
        this.blacklistPattern = blacklistPattern;
    }
}