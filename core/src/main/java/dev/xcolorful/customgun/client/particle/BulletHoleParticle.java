/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.particle;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAmmoIndexInstance;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.api.block.IBulletVictimBlock;
import dev.xcolorful.customgun.core.api.block.victim.IBulletVictimBlockGetter;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.particle.BulletHoleOption;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.awt.*;

/**
 * Author: Forked from MrCrayfish, continued by Timeless devs, continued continued by XiaoColorful
 */
public class BulletHoleParticle extends SingleQuadParticle {

    private final BulletHoleOption bulletHoleOption;
    private int uOffset;
    private int vOffset;
    private float textureDensity;

    private final Quaternionf rotationCache;
    private final BlockPos posCache;

    // ----------1.21.10----------
    /**
     * <ul>
     *     [1.21.10, )
     *     <li>{@code net.minecraft.client.renderer.state.QuadParticleRenderState} 的方片位于 XY 平面（法线为 +Z）</li>
     *     <li>而 {@link net.minecraft.core.Direction#getRotation()} 假设方片位于 XZ 平面（法线为 +Y）</li>
     *     <li>先把方片转到 getRotation 所假设的基上，法线才会与命中面朝向一致</li>
     * </ul>
     */
    @ApiStatus.AvailableSince("1.21.10")
    private static final Quaternionf QUAD_NORMAL_FIX = new Quaternionf().rotationX(-Mth.HALF_PI);
    /**
     * 沿命中面法线外推的距离，避免弹孔与方块面重合导致 z-fight
     */
    private static final float SURFACE_OFFSET = 0.005f;
    private final Vec3 surfaceOffset;

