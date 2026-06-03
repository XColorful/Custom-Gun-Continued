/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets.display;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.math.Transformation;
import xiao.customgun.client.util.ClientJsonUtils;
import xiao.customgun.core.api.resource.assets.display.BlockDisplayTag;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;

public final class BlockDisplay extends _AssetsDisplay<BlockDisplay> {

    // TODO 处理 26.1.2 没有 ItemTransforms 的问题
    private Transformation itemTransforms;

    private static final BlockDisplay PARSER = new BlockDisplay();
    public static BlockDisplay fromJson(JsonReader reader) throws IOException {
        return PARSER.fromJsonReader(reader);
    }
    @Override
    protected BlockDisplay fromJsonReader(JsonReader reader) throws IOException {
        BlockDisplay pojo = new BlockDisplay();
        reader.beginObject(); {
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case BlockDisplayTag.MODEL_LOCATION, BlockDisplayTag.MODEL_LOCATION_OLD1 -> pojo.setModelLocation(JsonUtils.readResourceLocation(reader));
                    case BlockDisplayTag.TEXTURE_LOCATION, BlockDisplayTag.TEXTURE_LOCATION_OLD1 -> pojo.setTextureLocation(JsonUtils.readResourceLocation(reader));

                    case BlockDisplayTag.ITEM_TRANSFORMS, BlockDisplayTag.ITEM_TRANSFORMS_OLD1 -> pojo.itemTransforms = ClientJsonUtils.readItemTransforms(reader);
                    default -> reader.skipValue();
                }
            }
        }
        reader.endObject();
        return pojo;
    }

    public static void toJson(JsonWriter writer, BlockDisplay pojo) throws IOException {
        if (pojo != null) pojo.toJson(writer);
    }
    @Override
    public void toJson(JsonWriter writer) throws IOException {
        writer.beginObject(); {
            JsonUtils.writeResourceLocation(writer, BlockDisplayTag.MODEL_LOCATION, this.getModelLocation());
            JsonUtils.writeResourceLocation(writer, BlockDisplayTag.TEXTURE_LOCATION, this.getTextureLocation());

            ClientJsonUtils.writeItemTransforms(writer, BlockDisplayTag.ITEM_TRANSFORMS, this.itemTransforms);
        }
        writer.endObject();
    }

    @Override
    protected void validatePojo() {
        super.validatePojo();
        if (!this.isValid()) return;

        boolean n1 = (this.itemTransforms == null);
        if (n1) {
            this.setValid(false);
            return;
        }

        this.setValid(true);
    }

    // --------Getter & Setter--------

    public Transformation getItemTransforms() {
        return itemTransforms;
    }

    public void setItemTransforms(Transformation itemTransforms) {
        this.itemTransforms = itemTransforms;
    }

    // --------Back compatibility--------
}