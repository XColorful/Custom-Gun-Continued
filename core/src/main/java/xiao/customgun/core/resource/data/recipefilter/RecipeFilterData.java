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
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.minecraft.IMcRegistry;
import xiao.customgun.core.api.resource.data.recipefilter.RecipeFilterDataTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class RecipeFilterData extends ResourcePojo<RecipeFilterData> {

    private List<String> whitelistRaw;
    private List<ResourceLocation> _whitelistLiteral;
    private List<Pattern> _whitelistPattern;

    private List<String> blacklistRaw;
    private List<ResourceLocation> _blacklistLiteral;
    private List<Pattern> _blacklistPattern;

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
        if (ENABLE_BACK_COMPATIBILITY) this.applyBackCompatibility();
        boolean n1 = (this.whitelistRaw == null | this.blacklistRaw == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        IMcRegistry mcRegistry = CustomGun.getMcRegistry();
        this._whitelistLiteral = parseListToLiteral(mcRegistry, this.whitelistRaw);
        this._whitelistPattern = parseListToPattern(this.whitelistRaw);
        this._blacklistLiteral = parseListToLiteral(mcRegistry, this.blacklistRaw);
        this._blacklistPattern = parseListToPattern(this.blacklistRaw);
        this.setValid(true);
    }
    /**
     * 将不以 ^ 开头的字符串解析为 ResourceLocation
     */
    private static List<ResourceLocation> parseListToLiteral(IMcRegistry mcRegistry, List<String> raw) {
        List<ResourceLocation> result = new ArrayList<>();
        for (String entry : raw) {
            if (entry.startsWith("^")) continue;
            var rl = mcRegistry.createResourceLocation(entry);
            if (rl != null) result.add(rl);
        }
        return result;
    }
    /**
     * 将以 ^ 开头的字符串解析为正则为 Pattern
     */
    private static List<Pattern> parseListToPattern(List<String> raw) {
        List<Pattern> result = new ArrayList<>();
        for (String entry : raw) {
            if (!entry.startsWith("^")) continue;
            try {
                result.add(Pattern.compile(entry));
            } catch (PatternSyntaxException e) {
                CustomGun.LOGGER.error("Failed to parse regex filter: {}", entry, e);
            }
        }
        return result;
    }

    // --------Getter & Setter--------

    public List<String> getWhitelistRaw() {
        return whitelistRaw;
    }
    public List<ResourceLocation> getWhitelistLiteral() {
        return _whitelistLiteral;
    }
    public List<Pattern> getWhitelistPattern() {
        return _whitelistPattern;
    }
    public List<String> getBlacklistRaw() {
        return blacklistRaw;
    }
    public List<ResourceLocation> getBlacklistLiteral() {
        return _blacklistLiteral;
    }
    public List<Pattern> getBlacklistPattern() {
        return _blacklistPattern;
    }

    public void setWhitelistRaw(List<String> whitelistRaw) {
        this.whitelistRaw = whitelistRaw;
    }
    public void setWhitelistLiteral(List<ResourceLocation> whitelistLiteral) {
        this._whitelistLiteral = whitelistLiteral;
    }
    public void setWhitelistPattern(List<Pattern> whitelistPattern) {
        this._whitelistPattern = whitelistPattern;
    }
    public void setBlacklistRaw(List<String> blacklistRaw) {
        this.blacklistRaw = blacklistRaw;
    }
    public void setBlacklistLiteral(List<ResourceLocation> blacklistLiteral) {
        this._blacklistLiteral = blacklistLiteral;
    }
    public void setBlacklistPattern(List<Pattern> blacklistPattern) {
        this._blacklistPattern = blacklistPattern;
    }

    // --------Back compatibility--------

    @Override
    public RecipeFilterData applyBackCompatibility() {
        this.whitelistRaw = this.whitelistRaw == null ? new ArrayList<>() : this.whitelistRaw;
        this.blacklistRaw = this.blacklistRaw == null ? new ArrayList<>() : this.blacklistRaw;
        return this;
    }
}