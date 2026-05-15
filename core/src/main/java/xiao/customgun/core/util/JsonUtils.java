/*
 * 改成跟 BattleRoyale 同构的写法
 */

package xiao.customgun.core.util;

import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.minecraft.IMcRegistry;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    /**
     * 缓存字段，避免每次都重新拿
     * 其他模组不应该在模组主类初始化时调用 // TODO 集中处理需要缓存加速的类, 省得加if
     */
    public static final IMcRegistry mcRegistry = CustomGun.getMcRegistry();

    // --------JsonPrimitive--------

    public static boolean readBoolean(JsonReader reader) throws IOException {
        JsonToken peek = reader.peek();
        if (peek == JsonToken.BOOLEAN) return reader.nextBoolean();
        if (peek == JsonToken.NULL) reader.nextNull();
        else reader.skipValue();
        return false;
    }
    public static void writeBoolean(JsonWriter writer, String key, boolean value) throws IOException {
        writer.name(key).value(value);
    }
    public static int readInt(JsonReader reader) throws IOException {
        JsonToken peek = reader.peek();
        if (peek == JsonToken.NUMBER) return reader.nextInt();
        if (peek == JsonToken.NULL) reader.nextNull();
        else reader.skipValue();
        return 0;
    }
    public static void writeInt(JsonWriter writer, String key, int value) throws IOException {
        writer.name(key).value(value);
    }
    public static float readFloat(JsonReader reader) throws IOException {
        JsonToken peek = reader.peek();
        if (peek == JsonToken.NUMBER) return (float) reader.nextDouble();
        if (peek == JsonToken.NULL) reader.nextNull();
        else reader.skipValue();
        return 0.0f;
    }
    public static void writeFloat(JsonWriter writer, String key, float value) throws IOException {
        writer.name(key).value(value);
    }
    public static long readLong(JsonReader reader) throws IOException {
        JsonToken peek = reader.peek();
        if (peek == JsonToken.NUMBER) return reader.nextLong();
        if (peek == JsonToken.NULL) reader.nextNull();
        else reader.skipValue();
        return 0L;
    }
    public static void writeLong(JsonWriter writer, String key, long value) throws IOException {
        writer.name(key).value(value);
    }
    public static String readString(JsonReader reader) throws IOException {
        JsonToken peek = reader.peek();
        if (peek == JsonToken.STRING) return reader.nextString();
        if (peek == JsonToken.NULL) reader.nextNull();
        else reader.skipValue();
        return null;
    }
    public static void writeString(JsonWriter writer, String key, String value) throws IOException {
        if (value != null) writer.name(key).value(value);
    }

    public static Object readObject(JsonReader reader) throws IOException {
        JsonToken peek = reader.peek();
        return switch (peek) {
            case STRING -> reader.nextString();
            case NUMBER -> reader.nextDouble();
            case BOOLEAN -> reader.nextBoolean();
            case NULL -> { reader.nextNull(); yield null; }
            default -> { reader.skipValue(); yield null; }
        };
    }
    public static void writeObject(JsonWriter writer, Object value) throws IOException {
        if (value instanceof String s) writer.value(s);
        else if (value instanceof Number n) writer.value(n);
        else if (value instanceof Boolean b) writer.value(b);
        else writer.nullValue();
    }

    // --------代理 (用于POJO嵌套)--------

    @FunctionalInterface
    public interface ReadFunction<T> {
        T apply(JsonReader reader) throws IOException;
    }
    @FunctionalInterface
    public interface WriteAction<T> {
        void accept(JsonWriter writer, T value) throws IOException;
    }
    public static <T> T read(JsonReader reader, ReadFunction<T> function) throws IOException {
        return function.apply(reader);
    }
    public static <T> void write(JsonWriter writer, String key, T value, WriteAction<T> action) throws IOException {
        if (value == null) return;
        writer.name(key); action.accept(writer, value);
    }

    // --------扩展类型--------

    public static ResourceLocation readResourceLocation(JsonReader reader) throws IOException {
        String rl = readString(reader);
        return rl != null ? mcRegistry.createResourceLocation(rl) : null;
    }

    public static void writeResourceLocation(JsonWriter writer, String key, ResourceLocation value) throws IOException {
        if (value != null) writer.name(key).value(value.toString());
    }

    @FunctionalInterface
    public interface FromStringFunction<T> {
        T apply(String name);
    }
    public static <T> T readFromString(JsonReader reader, FromStringFunction<T> function) throws IOException {
        String valueStr = readString(reader);
        return valueStr != null ? function.apply(valueStr) : null;
    }
    public static <T> void writeToString(JsonWriter writer, String key, T value) throws IOException {
        if (value != null) {
            writer.name(key).value(value.toString());
        }
    }

    // --------结构<类型>--------

    public static ArrayList<String> readStringList(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return new ArrayList<>();
        }
        ArrayList<String> list = new ArrayList<>();
        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            reader.beginArray();
            while (reader.hasNext()) {
                if (reader.peek() == JsonToken.STRING) {
                    list.add(reader.nextString());
                } else {
                    reader.skipValue();
                }
            }
            reader.endArray();
        } else {
            reader.skipValue();
        }
        return list;
    }
    public static void writeStringList(JsonWriter writer, String key, List<String> value) throws IOException {
        if (value == null) {
            return;
        }
        writer.name(key); writer.beginArray(); {
            for (String s : value) {
                if (s != null) {
                    writer.value(s);
                }
            }
        }
        writer.endArray();
    }

    public static <T> ArrayList<T> readList(JsonReader reader, ReadFunction<T> function) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return new ArrayList<>();
        }
        ArrayList<T> list = new ArrayList<>();
        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            reader.beginArray();
            while (reader.hasNext()) {
                T item = function.apply(reader);
                if (item != null) list.add(item);
            }
            reader.endArray();
        } else {
            reader.skipValue();
        }
        return list;
    }
    public static <T> void writeList(JsonWriter writer, String key, List<T> value, WriteAction<T> action) throws IOException {
        if (value == null) return;
        writer.name(key); writer.beginArray(); {
            for (T t : value) {
                if (t != null) action.accept(writer, t);
            }
        }
        writer.endArray();
    }

    public static int[] readIntArray(JsonReader reader) throws IOException {
        List<Integer> list = readList(reader, JsonUtils::readInt);
        return list.stream().mapToInt(i -> i).toArray();
    }
    public static void writeIntArray(JsonWriter writer, String key, int[] value) throws IOException {
        if (value == null) return;
        writer.name(key).beginArray(); {
            for (int i : value) writer.value(i);
        }
        writer.endArray();
    }

    public static float[] readFloatArray(JsonReader reader) throws IOException {
        List<Float> list = readList(reader, JsonUtils::readFloat);
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
    public static void writeFloatArray(JsonWriter writer, String key, float[] value) throws IOException {
        if (value == null) return;
        writer.name(key).beginArray(); {
            for (float f : value) writer.value(f);
        }
        writer.endArray();
    }

    // --------结构<类型,类型>--------

    public static HashMap<String, String> readString2StringMap(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return new HashMap<>();
        }
        HashMap<String, String> map = new HashMap<>();
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                if (reader.peek() == JsonToken.STRING) {
                    map.put(key, reader.nextString());
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } else {
            reader.skipValue();
        }
        return map;
    }
    public static void writeString2StringMap(JsonWriter writer, String key, Map<String, String> map) throws IOException {
        if (map == null) {
            return;
        }
        writer.name(key); writer.beginObject(); {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) { // 仅写入有效的键值对
                    writer.name(entry.getKey()).value(entry.getValue());
                }
            }
        }
        writer.endObject();
    }

    public static <T> HashMap<String, T> readString2ObjectMap(JsonReader reader, ReadFunction<T> function) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return new HashMap<>();
        }
        HashMap<String, T> map = new HashMap<>();
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                T value = function.apply(reader);
                if (value != null) map.put(key, value);
            }
            reader.endObject();
        } else {
            reader.skipValue();
        }
        return map;
    }
    public static <T> void writeString2ObjectMap(JsonWriter writer, String key, Map<String, T> map, WriteAction<T> action) throws IOException {
        if (map == null) return;
        writer.name(key).beginObject(); {
            for (Map.Entry<String, T> entry : map.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    writer.name(entry.getKey());
                    action.accept(writer, entry.getValue());
                }
            }
        }
        writer.endObject();
    }
    public static <T> HashMap<ResourceLocation, T> readRl2ObjectMap(JsonReader reader, ReadFunction<T> function) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return new HashMap<>();
        }
        HashMap<ResourceLocation, T> map = new HashMap<>();
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                var key = mcRegistry.createResourceLocation(reader.nextName());
                T value = function.apply(reader);
                if (key != null && value != null) {
                    map.put(key, value);
                }
            }
            reader.endObject();
        } else {
            reader.skipValue();
        }
        return map;
    }
    public static <T> void writeRl2ObjectMap(JsonWriter writer, String key, Map<ResourceLocation, T> map, WriteAction<T> action) throws IOException {
        if (map == null) return;
        writer.name(key).beginObject(); {
            for (Map.Entry<ResourceLocation, T> entry : map.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    writer.name(entry.getKey().toString());
                    action.accept(writer, entry.getValue());
                }
            }
        }
        writer.endObject();
    }

    @FunctionalInterface
    public interface KeyFunction<K> {
        K apply(String name);
    }
    @FunctionalInterface
    public interface KeyWriterAction<K> {
        String apply(K key);
    }
    public static <K, V> HashMap<K, V> readObject2ObjectMap(JsonReader reader, KeyFunction<K> keyFunction, ReadFunction<V> valueFunction) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return new HashMap<>();
        }
        HashMap<K, V> map = new HashMap<>();
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                String keyName = reader.nextName();
                K key = keyFunction.apply(keyName);
                if (key != null) {
                    V value = valueFunction.apply(reader);
                    if (value != null) {
                        map.put(key, value);
                    }
                } else {
                    valueFunction.apply(reader);
                }
            }
            reader.endObject();
        } else {
            reader.skipValue();
        }
        return map;
    }
    public static <K, V> void writeObject2ObjectMap(JsonWriter writer, String key, Map<K, V> map, KeyWriterAction<K> keyAction, WriteAction<V> valueAction) throws IOException {
        if (map == null) return;
        writer.name(key).beginObject(); {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    String keyName = keyAction.apply(entry.getKey());
                    if (keyName != null) {
                        writer.name(keyName);
                        valueAction.accept(writer, entry.getValue());
                    }
                }
            }
        }
        writer.endObject();
    }

    // --------JsonObject--------

    public static JsonReader getAsReader(JsonObject jsonObject) {
        return jsonObject != null ? new JsonTreeReader(jsonObject) : getAsReaderSafe(null);
    }
    public static JsonReader getAsReaderSafe(@Nullable JsonObject jsonObject) {
        return new JsonReader(new StringReader(jsonObject != null ? jsonObject.toString() : "{}"));
    }

    // --------PojoManager--------

    @FunctionalInterface
    public interface FromJsonReader<T> {
        T apply(JsonReader reader) throws IOException;
    }
}
