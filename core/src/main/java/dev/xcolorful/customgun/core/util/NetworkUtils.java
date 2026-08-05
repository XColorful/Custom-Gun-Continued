package dev.xcolorful.customgun.core.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Map;

public class NetworkUtils {

    public static void writeItem(FriendlyByteBuf buffer, ItemStack item){
        buffer.writeItem(item);
    }
    public static ItemStack readItem(FriendlyByteBuf buffer){
        return buffer.readItem();
    }

    public static void writeResourceLocation(FriendlyByteBuf buffer, ResourceLocation location){
        buffer.writeResourceLocation(location);
    }
    public static ResourceLocation readResourceLocation(FriendlyByteBuf buffer){
        return buffer.readResourceLocation();
    }

    public static void writeUtf(FriendlyByteBuf buffer, String string){
        buffer.writeUtf(string);
    }
    public static String readUtf(FriendlyByteBuf buffer){
        return buffer.readUtf();
    }

    public static void writeIngredient(FriendlyByteBuf buffer, Ingredient ingredient){
        ingredient.toNetwork(buffer);
    }
    public static Ingredient readIngredient(FriendlyByteBuf buffer){
        return Ingredient.fromNetwork(buffer);
    }

    public static void writeResourceLocationMap(FriendlyByteBuf buffer, Map<ResourceLocation, String> map) {
        buffer.writeMap(map, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeUtf);
    }
    public static Map<ResourceLocation, String> readResourceLocationMap(FriendlyByteBuf buffer) {
        return buffer.readMap(NetworkUtils::readResourceLocation, NetworkUtils::readUtf);
    }

    public static <K extends Enum<K>, V> void writeEnumMap(FriendlyByteBuf buffer, Map<K, V> map, FriendlyByteBuf.Writer<V> valueWriter) {
        buffer.writeMap(map, FriendlyByteBuf::writeEnum, valueWriter);
    }
    public static <K extends Enum<K>, V> Map<K, V> readEnumMap(FriendlyByteBuf buffer, Class<K> enumClass, FriendlyByteBuf.Reader<V> valueReader) {
        return buffer.readMap(buf -> buf.readEnum(enumClass), valueReader);
    }

    public static <K extends Enum<K>> void writeEnumIntMap(FriendlyByteBuf buffer, Map<K, Integer> map) {
        buffer.writeMap(map, FriendlyByteBuf::writeEnum, FriendlyByteBuf::writeInt);
    }
    public static <K extends Enum<K>> Map<K, Integer> readEnumIntMap(FriendlyByteBuf buffer, Class<K> enumClass) {
        return buffer.readMap(buf -> buf.readEnum(enumClass), FriendlyByteBuf::readInt);
    }
}
