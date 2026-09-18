package dev.xcolorful.customgun.core.init.registry;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.sound.gun.GunSoundType;
import dev.xcolorful.customgun.core.api.init.registry.IRegistrar;
import dev.xcolorful.customgun.core.api.init.registry.IRegistryObject;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final IRegistrar<SoundEvent> SOUNDS =
            CustomGun.getRegistrarFactory().createSounds(CustomGun.MOD_ID);


    /**
     * TODO 等mixin做完之后给每个 {@link GunSoundType} 都做一个注册类型，用ResourceLocation path前缀检测来生成对应的声音事件
     */
    public static final IRegistryObject<SoundEvent> GUN = SOUNDS.register("gun", () ->
            SoundEvent.createVariableRangeEvent(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "gun"))));
    public static final IRegistryObject<SoundEvent> TARGET_HIT = SOUNDS.register("target_block_hit", () ->
            SoundEvent.createVariableRangeEvent(CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "target_block_hit"))));
}