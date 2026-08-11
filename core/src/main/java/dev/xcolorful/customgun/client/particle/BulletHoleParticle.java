/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.particle;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.particle.BulletHoleOption;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

/**
 * Author: Forked from MrCrayfish, continued by Timeless devs
 */
public class BulletHoleParticle extends SingleQuadParticle {

    private final BulletHoleOption bulletHoleOption;
    private int uOffset;
    private int vOffset;
    private float textureDensity;

    private final Quaternionf rotationCache;
    private final BlockPos posCache;

    public BulletHoleParticle(ClientLevel level, double x, double y, double z,
                              BulletHoleOption bulletHoleOption) {
        super(level, x, y, z, null);
        this.bulletHoleOption = bulletHoleOption;
        this.rotationCache = this.bulletHoleOption.direction().getRotation();
        this.posCache = this.bulletHoleOption.pos();

        this.setSprite(this.calculateSprite(this.posCache));
        this.lifetime = this.calculateLifetime(level);
        this.hasPhysics = false;
        this.gravity = 0.0f;
        this.quadSize = PlannedRefactor.PARTICLE_SIZE;
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
        Vec3 view = camera.position();
        float particleX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - view.x());
        float particleY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - view.y());
        float particleZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - view.z());
        // Y 值稍微大一点点，防止 z-fight
        particleY += 0.005F;

        // 结合缓存的面朝向与可能的自旋(roll)计算最终旋转矩阵
        Quaternionf quaternionf = new Quaternionf(this.rotationCache);
        if (this.roll != 0.0F) {
            quaternionf.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
        }

        // 0 - 30 tick 内，从 15 亮度到 0 亮度
        int light = Math.max(15 - this.age / 2, 0);
        int lightColor = light << 4 | light << 20; // LightTexture.pack(int blockLight, int skyLight) { return blockLight << 4 | skyLight << 20; }

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

    private TextureAtlasSprite calculateSprite(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        Level world = minecraft.level;
        if (world != null) {
            BlockState state = world.getBlockState(pos);
//            return minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(state, world, pos);
        }
        CustomGun.LOGGER.warn("BulletHoleParticle: In calculateSprite {}, minecraft.level is null", pos);
//        return minecraft.getModelManager().getMissingBlockStateModel().particleIcon(world, BlockPos.ZERO, AIR.defaultBlockState());
        // TODO 貌似没找到 TextureAtlasSprite 获取方式
        return null;
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
        return Layer.TRANSLUCENT_TERRAIN;
    }


    // --------Client mod particles--------

    public static final ParticleProvider<BulletHoleOption> PROVIDER = (option, world,
                                                                       x, y, z,
                                                                       pXSpeed, pYSpeed, pZSpeed, randomSource) -> new BulletHoleParticle(world, x, y, z, option);
}
