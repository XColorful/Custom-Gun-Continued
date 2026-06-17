package xiao.customgun.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class RayTraceUtils {

    public static class BlockTrace {

        /**
         * 广义的、解耦的带方块过滤功能射线检测
         * @param getter 当前方块获取器
         * @param startPos 射线起点
         * @param endPos 射线终点
         * @param blockShapeFactory 方块碰撞形状计算工厂 (blockState, blockPos) -> VoxelShape
         * @param fluidShapeFactory 流体碰撞形状计算工厂 (fluidState, blockPos) -> VoxelShape
         * @param ignorePredicate 方块过滤器，返回 true 表示忽略当前方块
         */
        public static BlockHitResult rayTraceBlocksWithFilter(
                BlockGetter getter,
                Vec3 startPos,
                Vec3 endPos,
                BiFunction<BlockState, BlockPos, VoxelShape> blockShapeFactory,
                BiFunction<FluidState, BlockPos, VoxelShape> fluidShapeFactory,
                Predicate<BlockState> ignorePredicate
        ) {
            return traverseBlocks(startPos, endPos, null, (unused, blockPos) -> {
                BlockState blockState = getter.getBlockState(blockPos);
                if (ignorePredicate.test(blockState)) {
                    return null;
                }
                return getBlockHitResult(getter, startPos, endPos, blockPos, blockState, blockShapeFactory, fluidShapeFactory);
            }, (unused) -> {
                Vec3 delta = startPos.subtract(endPos);
                return BlockHitResult.miss(
                        endPos,
                        Direction.getApproximateNearest(delta.x, delta.y, delta.z),
                        BlockPos.containing(endPos)
                );
            });
        }

        /**
         * 计算当前方块与流体碰撞结果并返回距离最近的命中结果
         */
        private static @Nullable BlockHitResult getBlockHitResult(
                BlockGetter getter,
                Vec3 startPos,
                Vec3 endPos,
                BlockPos blockPos,
                BlockState blockState,
                BiFunction<BlockState, BlockPos, VoxelShape> blockShapeFactory,
                BiFunction<FluidState, BlockPos, VoxelShape> fluidShapeFactory
        ) {
            FluidState fluidState = getter.getFluidState(blockPos);
            VoxelShape blockShape = blockShapeFactory.apply(blockState, blockPos);
            BlockHitResult blockResult = getter.clipWithInteractionOverride(startPos, endPos, blockPos, blockShape, blockState);
            VoxelShape fluidShape = fluidShapeFactory.apply(fluidState, blockPos);
            BlockHitResult fluidResult = fluidShape.clip(startPos, endPos, blockPos);
            double blockDistanceSquared = blockResult == null ? Double.MAX_VALUE : startPos.distanceToSqr(blockResult.getLocation());
            double fluidDistanceSquared = fluidResult == null ? Double.MAX_VALUE : startPos.distanceToSqr(fluidResult.getLocation());
            return blockDistanceSquared <= fluidDistanceSquared ? blockResult : fluidResult;
        }

        /**
         * 基于 DDA 算法遍历射线路径上的所有方块
         * @see BlockGetter#traverseBlocks(Vec3, Vec3, Object, BiFunction, Function)
         */
        public static <T, C> T traverseBlocks(
                Vec3 startPos,
                Vec3 endPos,
                C context,
                BiFunction<C, BlockPos, @Nullable T> consumer,
                Function<C, T> missFactory
        ) {
            if (!startPos.equals(endPos)) {
                // 微调射线两端坐标，避免落在方块边界时产生浮点误差
                double endX = Mth.lerp(-1.0E-7, endPos.x, startPos.x);
                double endY = Mth.lerp(-1.0E-7, endPos.y, startPos.y);
                double endZ = Mth.lerp(-1.0E-7, endPos.z, startPos.z);
                double startX = Mth.lerp(-1.0E-7, startPos.x, endPos.x);
                double startY = Mth.lerp(-1.0E-7, startPos.y, endPos.y);
                double startZ = Mth.lerp(-1.0E-7, startPos.z, endPos.z);

                int currentBlockX = Mth.floor(startX);
                int currentBlockY = Mth.floor(startY);
                int currentBlockZ = Mth.floor(startZ);

                // 检测射线起点所在方块
                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(currentBlockX, currentBlockY, currentBlockZ);
                T first = consumer.apply(context, pos);
                if (first != null) {
                    return first;
                }

                // 初始化 DDA 步进参数
                double dx = endX - startX;
                double dy = endY - startY;
                double dz = endZ - startZ;
                int signX = Mth.sign(dx);
                int signY = Mth.sign(dy);
                int signZ = Mth.sign(dz);
                double tDeltaX = signX == 0 ? Double.MAX_VALUE : (double) signX / dx;
                double tDeltaY = signY == 0 ? Double.MAX_VALUE : (double) signY / dy;
                double tDeltaZ = signZ == 0 ? Double.MAX_VALUE : (double) signZ / dz;
                double tX = tDeltaX * (signX > 0 ? 1.0 - Mth.frac(startX) : Mth.frac(startX));
                double tY = tDeltaY * (signY > 0 ? 1.0 - Mth.frac(startY) : Mth.frac(startY));
                double tZ = tDeltaZ * (signZ > 0 ? 1.0 - Mth.frac(startZ) : Mth.frac(startZ));

                // 沿 DDA 路径逐方块推进
                while (tX <= 1.0 || tY <= 1.0 || tZ <= 1.0) {
                    if (tX < tY) {
                        if (tX < tZ) {
                            currentBlockX += signX;
                            tX += tDeltaX;
                        } else {
                            currentBlockZ += signZ;
                            tZ += tDeltaZ;
                        }
                    } else if (tY < tZ) {
                        currentBlockY += signY;
                        tY += tDeltaY;
                    } else {
                        currentBlockZ += signZ;
                        tZ += tDeltaZ;
                    }

                    // 检测推进后的方块
                    T result = consumer.apply(context, pos.set(currentBlockX, currentBlockY, currentBlockZ));
                    if (result != null) {
                        return result;
                    }
                }
            }
            // 射线未命中任何目标
            return missFactory.apply(context);
        }
    }

    public static class EntityTrace {

        /**
         * 广义的、解耦的带实体过滤功能射线检测
         * @param contextEntity 当前探测的基准实体（用于获取 Level）
         * @param startPos 射线起点
         * @param endPos 射线终点
         * @param searchBox 实体扫描范围
         * @param targetPredicate 实体过滤器
         * @param hitFactory 命中结果包装工厂 (entity, hitPos) -> T
         */
        public static <T> List<T> rayTraceEntities(
                Entity contextEntity,
                Vec3 startPos,
                Vec3 endPos,
                AABB searchBox,
                Predicate<Entity> targetPredicate,
                BiFunction<Entity, Vec3, T> hitFactory
        ) {
            List<T> hitResults = new ArrayList<>();
            Level level = contextEntity.level();

            // 获取扫描范围内满足过滤条件的所有候选实体
            List<Entity> entities = level.getEntities(contextEntity, searchBox, targetPredicate);

            for (Entity entity : entities) {
                // 计算射线与当前实体包围盒的精确交点
                Optional<Vec3> clipResult = entity.getBoundingBox().clip(startPos, endPos);
                if (clipResult.isPresent()) {
                    // 将命中结果包装为调用方指定类型
                    T result = hitFactory.apply(entity, clipResult.get());
                    // 允许包装工厂返回 null 过滤当前命中结果
                    if (result != null) {
                        hitResults.add(result);
                    }
                }
            }
            return hitResults;
        }

        /**
         * 根据射线路径创建实体扫描范围盒
         * @param entity 基准实体
         * @param startPos 射线起点
         * @param endPos 射线终点
         * @param inflateRadius 外扩半径
         */
        public static AABB createTraceBox(
                Entity entity,
                Vec3 startPos,
                Vec3 endPos,
                double inflateRadius
        ) {
            return entity.getBoundingBox()
                    .expandTowards(endPos.subtract(startPos))
                    .inflate(inflateRadius);
        }

        /**
         * 广义的、解耦的带实体过滤功能射线检测（自动拉伸扫描范围重载版本）
         * @param contextEntity 当前探测的基准实体
         * @param startPos 射线起点
         * @param endPos 射线终点
         * @param inflateRadius 轴向上的额外扩充半径
         * @param targetPredicate 实体过滤器
         * @param hitFactory 命中结果包装工厂 (entity, hitPos) -> T
         */
        public static <T> List<T> rayTraceEntities(
                Entity contextEntity,
                Vec3 startPos,
                Vec3 endPos,
                double inflateRadius,
                Predicate<Entity> targetPredicate,
                BiFunction<Entity, Vec3, T> hitFactory
        ) {
            AABB searchBox = createTraceBox(contextEntity, startPos, endPos, inflateRadius);
            return rayTraceEntities(contextEntity, startPos, endPos, searchBox, targetPredicate, hitFactory);
        }
    }
}