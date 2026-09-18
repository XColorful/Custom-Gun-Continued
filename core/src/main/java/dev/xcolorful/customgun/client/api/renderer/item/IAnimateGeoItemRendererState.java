package dev.xcolorful.customgun.client.api.renderer.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IAnimateGeoItemRendererState {

    /**
     * 尝试初始化状态机并触发切入信号
     */
    void tryInit(ItemStack itemStack, Player player, float partialTicks);

    /**
     * 尝试退出状态机并触发切出信号
     */
    void tryExit(ItemStack itemStack, long putAwayTime);

    /**
     * 尝试触发状态机转移
     * @param input 输入信号
     */
    void triggerAnimation(ItemStack itemStack, String input);

    boolean needReInit(ItemStack itemStack);

    /**
     * 更新状态机但是不进行模型写入，用于播放音效
     */
    void visualUpdate(ItemStack itemStack);
}
