package dev.xcolorful.customgun.core.resource.data.recipefilter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.resource.IResourcePojoExtension;
import dev.xcolorful.customgun.core.api.resource.data.recipefilter.RecipeFilterDataTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.util.JsonUtils;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class RecipeFilterData extends ResourcePojo<RecipeFilterData>
        implements IResourcePojoExtension.Appendable<RecipeFilterData> {

    private List<String> whitelistRaw;
    private List<Identifier> _whitelistLiteral;
    private List<Pattern> _whitelistPattern;

    private List<String> blacklistRaw;
    private List<Identifier> _blacklistLiteral;
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
                    default -> this.readCustomKey(reader, key);
                }
            }
        }
        reader.endObject();
        return pojo;
    }
    @Override
    protected boolean readCustomKey(JsonReader reader, String key) throws IOException {
        return super.readCustomKey(reader, key);
    }

    public static void toJson(JsonWriter writer, RecipeFilterData pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeStringList(writer, RecipeFilterDataTag.WHITELIST, this.whitelistRaw);
            JsonUtils.writeStringList(writer, RecipeFilterDataTag.BLACKLIST, this.blacklistRaw);
            writeCustomValue(writer);
        }
        writer.endObject();
    }
    @Override
    protected void writeCustomValue(JsonWriter writer) {
        super.writeCustomValue(writer);
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
    private static List<Identifier> parseListToLiteral(IMcRegistry mcRegistry, List<String> raw) {
        List<Identifier> result = new ArrayList<>();
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

    @Override
    protected RecipeFilterData copyTo(final RecipeFilterData pojo) {
        List<String> whitelistRaw = new ArrayList<>(this.whitelistRaw.size()); {
            for (int i = 0; i < this.whitelistRaw.size(); i++) {
                whitelistRaw.add(this.whitelistRaw.get(i));
            }
        } pojo.setWhitelistRaw(whitelistRaw);
        List<Identifier> _whitelistLiteral = new ArrayList<>(this._whitelistLiteral.size()); {
            for (int i = 0; i < this._whitelistLiteral.size(); i++) {
                _whitelistLiteral.add(this._whitelistLiteral.get(i));
            }
        } pojo.setWhitelistLiteral(_whitelistLiteral);
        List<Pattern> _whitelistPattern = new ArrayList<>(this._whitelistPattern.size()); {
            for (int i = 0; i < this._whitelistPattern.size(); i++) {
                _whitelistPattern.add(this._whitelistPattern.get(i));
            }
        } pojo.setWhitelistPattern(_whitelistPattern);

        List<String> blacklistRaw = new ArrayList<>(this.blacklistRaw.size()); {
            for (int i = 0; i < this.blacklistRaw.size(); i++) {
                blacklistRaw.add(this.blacklistRaw.get(i));
            }
        } pojo.setBlacklistRaw(blacklistRaw);
        List<Identifier> _blacklistLiteral = new ArrayList<>(this._blacklistLiteral.size()); {
            for (int i = 0; i < this._blacklistLiteral.size(); i++) {
                _blacklistLiteral.add(this._blacklistLiteral.get(i));
            }
        } pojo.setBlacklistLiteral(_blacklistLiteral);
        List<Pattern> _blacklistPattern = new ArrayList<>(this._blacklistPattern.size()); {
            for (int i = 0; i < this._blacklistPattern.size(); i++) {
                _blacklistPattern.add(this._blacklistPattern.get(i));
            }
        } pojo.setBlacklistPattern(_blacklistPattern);
        return pojo;
    }

    @Override
    public RecipeFilterData copy() {
        RecipeFilterData pojo = copyTo(new RecipeFilterData());
        pojo.setValid(this.isValid());
        return pojo;
    }

    // --------Getter & Setter--------

    public List<String> getWhitelistRaw() {
        return whitelistRaw;
    }
    public List<Identifier> getWhitelistLiteral() {
        return _whitelistLiteral;
    }
    public List<Pattern> getWhitelistPattern() {
        return _whitelistPattern;
    }
    public List<String> getBlacklistRaw() {
        return blacklistRaw;
    }
    public List<Identifier> getBlacklistLiteral() {
        return _blacklistLiteral;
    }
    public List<Pattern> getBlacklistPattern() {
        return _blacklistPattern;
    }

    public void setWhitelistRaw(List<String> whitelistRaw) {
        this.whitelistRaw = whitelistRaw;
    }
    public void setWhitelistLiteral(List<Identifier> whitelistLiteral) {
        this._whitelistLiteral = whitelistLiteral;
    }
    public void setWhitelistPattern(List<Pattern> whitelistPattern) {
        this._whitelistPattern = whitelistPattern;
    }
    public void setBlacklistRaw(List<String> blacklistRaw) {
        this.blacklistRaw = blacklistRaw;
    }
    public void setBlacklistLiteral(List<Identifier> blacklistLiteral) {
        this._blacklistLiteral = blacklistLiteral;
    }
    public void setBlacklistPattern(List<Pattern> blacklistPattern) {
        this._blacklistPattern = blacklistPattern;
    }

    // --------IResourcePojoExtension--------

    @Override
    public RecipeFilterData asPojo() {
        return this;
    }

    @Override
    public void appendRaw(RecipeFilterData pojo) {
        this.whitelistRaw.addAll(pojo.whitelistRaw);
        this._whitelistLiteral.addAll(pojo._whitelistLiteral);
        this._whitelistPattern.addAll(pojo._whitelistPattern);

        this.blacklistRaw.addAll(pojo.blacklistRaw);
        this._blacklistLiteral.addAll(pojo._blacklistLiteral);
        this._blacklistPattern.addAll(pojo._blacklistPattern);
    }

    // --------Back compatibility--------

    @Override
    public RecipeFilterData applyBackCompatibility() {
        this.whitelistRaw = this.whitelistRaw == null ? new ArrayList<>() : this.whitelistRaw;
        this.blacklistRaw = this.blacklistRaw == null ? new ArrayList<>() : this.blacklistRaw;
        return this;
    }
}