package dev.xcolorful.customgun.client.sound.gun;

import dev.xcolorful.customgun.client.api.sound.gun.GunSoundType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 何意味?
 */
@Deprecated(forRemoval = true)
public final class GunSoundPreload {
    public static final List<String> DEFAULT_PRELOAD_NAMES = Arrays.stream(GunSoundType.values())
            .filter(type -> type.preload)
            .map(GunSoundType::getTagName)
            .collect(Collectors.toList());

    private GunSoundPreload() {
    }
}
