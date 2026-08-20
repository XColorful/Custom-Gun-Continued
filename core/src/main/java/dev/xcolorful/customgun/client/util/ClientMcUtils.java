package dev.xcolorful.customgun.client.util;

import net.minecraft.client.Minecraft;

public class ClientMcUtils {

    public static void schedule(Minecraft mc, Runnable task){
        mc.tell(task); // mc.schedule(task);
    }
}
