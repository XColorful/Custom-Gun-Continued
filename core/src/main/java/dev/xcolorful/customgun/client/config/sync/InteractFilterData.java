package dev.xcolorful.customgun.client.config.sync;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.resource.data.tag.InteractKeyType;
import dev.xcolorful.customgun.core.config.SyncConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class InteractFilterData {
    /**
     * 默认给用 -> 默认一般玩家不知道去哪设置
     * 默认不给用 -> 避免所有普通方块也显示 (更重要)
     */
    private static volatile boolean DEFAULT_RESULT = false;
    // 方块
    private static volatile Map<Identifier, Boolean> BLOCK_FILTER = new HashMap<>();
    // 实体
    private static volatile Map<Identifier, Boolean> ENTITY_FILTER = new HashMap<>();

    public static void reloadInteractFilter() {
        IMcRegistry mcRegistry = CustomGun.getMcRegistry();

        // 先构建好再整体换引用：读方不会看到清空到一半的过滤器
        Map<ResourceLocation, Boolean> blocks = new HashMap<>();
        Map<ResourceLocation, Boolean> entities = new HashMap<>(); {
            // 方块
            for (String blockEntry : SyncConfig.INTERACT_KEY_BLACKLIST_BLOCKS.get())
                blocks.put(mcRegistry.createResourceLocation(blockEntry), false);
            for (String blockEntry : SyncConfig.INTERACT_KEY_WHITELIST_BLOCKS.get())
                blocks.put(mcRegistry.createResourceLocation(blockEntry), true);

            // 实体
            for (String entityEntry : SyncConfig.INTERACT_KEY_BLACKLIST_ENTITIES.get())
                entities.put(mcRegistry.createResourceLocation(entityEntry), false);
            for (String entityEntry : SyncConfig.INTERACT_KEY_WHITELIST_ENTITIES.get())
                entities.put(mcRegistry.createResourceLocation(entityEntry), true);
        }
        BLOCK_FILTER = blocks;
        ENTITY_FILTER = entities;
    }

    @ApiStatus.Internal
    public static boolean addBlockFilter(Identifier rl, @Nullable Boolean allowed) {
        // 已发布的表不再原地改，复制一份改完再换上去
        var rebuilt = new HashMap<>(BLOCK_FILTER);
        boolean changed = allowed == null ? rebuilt.remove(rl) != null : rebuilt.put(rl, allowed) != null;
        BLOCK_FILTER = rebuilt;
        return changed;
    }
    public static void setDefaultResult(boolean result) {
        DEFAULT_RESULT = result;
    }

    public static boolean canInteract(BlockState blockState) {
        // 避免空气干扰
        if (blockState.isAir()) return false;

        Block block = blockState.getBlock();
        // ResourceLocation 过滤
        Boolean allowed = BLOCK_FILTER.get(CustomGun.getMcRegistry().getBlockRl(block));
        if (allowed != null) return allowed;
        // TagKey 过滤
        if (blockState.is(InteractKeyType.BLOCK.getBlacklist())) return false;
        if (blockState.is(InteractKeyType.BLOCK.getWhitelist())) return true;
        // 默认结果
        return DEFAULT_RESULT;
    }
    public static boolean canInteract(Entity entity) {
        EntityType<?> type = entity.getType();
        // ResourceLocation 过滤
        Boolean allowed = ENTITY_FILTER.get(CustomGun.getMcRegistry().getEntityTypeRl(type));
        if (allowed != null) return allowed;
        // TagKey 过滤
        if (entity.is(InteractKeyType.ENTITY.getBlacklist())) return false;
        if (entity.is(InteractKeyType.ENTITY.getWhitelist())) return true;
        // 默认结果
        return DEFAULT_RESULT;
    }
}
