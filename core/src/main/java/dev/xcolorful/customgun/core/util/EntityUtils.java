package dev.xcolorful.customgun.core.util;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.IEntityHitboxHistory;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.hitbox.IEntityHitboxHistoryGetter;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.minecraft.entity.IEntityExtension;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityUtils {

    /**
     * 这个字段在1.20.1算三维距离，在高版本仅攀爬时算垂直距离
     */
    public static float getMoveDist(@NotNull Entity entity) {
        return entity.moveDist;
    }
    @Deprecated(forRemoval = true, since = "1.21.4")
    public static float getWalkDist(@NotNull Entity entity) {
        return getMoveDist(entity);
    }
    @Deprecated(forRemoval = true, since = "1.21.4")
    public static float getOldWalkDist(@NotNull Entity entity) {
        return getMoveDist(entity);
    }

    @ApiStatus.Internal public static float getHorizontalMoveDist(@NotNull Entity entity) {
        return ((IEntityExtension) entity).cgc$getHorizontalMoveDist();
    }
    @ApiStatus.Internal public static float getHorizontalMoveDistOld(@NotNull Entity entity) {
        return ((IEntityExtension) entity).cgc$getHorizontalMoveDistOld();
    }

    public static int getInvulnerableTime(Entity entity) {
        // [1.20.1, 26.3)
        return entity.invulnerableTime;

        // [26.3, )
//        /**
//         * 26.3 起拆分原 invulnerableTime
//         * <ul>
//         *     <li>临时无敌 {@link Entity#invulnerableTime}，参与 {@link Entity#isInvulnerable} 判定</li>
//         *     <li>受击冷却 {@link LivingEntity#damageCooldownTime}，参与 {@link Entity#hurtServer} 判定</li>
//         * </ul>
//         */
//        if (entity instanceof LivingEntity livingEntity) return livingEntity.damageCooldownTime;
//        else return entity.getInvulnerableTime();
    }
    public static void setInvulnerableTime(Entity entity, int ticks) {
        // [1.20.1, 26.3)
        entity.invulnerableTime = ticks;

        // [26.3, )
//        if (entity instanceof LivingEntity livingEntity) livingEntity.damageCooldownTime = ticks;
//        else entity.setInvulnerableTime(ticks);
    }

    public static boolean hurt(Entity entity, DamageSource damageSource, float amount) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return false;

        // [1.20.1, 1.21.4)
//        return entity.hurt(damageSource, amount);

        // [1.21.4, )
        return entity.hurtServer(serverLevel, damageSource, amount);
    }
    public static void knockback(LivingEntity livingEntity, double power, double xd, double zd,
                                 DamageSource damageSource, float damage) {
        livingEntity.knockback(power, xd, zd
                // [26.2, )
//                , damageSource, damage
        );
    }
    public static void swing(LivingEntity livingEntity, InteractionHand hand, boolean sendToSwingingEntity) {
        livingEntity.swing(hand,
//                SwingAnimation.DEFAULT, // [26.3, )
                sendToSwingingEntity);
    }

    public static void setThrower(ItemEntity itemEntity, Entity thrower) {
        // [1.20.1, 1.20.4)
//        itemEntity.setThrower(thrower.getUUID());

        // [1.20.4, )
        itemEntity.setThrower(thrower);
    }

    public static ItemStack getItemHeldByArm(@NotNull LivingEntity livingEntity, HumanoidArm arm) {
        // [1.20.1, 1.21.4)
//        return livingEntity.getMainArm() == arm ? livingEntity.getMainHandItem() : livingEntity.getOffhandItem();

        // [1.21.4, )
        return livingEntity.getItemHeldByArm(arm);
    }

    public static boolean is(@NotNull Entity entity, TagKey<EntityType<?>> tagKey) {
        // [1.20.1, 26.1.x)
//        return entity.getType().is(tagKey);

        // [26.1.x, )
        return entity.is(tagKey);
    }

    public static class Hitbox {

        /**
         * @param entity 获取碰撞箱的实体
         * @param livingShooter (可选) 射手生物
         * @return 延迟补偿后的碰撞箱
         */
        public static @NotNull AABB getTracedHitbox(@NotNull Entity entity, @Nullable Entity livingShooter) {
            @Nullable ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromEntity(livingShooter);
            if (iLivingShooter == null) return entity.getBoundingBox();

            IEntityHitboxHistory entityHitboxHistory = IEntityHitboxHistoryGetter.cgc$fromEntity(entity);
            if (entityHitboxHistory != null) {
                int tickBefore = Mth.floor(iLivingShooter.cgc$getShooterLatencyMs() / 1000.0 * 20 + 0.5);
                @Nullable AABB historyBox = entityHitboxHistory.cgc$getHistoryHitbox(tickBefore);
                if (historyBox != null) {
                    return historyBox;
                }
            }

            return entity.getBoundingBox();
        }
        public static @NotNull AABB getTracedHitbox(@NotNull Entity entity) {
            return getTracedHitbox(entity, null);
        }
    }

    /**
     * 适用场景：
     * <ul>
     *     <li>[1.20.1, 1.21.1) {@code IForgeItem#getAttributeModifiers(EquipmentSlot, ItemStack)}</li>
     *     <li>[1.21.1, ) {@code IItemExtension#getDefaultAttributeModifiers(ItemStack)}</li>
     *     <li>[1.21.4, ) 原版移除了 {@code generic.} 前缀，兼容旧数据读取</li>
     * </ul>
     * 注意：
     * <ul>
     *     <li>非线程安全，模组加载期（Forge 并行加载机制）调用可能会 {@link ConcurrentModificationException}</li>
     * </ul>
     */
    public static class _Attribute {

        /**
         * 使用 {@code ResourceLocation#toString} 生成 {@link UUID}
         * <li>攻击伤害/攻速直接取原版物品默认属性修饰符里的实例，因为原版 tooltip 以 {@code ==} 判断这两项</li>
         */
//        @Deprecated(since = "1.21.1")
        private static final Map<ResourceLocation, UUID> attributeUuidCache = new HashMap<>();
        @ApiStatus.AvailableSince("1.21.1")
        private static final Map<ResourceLocation, ResourceLocation> attributeLocationCache = new HashMap<>();

        /**
         * @param actualUuid 实际使用的 {@link UUID}
         * @param attributeLocations 兼容的输入
         */
//        @Deprecated(since = "1.21.1")
        @ApiStatus.Internal
        public static void addAttributeUuid(UUID actualUuid, ResourceLocation... attributeLocations) {
            for (ResourceLocation attributeLocation : attributeLocations) {
                attributeUuidCache.putIfAbsent(attributeLocation, actualUuid);
            }
        }
        /**
         * @param attributeLocations 兼容的输入
         * @param actualAttributeLocation 实际使用的 {@code ResourceLocation}
         */
        @ApiStatus.AvailableSince("1.21.1")
        @ApiStatus.Internal
        public static void addAttributeLocation(ResourceLocation actualAttributeLocation,
                                                ResourceLocation... attributeLocations) {
            for (ResourceLocation attributeLocation : attributeLocations) {
                attributeLocationCache.putIfAbsent(attributeLocation, actualAttributeLocation);
            }
        }

        private static final ResourceLocation[] ATTACK_DAMAGES;
        private static final ResourceLocation[] ATTACK_SPEEDS;
        static {
            IMcRegistry mcRegistry = CustomGun.getMcRegistry();
            ATTACK_DAMAGES = new ResourceLocation[]{
                    mcRegistry.createResourceLocation("minecraft:generic.attack_damage"),
                    mcRegistry.createResourceLocation("minecraft:attack_damage")
            };
            ATTACK_SPEEDS = new ResourceLocation[]{
                    mcRegistry.createResourceLocation("minecraft:generic.attack_speed"),
                    mcRegistry.createResourceLocation("minecraft:attack_speed")
            };
        }

        static {
            // [1.20.1, 1.21.1)
            @SuppressWarnings("deprecation")
            Multimap<Attribute, AttributeModifier> vanillaModifiers = Items.IRON_SWORD.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
            addAttributeUuid(vanillaModifiers.get(Attributes.ATTACK_DAMAGE).iterator().next().getId(), ATTACK_DAMAGES);
            addAttributeUuid(vanillaModifiers.get(Attributes.ATTACK_SPEED).iterator().next().getId(), ATTACK_SPEEDS);

            // [1.21.1, )
//            addAttributeLocation(Item.BASE_ATTACK_DAMAGE_ID, ATTACK_DAMAGES);
//            addAttributeLocation(Item.BASE_ATTACK_SPEED_ID, ATTACK_SPEEDS);
        }

        /**
         * <ul>
         *     <li>[1.20.1, 1.21.1) 返回 {@code ImmutableMultimap.Builder<Attribute, AttributeModifier>}</li>
         *     <li>[1.21.1, ) 返回 {@code ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier>}</li>
         * </ul>
         * 使用方式：
         * <ol>
         *     <li>本方法返回值用 {@code var builder} 来跨版本编译期兼容 (性质同 {@code var resourceLocation})</li>
         *     <li>调用 {@link #resolveAttributeLocation}，返回值用 {@code @Nullable var attribute} 来检查键是否有对应的 {@link Attribute}，并跨版本编译器兼容</li>
         *     <li>调用 {@link #createAttributeModifier} 创建值 {@link AttributeModifier}</li>
         *     <li>{@code builder.put(attribute, attributeModifier)}</li>
         *     <li>重复 2-4 后，{@code builder.build()} 收尾</li>
         * </ol>
         */
        public static ImmutableMultimap.Builder<Attribute, AttributeModifier> createAttributeModifierBuilder() {
            return ImmutableMultimap.builder();
        }

        /**
         * <ul>
         *     <li>[1.20.1, 1.21.1) 返回 {@link Attribute}</li>
         *     <li>[1.21.1, ) 返回 {@link Holder<Attribute>}</li>
         *     <li>[1.21.4, ) 原版 {@link Attribute} 移除了 {@code generic.} 前缀，自动尝试新旧格式读取</li>
         * </ul>
         * @return 解析结果；返回 {@code null} 则表示解析失败
         */
        @SuppressWarnings("deprecation")
        public static @Nullable Attribute resolveAttributeLocation(ResourceLocation attributeLocation) {
            if (BuiltInRegistries.ATTRIBUTE.containsKey(attributeLocation)) {
                return BuiltInRegistries.ATTRIBUTE.get(attributeLocation);
            }

            if ("minecraft".equals(attributeLocation.getNamespace())) {
                if (attributeLocation.getPath().startsWith("generic.")) {
                    attributeLocation = CustomGun.getMcRegistry().createResourceLocation("minecraft:" + attributeLocation.getPath().substring("generic.".length()));
                } else {
                    attributeLocation = CustomGun.getMcRegistry().createResourceLocation("minecraft:generic." + attributeLocation.getPath());
                }
            }

            return BuiltInRegistries.ATTRIBUTE.get(attributeLocation);
        }

        public static AttributeModifier createAttributeModifier(ResourceLocation attributeLocation,
                                                                double amount,
                                                                AttributeModifier.Operation operation) {
            // [1.20.1, 1.21.1)
            return new AttributeModifier(
                    attributeUuidCache.computeIfAbsent(attributeLocation,
                            location -> UUID.nameUUIDFromBytes(location.toString().getBytes(StandardCharsets.UTF_8))),
                    attributeLocation.toString(),
                    amount,
                    operation
            );

            // [1.21.1, )
//            return new AttributeModifier(
//                    attributeLocationCache.computeIfAbsent(attributeLocation, location -> location),
//                    amount,
//                    operation
//            );
        }
    }

    /**
     * 不作为通用API，仅适配特定平台
     */
    public static class Forge {

        public static void reviveCaps(@NotNull LivingEntity livingEntity) {
//            try {
//            livingEntity.reviveCaps();
//            } catch (NoSuchMethodError ignored) {} // neoforge1.20.4+没这方法，省一个forge侧mixin
        }
        public static void invalidateCaps(@NotNull LivingEntity livingEntity) {
//            try {
//            livingEntity.invalidateCaps();
//            } catch (NoSuchMethodError ignored) {} // neoforge1.20.4+没这方法，省一个forge侧mixin
        }
    }
}
