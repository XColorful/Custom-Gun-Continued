package dev.xcolorful.customgun.forgeclient.compat.forge.gui;

import dev.xcolorful.customgun.CustomGun;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomGun.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GuiLayerRegistry {

    private static ForgeLayeredDraw lastForgeLayeredDraw;
    private static final Map<LayeredDraw.Layer, ResourceLocation> cache = new HashMap<>();

    private static Field namedLayersField;
    private static Field subLayerStacksField;

    static {
        try {
            namedLayersField = ForgeLayeredDraw.class.getDeclaredField("namedLayers");
            namedLayersField.setAccessible(true);

            subLayerStacksField = ForgeLayeredDraw.class.getDeclaredField("subLayerStacks");
            subLayerStacksField.setAccessible(true);
        } catch (Exception e) {
            CustomGun.LOGGER.error("GuiLayerRegistry: Failed to initialize reflection fields for ForgeLayeredDraw", e);
        }
    }

    @SubscribeEvent
    public static void onRegisterLayers(AddGuiOverlayLayersEvent event) {
        clearCache();
        lastForgeLayeredDraw = event.getLayeredDraw();
    }

    public static @Nullable ForgeLayeredDraw getLastForgeLayeredDraw() {
        return lastForgeLayeredDraw;
    }

    @ApiStatus.Internal
    public static void clearCache() {
        lastForgeLayeredDraw = null;
        cache.clear();
    }

    public static @Nullable ResourceLocation getRegistryLocation(LayeredDraw.Layer layer) {
        if (lastForgeLayeredDraw == null) return null;

        if (cache.containsKey(layer)) {
            return cache.get(layer);
        }

        @Nullable ResourceLocation registryLocation = _getRegistryLocation(lastForgeLayeredDraw, layer);
        cache.put(layer, registryLocation);
        return registryLocation;
    }

    @SuppressWarnings("unchecked")
    private static @Nullable ResourceLocation _getRegistryLocation(@NotNull ForgeLayeredDraw forgeLayeredDraw, LayeredDraw.Layer layer) {
        try {
            if (namedLayersField != null) {
                Map<ResourceLocation, LayeredDraw.Layer> namedLayers = (Map<ResourceLocation, LayeredDraw.Layer>) namedLayersField.get(forgeLayeredDraw);
                for (Map.Entry<ResourceLocation, LayeredDraw.Layer> entry : namedLayers.entrySet()) {
                    if (entry.getValue() == layer) {
                        return entry.getKey();
                    }
                }
            }

            if (subLayerStacksField != null) {
                Map<ResourceLocation, Map.Entry<LayeredDraw, BooleanSupplier>> subLayerStacks =
                        (Map<ResourceLocation, Map.Entry<LayeredDraw, BooleanSupplier>>) subLayerStacksField.get(forgeLayeredDraw);

                for (Map.Entry<LayeredDraw, BooleanSupplier> entry : subLayerStacks.values()) {
                    if (entry.getKey() instanceof ForgeLayeredDraw childStack) {
                        ResourceLocation location = _getRegistryLocation(childStack, layer);
                        if (location != null) {
                            return location;
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            CustomGun.LOGGER.error("GuiLayerRegistry: Failed to access ForgeLayeredDraw fields via reflection", e);
        }

        return null;
    }
}
