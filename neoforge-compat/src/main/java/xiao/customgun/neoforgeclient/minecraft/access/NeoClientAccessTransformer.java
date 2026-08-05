package xiao.customgun.neoforgeclient.minecraft.access;

import net.minecraft.client.Minecraft;
import xiao.customgun.client.api.minecraft.access.IClientAccessTransformer;

public class NeoClientAccessTransformer implements IClientAccessTransformer {

    @Override public void
    startUseItem(
            Minecraft minecraft
    ) {
        minecraft.startUseItem();
    }
}
