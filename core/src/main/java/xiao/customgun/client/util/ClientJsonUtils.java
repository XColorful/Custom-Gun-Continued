/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * 改成跟 BattleRoyale 同构的写法
 */

package xiao.customgun.client.util;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonToken;
import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import xiao.customgun.CustomGun;
import xiao.customgun.client.resource.assets.animation.bedrock.animation.bone._KeyFrame;
import xiao.customgun.core.util.JsonUtils;

import java.io.IOException;
import java.util.Map;

public class ClientJsonUtils {

    // --------扩展类型--------

    public static ItemTransforms readItemTransforms(JsonReader reader) throws IOException {
        return null;
    }
    public static void writeItemTransforms(JsonWriter writer, String key, ItemTransforms itemTransforms) throws IOException {
    }

    /**
     * 原 AnimationKeyframesSerializer 相关逻辑
     */
    public static Double2ObjectRBTreeMap<_KeyFrame> readKeyFrames(JsonReader reader) throws IOException {
        JsonToken peek = reader.peek();
        if (peek == JsonToken.NULL) {
            reader.nextNull();
            return new Double2ObjectRBTreeMap<>();
        }

        Double2ObjectRBTreeMap<_KeyFrame> treeMap = new Double2ObjectRBTreeMap<>();

        // 1. 如果是纯数字（如 "scale": 1.5），将其等价解包为 0.0s 时的三轴等值 [1.5, 1.5, 1.5]
        if (peek == JsonToken.NUMBER) {
            float val = (float) reader.nextDouble();
            treeMap.put(0.0d, _KeyFrame.ofStatic(new float[]{val, val, val}));
        }
        // 2. 如果是最外层直接写 Molang 字符串（原模组逻辑：拦截并丢弃，返回空时间轴）
        else if (peek == JsonToken.STRING) {
            String molang = reader.nextString();
            CustomGun.LOGGER.debug("Molang is not supported: {}", molang);
            return treeMap;
        }
        // 3. 常规数组："[100.0, 4.9, -179.1]" -> 转换为 0.0s 时间戳的静态关键帧
        else if (peek == JsonToken.BEGIN_ARRAY) {
            float[] data = readFloatArrayWithMolangTolerance(reader, 3);
            treeMap.put(0.0d, _KeyFrame.ofStatic(data));
        }
        // 4. 常规字典：{"0.25": [...], "0.4": {...}}
        else if (peek == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                String timeStr = reader.nextName();
                try {
                    // 原模组如果键不支持 Molang 会直接退出的逻辑，这里跳过或收尾
                    double time = Double.parseDouble(timeStr);
                    _KeyFrame frame = _KeyFrame.fromJson(reader);
                    if (frame != null) {
                        treeMap.put(time, frame);
                    }
                } catch (NumberFormatException e) {
                    // 键是 Molang 表达式情况，跳过该非法帧对应的值
                    reader.skipValue();
                }
            }
            reader.endObject();
        } else {
            reader.skipValue();
        }

        return treeMap;
    }

    /**
     * 辅助流式读取器：能够容错非标准浮点数组中夹杂的 Molang 表达式字符串
     */
    public static float[] readFloatArrayWithMolangTolerance(JsonReader reader, int expectLength) throws IOException {
        float[] arr = new float[expectLength];
        reader.beginArray();
        for (int i = 0; i < expectLength; i++) {
            if (reader.hasNext()) {
                if (reader.peek() == JsonToken.STRING) {
                    reader.nextString(); // 吃掉不认识的 Molang 文本
                    arr[i] = 0.0f;       // 降级软着陆
                } else {
                    arr[i] = (float) reader.nextDouble();
                }
            }
        }
        while (reader.hasNext()) {
            reader.skipValue(); // 阻断过长数据污染
        }
        reader.endArray();
        return arr;
    }

    public static void writeKeyFrames(JsonWriter writer, String key, Double2ObjectRBTreeMap<_KeyFrame> keyFrames) throws IOException {
        if (keyFrames == null) return;
        if (key != null) writer.name(key);

        // 微优化：如果仅包含一个 0.0s 且三轴相等的纯数据静态关键帧，可以直接写出为最简数字形态
        if (keyFrames.size() == 1 && keyFrames.containsKey(0.0d)) {
            _KeyFrame first = keyFrames.get(0.0d);
            if (first.getPre() == null && first.getPost() == null && first.getLerpMode() == null && first.getData() != null) {
                float[] d = first.getData();
                if (d.length == 3 && d[0] == d[1] && d[1] == d[2]) {
                    writer.value(d[0]);
                    return;
                }
                writer.beginArray(); {
                    for (float f : d) writer.value(f);
                }
                writer.endArray();
                return;
            }
        }

        writer.beginObject();
        for (Map.Entry<Double, _KeyFrame> entry : keyFrames.double2ObjectEntrySet()) {
            writer.name(String.valueOf(entry.getKey()));
            _KeyFrame.toJson(writer, entry.getValue());
        }
        writer.endObject();
    }
}