    public BulletHoleParticle(ClientLevel level, double x, double y, double z,
                              BulletHoleOption bulletHoleOption) {
        super(level, x, y, z, null);
        this.bulletHoleOption = bulletHoleOption;
        this.rotationCache = new Quaternionf(
                this.bulletHoleOption.direction()
                        .getRotation()
                        .mul(QUAD_NORMAL_FIX) // [1.21.10, )
        );
        this.posCache = this.bulletHoleOption.pos();
        this.surfaceOffset = this.bulletHoleOption.direction().getUnitVec3().scale(SURFACE_OFFSET); // [1.21.10, )

        this.setSprite(this.calculateSprite(this.posCache));
        this.lifetime = this.calculateLifetime(level);
        this.hasPhysics = false;
        this.gravity = 0.0f;
        this.quadSize = PlannedRefactor.PARTICLE_SIZE;

        if (this.shouldRemove()) this.remove();

        // 曳光弹颜色
        @Nullable Color tracerColor = this.calculateTracerColor();
        if (tracerColor != null) {
            this.rCol = tracerColor.getRed() / 255f;
            this.gCol = tracerColor.getGreen() / 255f;
            this.bCol = tracerColor.getBlue() / 255f;
        }
        this.alpha = 0.9f;
    }
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.shouldRemove()) this.remove();
    }
    private boolean shouldRemove() {
        final BlockState blockState = this.level.getBlockState(this.posCache);
        if (blockState.isAir()) return true;

        @Nullable IBulletVictimBlock iBulletVictimBlock = IBulletVictimBlockGetter.fromBlock(blockState.getBlock());
        if (iBulletVictimBlock != null && !iBulletVictimBlock.cgc$hasProjectileHitVisual()) {
            return true;
        }

        // 阻止弹孔在与方块不构成有效附着时继续渲染
        VoxelShape shape = blockState.getCollisionShape(this.level, this.posCache);
        if (shape.isEmpty()) {
            return true;
        }
        AABB baseBlockBoundingBox = shape.bounds();
        AABB blockBoundingBox = baseBlockBoundingBox.move(this.posCache);
        boolean intersects = blockBoundingBox.intersects(
                this.x - 0.1, this.y - 0.1, this.z - 0.1,
                this.x + 0.1, this.y + 0.1, this.z + 0.1);
        return !intersects;
    }

    @Override
    public void extract(QuadParticleRenderState particleTypeRenderState, Camera camera, float partialTicks) {
        Vec3 view = camera.getPosition();
        float particleX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - view.x() + this.surfaceOffset.x);
        float particleY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - view.y() + this.surfaceOffset.y);
        float particleZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - view.z() + this.surfaceOffset.z);

        // 结合缓存的面朝向与可能的自旋(roll)计算最终旋转矩阵
        Quaternionf quaternionf = new Quaternionf(this.rotationCache);
        if (this.roll != 0.0F) {
            quaternionf.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
        }

        // 0 - 30 tick 内，从 15 亮度到 0 亮度
        int light = Math.max(15 - this.age / 2, 0);
        int lightColor = ClientRenderUtils.LightTexture_.pack(light, light);

        // 颜色，逐渐渐变到 0 0 0，也就是黑色
        float colorPercent = light / 15.0f;
        float red = this.rCol * colorPercent;
        float green = this.gCol * colorPercent;
        float blue = this.bCol * colorPercent;

        // 透明度，逐渐变成 0，也就是透明
        double threshold = RenderConfig.BULLET_HOLE_PARTICLE_FADE_THRESHOLD.get() * this.lifetime;
        float fade = 1.0f - (float) (Math.max(this.age - threshold, 0) / (this.lifetime - threshold));
        float alphaFade = this.alpha * fade;

        particleTypeRenderState.add(
                this.getLayer(),
                particleX,
                particleY,
                particleZ,
                quaternionf.x,
                quaternionf.y,
                quaternionf.z,
                quaternionf.w,
                this.getQuadSize(partialTicks),
                this.getU0(),
                this.getU1(),
                this.getV0(),
                this.getV1(),
                ARGB.colorFromFloat(alphaFade, red, green, blue),
                lightColor
        );
    }

    // --------便利方法--------

    /**
     * @since 26.1.x {@link Level}不再是{@link BlockAndTintGetter}，只有{@link ClientLevel}实现了该接口
     */
    private TextureAtlasSprite calculateSprite(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        return Minecraft.getInstance()

                .getBlockRenderer() // [1.20.1, 26.1.x)
//                .getModelManager() // [26.1.x, )

                .getBlockModelShaper() // [1.20.1, 26.1.x)
//                .getBlockStateModelSet() // [26.1.x, )

//                .getTexture(state, this.level, pos) // [1.20.1, 1.21.6)
                .getParticleIcon(state, this.level, pos) // [1.21.6, 26.1.x)
//                .getParticleMaterial(state, this.level, pos).sprite() // [26.1.x, )
                ;
    }

    @Override
    protected void setSprite(TextureAtlasSprite sprite) {
        super.setSprite(sprite);
        this.uOffset = this.random.nextInt(16);
        this.vOffset = this.random.nextInt(16);
        // 材质应该都是方形
        this.textureDensity = (sprite.getU1() - sprite.getU0()) / 16.0F;
    }

    private int calculateLifetime(ClientLevel level) {
        int lifeTicks = RenderConfig.BULLET_HOLE_PARTICLE_LIFE.get();
        if (lifeTicks <= 1) return lifeTicks;
        else return lifeTicks + level.getRandom().nextInt(lifeTicks / 2);
    }

    private @Nullable Color calculateTracerColor() {
        IMcRegistry mcRegistry = CustomGun.getMcRegistry();
        @Nullable var gunDisplayLocation = mcRegistry.createResourceLocation(this.bulletHoleOption.gunDisplayLocation());
        @Nullable var gunLocation = mcRegistry.createResourceLocation(this.bulletHoleOption.gunLocation());

        @Nullable Color color = null; {
            @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunDisplayLocation, gunLocation);
            // 优先枪械 display 的曳光弹颜色
            if (gunDisplayInstance != null) {
                color = gunDisplayInstance.getTracerColor();
            }

            // 其次用子弹的曳光弹颜色
            if (color == null) {
                @Nullable var ammoLocation = mcRegistry.createResourceLocation(this.bulletHoleOption.ammoLocation());
                @Nullable ClientAmmoIndexInstance ammoIndexInstance = ClientResourceApi.getClientAmmoIndexInstance(ammoLocation);
                if (ammoIndexInstance != null) {
                    color = ammoIndexInstance.getAmmoDisplay().getTracerColor();
                }
            }
        }

        return color;
    }

    // --------Getter--------

    @Override protected float getU0() {
        return this.sprite.getU0() + this.uOffset * this.textureDensity;
    }
    @Override protected float getV0() {
        return this.sprite.getV0() + this.vOffset * this.textureDensity;
    }
    @Override protected float getU1() {
        return this.getU0() + this.textureDensity;
    }
    @Override protected float getV1() {
        return this.getV0() + this.textureDensity;
    }

    @ApiStatus.AvailableSince("1.21.10")
    @Override protected @NotNull Layer getLayer() {
        return Layer.TERRAIN;
    }


    // --------Client mod particles--------

    public static final ParticleProvider<BulletHoleOption> PROVIDER = (option, world,
                                                                       x, y, z,
                                                                       pXSpeed, pYSpeed, pZSpeed, randomSource) -> new BulletHoleParticle(world, x, y, z, option);
}
