package xiao.customgun.forgeclient.minecraft.access;

import net.minecraft.client.Minecraft;
import xiao.customgun.client.api.minecraft.access.IClientAccessTransformer;

public class ForgeClientAccessTransformer implements IClientAccessTransformer {

    @Override public void
    startUseItem(
            Minecraft minecraft
    ) {
        minecraft.startUseItem();
    }
}
