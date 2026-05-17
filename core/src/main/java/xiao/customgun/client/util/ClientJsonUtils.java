/*
 * 改成跟 BattleRoyale 同构的写法
 */

package xiao.customgun.client.util;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.renderer.block.model.ItemTransforms;

import java.io.IOException;

public class ClientJsonUtils {

    // --------扩展类型--------

    public static ItemTransforms readItemTransforms(JsonReader reader) throws IOException {
        return null;
    }

    public static void writeItemTransforms(JsonWriter writer, String key, ItemTransforms itemTransforms) throws IOException {
    }
}
