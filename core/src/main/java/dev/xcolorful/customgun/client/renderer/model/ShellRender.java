/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.compat.oculus.OculusCompat;
import dev.xcolorful.customgun.client.model.AmmoModelObject;
import dev.xcolorful.customgun.client.model.GunModelObject;
import dev.xcolorful.customgun.client.resource.assets.display.AmmoDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.ammo._ShellDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.gun._ShellEjectionParam;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAmmoIndexInstance;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.concurrent.ConcurrentLinkedDeque;

public class ShellRender implements IModelComponentRenderer {
    public static class State {
        public static boolean isSelf = false;
    }

    private final GunModelObject gunModelObject;
    /**
     * 抛壳队列
     */
    private final ConcurrentLinkedDeque<_Data> shellQueue = new ConcurrentLinkedDeque<>();

    public ShellRender(GunModelObject gunModelObject) {
        this.gunModelObject = gunModelObject;
    }

    public void addShell(Vector3f randomVelocity) {
        if (this.shellQueue.size() > 128) {
            this.shellQueue.pollFirst();
        }
        double xRandom = Math.random() * randomVelocity.x();
        double yRandom = Math.random() * randomVelocity.y();
        double zRandom = Math.random() * randomVelocity.z();
        Vector3f vector3f = new Vector3f((float) xRandom, (float) yRandom, (float) zRandom);
        this.shellQueue.offerLast(new _Data(System.currentTimeMillis(), vector3f));
    }

    /**
     * 清理过期的抛壳
     */
    public void cleanShellQueue(long lifetimeMs) {
        long currentTimeMillis = System.currentTimeMillis();

        _Data shellData;
        while ((shellData = this.shellQueue.peekFirst()) != null
                && (currentTimeMillis - shellData.timestamp > lifetimeMs)) {
            this.shellQueue.pollFirst();
        }
    }

    // --------IModelComponentRenderer--------

    @Override
    public void render(PoseStack poseStack,
                       VertexConsumer vertexBuffer,
                       ItemDisplayContext transformType,
                       int light, int overlay) {
        if (!State.isSelf) return;

        if (OculusCompat.isRenderShadow()) return;

        ItemStack gunItem = this.gunModelObject.getCurrentGunItem();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        @Nullable _ShellEjectionParam shellEjectionParam;
        { // 获取抛壳数据
            @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
            if (gunDisplayInstance == null) return;

            GunDisplay gunDisplay = gunDisplayInstance.getPojo();
            shellEjectionParam = gunDisplay.getShellEjectionParam();
            if (shellEjectionParam == null) {
                this.shellQueue.clear();
                return;
            }
        }

        @Nullable ClientAmmoIndexInstance clientAmmoIndexInstance;
        { // 获取子弹数据
            var gunLocation = iGun.getGunLocation(gunItem);
            @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
            if (gunIndexInstance == null) return;

            GunData gunData = gunIndexInstance.getGunData();
            var ammoLocation = gunData.getAmmoLocation();
            clientAmmoIndexInstance = ClientResourceApi.getClientAmmoIndexInstance(ammoLocation);
            if (clientAmmoIndexInstance == null) return;
        }

        @Nullable AmmoModelObject ammoModelObject;
        @Nullable _ShellDisplay shellDisplay;
        { // 获取子弹display
            ammoModelObject = clientAmmoIndexInstance.getAmmoModel();
            if (ammoModelObject == null) return;

            AmmoDisplay ammoDisplay = clientAmmoIndexInstance.getAmmoDisplay();
            shellDisplay = ammoDisplay.getShellDisplay();
            if (shellDisplay == null) return;
        }

        this.renderShell(poseStack, gunModelObject, ammoModelObject, shellDisplay, shellEjectionParam);
    }

    private void renderShell(PoseStack poseStack,
                                    GunModelObject gunModelObject,
                                    AmmoModelObject ammoModelObject,
                                    _ShellDisplay shellDisplay,
                                    _ShellEjectionParam shellEjectionParam) {
        var shellTextureLocation = shellDisplay.getTextureLocation();
        if (shellTextureLocation == null) return;

        // 清理抛壳队列
        if (!this.shellQueue.isEmpty()) {
            long lifetimeMs = (long) shellEjectionParam.getLifetimeSeconds() * 1000;
            this.cleanShellQueue(lifetimeMs);
        }

        float[] baseVelocity = shellEjectionParam.getBaseVelocity();
        float[] acceleration = shellEjectionParam.getAcceleration();
        float[] angularVelocity = shellEjectionParam.getAngularVelocity();

        // 缓存一下 PoseStack
        for (_Data data : this.shellQueue) {
            if (data.normal == null && data.pose == null) {
                data.normal = new Matrix3f(poseStack.last().normal());
                data.pose = new Matrix4f(poseStack.last().pose());
            }
        }
        
        // 渲染抛壳
        this.gunModelObject.delegateRender((_poseStack, _vertexConsumer, _transformType, light, overlay) -> {
            this.shellQueue.forEach(data -> renderSingleShell(_transformType, light, overlay,
                    ammoModelObject, shellTextureLocation, data,
                    baseVelocity, acceleration, angularVelocity));
        });
    }


    private void renderSingleShell(ItemDisplayContext transformType,
                                   int light, int overlay,
                                   AmmoModelObject ammoModelObject,
                                   ResourceLocation shellTextureLocation,
                                   _Data data,
                                   float[] baseVelocity, float[] acceleration, float[] angularVelocity) {
        // 再检查一次
        if (data.normal == null && data.pose == null) return;
        
        // 先初始化到缓存位置和朝向
        PoseStack poseStack = new PoseStack();
        poseStack.last().normal().mul(data.normal);
        poseStack.last().pose().mul(data.pose);

        // 获取存留时间和各种参数
        long remindTime = System.currentTimeMillis() - data.timestamp;
        double time = remindTime / 1000.0;
        Vector3f randomOffset = data.randomOffset;

        // 位移，满足标准的匀变速直线运动
        double x = (baseVelocity[0] + randomOffset.x()) * time + 0.5 * acceleration[0] * time * time;
        double y = (baseVelocity[1] + randomOffset.y()) * time + 0.5 * acceleration[1] * time * time;
        double z = (baseVelocity[2] + randomOffset.z()) * time + 0.5 * acceleration[2] * time * time;
        poseStack.translate(-x, -y, z);

        // 旋转
        double xw = time * angularVelocity[0];
        double yw = time * angularVelocity[1];
        double zw = time * angularVelocity[2];
        poseStack.mulPose(Axis.XN.rotationDegrees((float) xw));
        poseStack.mulPose(Axis.YN.rotationDegrees((float) yw));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) zw));
        poseStack.translate(0, -1.5, 0);

        ammoModelObject.render(poseStack, transformType, RenderType.entityCutout(shellTextureLocation), light, overlay);
    }

    // --------闲人勿入（内部实现类型）--------

    public static class _Data {
        public final long timestamp;
        public final Vector3f randomOffset;

        public Matrix3f normal = null;
        public Matrix4f pose = null;

        public _Data(long timestamp, Vector3f randomOffset) {
            this.timestamp = timestamp;
            this.randomOffset = randomOffset;
        }
    }
}
