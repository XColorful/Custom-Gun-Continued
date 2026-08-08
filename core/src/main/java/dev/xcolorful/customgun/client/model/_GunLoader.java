/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model;

import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockRenderer;
import dev.xcolorful.customgun.client.api.renderer.model.IModelComponentRenderer;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.renderer.model.*;
import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import dev.xcolorful.customgun.core.api.item.attachment.MagazineCategory;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry.NodeName;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class _GunLoader {

    protected static void constructMagazineNode(GunModelObject _this) {
        @Nullable IBedrockRenderer magazine = _this.modelMap_get(NodeName.MAG_NORMAL.getName());
        if (magazine != null) _this.magazineNode = magazine.getModelRenderer();

        @Nullable IBedrockRenderer additionalMagazine = _this.modelMap_get(NodeName.MAG_ADDITIONAL.getName());
        if (additionalMagazine != null) _this.additionalMagazineNode = additionalMagazine.getModelRenderer();
    }

    /**
     * TODO 渲染时每帧都这么拼命地读NBT？1.21.1+读操作还有额外的copy开销怎么办？
     */
    protected static void constructFunctionalRenderer(GunModelObject _this) {
        // 左手手臂LeftHandRender
        _this.setFunctionalRenderer(NodeName.LEFTHAND_POS.getName(), bedrockPart -> new HandRender.Left(_this));
        // 右手手臂
        _this.setFunctionalRenderer(NodeName.RIGHTHAND_POS.getName(), bedrockPart -> new HandRender.Right(_this));
        // 枪口火焰
        _this.setFunctionalRenderer(NodeName.MUZZLE_FLASH_ORIGIN.getName(), bedrockPart -> new MuzzleFlashRender(_this));
        // 枪管内的子弹，用于闭膛待机枪械
        _this.setFunctionalRenderer(NodeName.BULLET_IN_BARREL.getName(), bedrockPart -> {
            @Nullable IGun iGun = IGunGetter.fromItemStack(_this.currentGunItem);
            bedrockPart.visible = iGun != null && iGun.hasBarrelAmmo(_this.currentGunItem); // 不检查open bolt
            return null;
        });
        // 弹匣内子弹
        _this.setFunctionalRenderer(NodeName.BULLET_IN_MAG.getName(), bedrockPart -> {
            @Nullable IGun iGun = IGunGetter.fromItemStack(_this.currentGunItem);
            bedrockPart.visible = iGun != null && iGun.getMagAmmoCount(_this.currentGunItem) > 0;
            return null;
        });
        // 机枪弹链
        _this.setFunctionalRenderer(NodeName.BULLET_CHAIN.getName(), bedrockPart -> {
            @Nullable IGun iGun = IGunGetter.fromItemStack(_this.currentGunItem);
            bedrockPart.visible = iGun != null && iGun.getMagAmmoCount(_this.currentGunItem) > 0;
            return null;
        });
        // 有通用瞄具时显示，用于放瞄具的导轨（如 AKM 的导轨）
        _this.setFunctionalRenderer(NodeName.MOUNT.getName(), bedrockPart -> {
            @Nullable ItemStack scopeItem = _this.currentAttachmentItem.get(AttachmentCategory.SCOPE);
            bedrockPart.visible = scopeItem != null && !scopeItem.isEmpty() && _this.renderMount; // 安装瞄具时可见
            return null;
        });
        // 无瞄具时可见，通常用于 M4 上
        _this.setFunctionalRenderer(NodeName.CARRY.getName(), bedrockPart -> {
            @Nullable ItemStack scopeItem = _this.currentAttachmentItem.get(AttachmentCategory.SCOPE);
            bedrockPart.visible = scopeItem == null || scopeItem.isEmpty();
            return null;
        });
        // 有瞄具时显示，折叠的机械瞄具
        _this.setFunctionalRenderer(NodeName.SIGHT_FOLDED.getName(), bedrockPart -> {
            @Nullable ItemStack scopeItem = _this.currentAttachmentItem.get(AttachmentCategory.SCOPE);
            bedrockPart.visible = scopeItem != null && !scopeItem.isEmpty() && _this.renderMount; // 安装瞄具时可见
            return null;
        });
        // 无瞄具时可见，机械瞄具
        _this.setFunctionalRenderer(NodeName.SIGHT.getName(), bedrockPart -> {
            @Nullable ItemStack scopeItem = _this.currentAttachmentItem.get(AttachmentCategory.SCOPE);
            bedrockPart.visible = scopeItem == null || scopeItem.isEmpty();
            return null;
        });
        // 安装一级扩容弹匣时显示
        _this.setFunctionalRenderer(NodeName.MAG_EXTENDED_1.getName(), bedrockPart -> {
            bedrockPart.visible = _this.currentMagazineCategory == MagazineCategory.EXTENDED_MAG_1;
            return null;
        });
        // 安装二级扩容弹匣时显示
        _this.setFunctionalRenderer(NodeName.MAG_EXTENDED_2.getName(), bedrockPart -> {
            bedrockPart.visible = _this.currentMagazineCategory == MagazineCategory.EXTENDED_MAG_2;
            return null;
        });
        // 安装三级扩容弹匣时显示
        _this.setFunctionalRenderer(NodeName.MAG_EXTENDED_3.getName(), bedrockPart -> {
            bedrockPart.visible = _this.currentMagazineCategory == MagazineCategory.EXTENDED_MAG_3;
            return null;
        });
        // 没有安装扩容弹匣时显示
        _this.setFunctionalRenderer(NodeName.MAG_STANDARD.getName(), bedrockPart -> {
            bedrockPart.visible = _this.currentMagazineCategory == MagazineCategory.NONE;
            return null;
        });
        // 部分枪械换弹动画播放时，会同时出现两个弹匣，这个就是程序自动渲染另一个弹匣的代码
        _this.setFunctionalRenderer(NodeName.MAG_ADDITIONAL.getName(), bedrockPart -> renderAdditionalMagazine(_this, bedrockPart));
        // 默认护木渲染
        _this.setFunctionalRenderer(NodeName.HANDGUARD_DEFAULT.getName(), bedrockPart -> handguardDefaultRender(_this, bedrockPart));
        // 战术护木渲染
        _this.setFunctionalRenderer(NodeName.HANDGUARD_TACTICAL.getName(), bedrockPart -> handguardTacticalRender(_this, bedrockPart));
    }

    private static @NotNull IModelComponentRenderer renderAdditionalMagazine(GunModelObject _this, BedrockPart bedrockPart) {
        return (poseStack,
                vertexBuffer,
                transformType,
                light,
                overlay) -> {
            if (!bedrockPart.visible) return;

            bedrockPart.compile(poseStack.last(), vertexBuffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            for (int i = 0; i < bedrockPart.children.size(); i++) {
                BedrockPart part = bedrockPart.children.get(i);
                part.render(poseStack, transformType, vertexBuffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            }

            if (_this.magazineNode == null || !_this.magazineNode.visible) return;

            _this.magazineNode.compile(poseStack.last(), vertexBuffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            for (int i = 0; i < _this.magazineNode.children.size(); i++) {
                BedrockPart part = _this.magazineNode.children.get(i);
                part.render(poseStack, transformType, vertexBuffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        };
    }

    private static @Nullable IModelComponentRenderer handguardDefaultRender(GunModelObject _this, BedrockPart bedrockPart) {
        ItemStack laserItem = _this.currentAttachmentItem.get(AttachmentCategory.LASER);
        ItemStack gripItem = _this.currentAttachmentItem.get(AttachmentCategory.GRIP);
        bedrockPart.visible = (laserItem == null || laserItem.isEmpty()) && (gripItem == null || gripItem.isEmpty());
        return null;
    }
    private static @Nullable IModelComponentRenderer handguardTacticalRender(GunModelObject _this, BedrockPart bedrockPart) {
        ItemStack laserItem = _this.currentAttachmentItem.get(AttachmentCategory.LASER);
        ItemStack gripItem = _this.currentAttachmentItem.get(AttachmentCategory.GRIP);
        bedrockPart.visible = (laserItem != null && !laserItem.isEmpty()) || (gripItem != null && !gripItem.isEmpty());
        return null;
    }

    protected static void constructOtherPath(GunModelObject _this) {
        _this.ironSightPath = _this.getPath(_this.modelMap_get(NodeName.IRON_VIEW.getName()));
        _this.idleSightPath = _this.getPath(_this.modelMap_get(NodeName.IDLE_VIEW.getName()));
        _this.thirdPersonHandOriginPath = _this.getPath(_this.modelMap_get(NodeName.THIRD_PERSON_HAND_ORIGIN.getName()));
        _this.fixedOriginPath = _this.getPath(_this.modelMap_get(NodeName.FIXED_ORIGIN.getName()));
        _this.groundOriginPath = _this.getPath(_this.modelMap_get(NodeName.GROUND_ORIGIN.getName()));
        _this.muzzleFlashPosPath = _this.getPath(_this.modelMap_get(NodeName.MUZZLE_FLASH_ORIGIN.getName()));
        _this.scopePosPath = _this.getPath(_this.modelMap_get(AttachmentCategory.SCOPE.name().toLowerCase() + NodeName.Suffix.ATTACHMENT_POS.getName()));
        _this.laserBeamPaths = _this.getPath(_this.modelMap_get(NodeName.LASER_BEAM.getName()));

        @Nullable IBedrockRenderer root = _this.modelMap_get(NodeName.ROOT.getName());
        _this.root = root != null ? root.getModelRenderer() : null;
    }

    private static final AttachmentCategory[] ATTACHMENT_CATEGORIES = AttachmentCategory.values();
    private static final String[] ATTACHMENT_CATEGORY_NODE_NAMES;
    static {
        ATTACHMENT_CATEGORY_NODE_NAMES = new String[ATTACHMENT_CATEGORIES.length];
        for (int i = 0; i < ATTACHMENT_CATEGORIES.length; i++) {
            AttachmentCategory type = ATTACHMENT_CATEGORIES[i];
            if (type == AttachmentCategory.NONE) {
                ATTACHMENT_CATEGORY_NODE_NAMES[i] = NodeName.REFIT_VIEW.getName();
            } else {
                ATTACHMENT_CATEGORY_NODE_NAMES[i] = NodeName.Prefix.REFIT_VIEW.getName() + type.getConstantName() + NodeName.Suffix.REFIT_VIEW.getName();
            }
        }
    }
    protected static void constructRefitAttachmentViewPath(GunModelObject _this) {
        for (int i = 0; i < ATTACHMENT_CATEGORIES.length; i++) {
            String nodeName = ATTACHMENT_CATEGORY_NODE_NAMES[i];
            _this.refitAttachmentViewPath.put(ATTACHMENT_CATEGORIES[i], _this.getPath(_this.modelMap_get(nodeName)));
        }
    }

    /**
     * 获取抛壳节点名的顺序：
     * <ol>
     *     <li>"shell"</li>
     *     <li>"shell_1"</li>
     *     <li>"shell_2"</li>
     * </ol>
     */
    protected static void constructShellOriginNodes(GunModelObject _this) {
        @Nullable IBedrockRenderer rendererWrapper = _this.modelMap_get(NodeName.SHELL_ORIGIN.getName());
        int i = 1;
        while (rendererWrapper != null) {
            ShellRender shellRender = new ShellRender(_this);
            _this.setFunctionalRenderer(rendererWrapper.getModelRenderer().name, bedrockPart -> shellRender);
            _this.shellRenders.add(shellRender);

            rendererWrapper = _this.modelMap_get(NodeName.Prefix.SHELL_ORIGIN.getName() + i++);
        }
    }

    private static final String[] POSITION_NODE_NAME;
    private static final String[] DEFAULT_NODE_NAME;
    static {
        POSITION_NODE_NAME = new String[ATTACHMENT_CATEGORIES.length];
        DEFAULT_NODE_NAME = new String[ATTACHMENT_CATEGORIES.length];
        for (int i = 0; i < ATTACHMENT_CATEGORIES.length; i++) {
            AttachmentCategory type = ATTACHMENT_CATEGORIES[i];
            POSITION_NODE_NAME[i] = type.getConstantName() + NodeName.Suffix.ATTACHMENT_POS.getName();
            DEFAULT_NODE_NAME[i] = type.getConstantName() + NodeName.Suffix.DEFAULT_ATTACHMENT.getName();
        }
    }
    protected static void constructAllAttachmentRender(GunModelObject _this) {
        for (int i = 0; i < ATTACHMENT_CATEGORIES.length; i++) {
            AttachmentCategory category = ATTACHMENT_CATEGORIES[i];

            // 瞄具的渲染需要提前
            if (category == AttachmentCategory.NONE || category == AttachmentCategory.SCOPE) {
                continue;
            }

            _this.setFunctionalRenderer(POSITION_NODE_NAME[i], bedrockPart -> {
                bedrockPart.visible = false;
                return new AttachmentRender(_this, category);
            });

            _this.setFunctionalRenderer(DEFAULT_NODE_NAME[i], bedrockPart -> {
                ItemStack attachmentItem = _this.currentAttachmentItem.get(category);
                if (category == AttachmentCategory.MUZZLE && checkShowMuzzle(bedrockPart, attachmentItem)) {
                    return null;
                }
                bedrockPart.visible = attachmentItem == null || attachmentItem.isEmpty();
                return null;
            });
        }
    }
    private static boolean checkShowMuzzle(BedrockPart bedrockPart, ItemStack attachmentItem) {
        @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
        if (iAttachment == null) return false;

        var attachmentLocation = iAttachment.getAttachmentLocation(attachmentItem);
        @Nullable ClientAttachmentIndexInstance clientAttachmentIndexInstance = ClientResourceApi.getClientAttachmentIndexInstance(attachmentLocation);
        if (clientAttachmentIndexInstance == null) return false;

        AttachmentDisplay attachmentDisplay = clientAttachmentIndexInstance.getAttachmentDisplay();
        bedrockPart.visible = attachmentDisplay.getShowMuzzle();
        return true;
    }

    protected static void constructAttachmentAdapterNodeRender(GunModelObject _this) {
        _this.setFunctionalRenderer(NodeName.ATTACHMENT_ADAPTER.getName(), bedrockPart -> {
            for (int i = 0; i < bedrockPart.children.size(); i++) {
                BedrockPart child = bedrockPart.children.get(i);
                if (child.name == null) {
                    child.visible = false;
                    continue;
                }
                child.visible = _this.adapterToRender.contains(child.name);
            }
            return null;
        });
    }
}
