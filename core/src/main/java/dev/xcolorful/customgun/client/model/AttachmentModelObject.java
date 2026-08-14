/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.api.model.IAttachmentModelObjectRender;
import dev.xcolorful.customgun.client.api.model.IModelTextConsumer;
import dev.xcolorful.customgun.client.api.model.bedrock.IBedrockRenderer;
import dev.xcolorful.customgun.client.model.bedrock.BedrockPart;
import dev.xcolorful.customgun.client.renderer.model.TextRender;
import dev.xcolorful.customgun.client.resource.assets.display._ModelNodeTextDisplay;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry.NodeName;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/*
        准心 <------ | 镜身 - 镜片 - 镜片上的环 | <--- 摄像机视角
<------------------------激光
 */
public final class AttachmentModelObject extends AnimatedModelObject implements IAttachmentModelObjectRender, IModelTextConsumer {

    /**
     * 开镜时摄像机视角位置
     */
    private final ArrayList<List<BedrockPart>> scopeViewPaths = new ArrayList<>(3);
    /**
     * 目镜上那个环
     */
    @ApiStatus.Internal
    public @Nullable List<BedrockPart> ocularRingPath;
    /**
     * 镜片
     * @deprecated Go to {@link #divisionOcularEntries} {@link _Division_Ocular_Entry#ocularNodePath}
     */
    @Deprecated(forRemoval = true) private final List<_OcularNodeEntry> ocularNodePaths = null;
    /**
     * 瞄准镜镜身
     */
    @ApiStatus.Internal
    public @Nullable List<BedrockPart> scopeBodyPath;
    /**
     * 准心
     * @deprecated Go to {@link #divisionOcularEntries} {@link _Division_Ocular_Entry#divisionOcularEntries}
     */
    @Deprecated(forRemoval = true) private final List<List<BedrockPart>> divisionNodePaths = null;
    /**
     * 激光
     */
    final ArrayList<List<BedrockPart>> laserBeamPaths = new ArrayList<>(1);

    /**
     * 统一原先的{@link #ocularNodePaths}和{@link #divisionNodePaths}，从而保证使用同一stencil
     * <br>
     * 对于只需要{@link _Division_Ocular_Entry#getDivisionNodePath()}或{@link _Division_Ocular_Entry#getOcularNodePath()}的操作：
     * <ul>
     *     <li>当正序遍历时，获取值为{@code null}时可提前{@code break}</li>
     *     <li>当倒叙遍历时，获取值为{@code null}则需要{@code continue}</li>
     * </ul>
     */
    @ApiStatus.Internal
    public final ArrayList<_Division_Ocular_Entry> divisionOcularEntries = new ArrayList<>(3);

    @Nullable ItemStack currentGunItem;
    @Nullable ItemStack attachmentItem;

    boolean enableScope = false;
    boolean enableSight = false;
    private float scopeViewRadiusModifier = 1;

    AttachmentModelObject(@NotNull BedrockModel pojo) {
        super(pojo);
    }

    public static @Nullable AttachmentModelObject fromPojo(BedrockModel pojo) {
        if (pojo == null) return null;
        AttachmentModelObject instance = new AttachmentModelObject(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        if (!super.resetCache()) return false;

        // scope body 镜身
        this.scopeBodyPath = this.getPath(this.modelMap_get(NodeName.SCOPE_BODY.getName()));

        // ocular ring 目镜上那个环
        this.ocularRingPath = this.getPath(this.modelMap_get(NodeName.OCULAR_RING.getName()));

        List<_OcularNodeEntry> _ocularNodePaths = new ArrayList<>(3);
        List<_DivisionNodeEntry> _divisionNodePaths = new ArrayList<>(3);
        @Nullable String stripped = null;

        // 遍历全部节点
        for (Map.Entry<String, IBedrockRenderer> entry : this.modelMap_entrySet()) {
            String nodeName = entry.getKey();
            IBedrockRenderer renderer = entry.getValue();
            BedrockPart bedrockPart = renderer.getModelRenderer();
            @Nullable String name = bedrockPart.name;

            // scope view 开镜摄像机位置
            if (NodeName.SCOPE_VIEW.matches(nodeName) || NodeName.Prefix.SCOPE_VIEW.matches(nodeName)) {
                this.scopeViewPaths.add(this.getPath(renderer));
                continue;
            }

            // laser beam 激光
            if (NodeName.LASER_BEAM.matches(nodeName)
                    || NodeName.Prefix.LASER_BEAM.matches(nodeName)
            ) {
                this.laserBeamPaths.add(this.getPath(renderer));
                continue;
            }

            stripped = null;

            // ocular 镜片
            if (NodeName.OCULAR_SIGHT.matches(nodeName) // 先匹配长的
                    || NodeName.Prefix.OCULAR_SIGHT.matches(nodeName)
                    || (stripped = NodeName.OCULAR.getStrippedIfMatches(nodeName)) != null
                    || (stripped = NodeName.Prefix.OCULAR.getStrippedIfMatches(nodeName)) != null
            ) {
                if (stripped == null) stripped = nodeName.substring(NodeName.OCULAR.getName().length()); // 只去掉"ocular"，保留"_sight"或"_sight_{}"
                _ocularNodePaths.add(new _OcularNodeEntry(stripped, this.getPath(renderer), false));
                continue;
            } else if (NodeName.OCULAR_SCOPE.matches(nodeName)
                    || NodeName.Prefix.OCULAR_SCOPE.matches(nodeName)
            ) {
                stripped = nodeName.substring(NodeName.OCULAR.getName().length()); // 只去掉"ocular"，保留"_scope"或"_scope_{}"
                _ocularNodePaths.add(new _OcularNodeEntry(stripped, this.getPath(renderer), true));
                continue;
            }

            // division 准心
            if ((stripped = NodeName.DIVISION.getStrippedIfMatches(nodeName)) != null
                    || (stripped = NodeName.Prefix.DIVISION.getStrippedIfMatches(nodeName)) != null
            ) {
                renderer.setVisible(false);
                _divisionNodePaths.add(new _DivisionNodeEntry(stripped, this.getPath(renderer)));
                continue;
            }
        }

        { // 排序
            this.scopeViewPaths.sort(Comparator.comparing(path -> {
                if (path.isEmpty()) return "";
                BedrockPart lastPart = path.get(path.size() - 1);
                return lastPart.name != null ? lastPart.name : "";
            }));

            this.divisionOcularEntries.addAll(_AttachmentSort.getOcularDivisionSorted(_ocularNodePaths, _divisionNodePaths));
        }

        return true;
    }

    // --------Getter--------

    public @Nullable List<BedrockPart> getScopeViewPath(int scopeViewIndex) {
        if (this.scopeViewPaths.isEmpty()) return null;
        else if (this.scopeViewPaths.size() <= scopeViewIndex) return null;
        else return this.scopeViewPaths.get(scopeViewIndex);
    }
    public boolean getEnableScope() {
        return this.enableScope;
    }
    public boolean getEnableSight() {
        return this.enableSight;
    }
    public float getScopeViewRadiusModifier() {
        return this.scopeViewRadiusModifier;
    }

    // --------Setter--------

    public void setEnableScope(boolean enable) {
        this.enableScope = enable;
    }
    public void setEnableSight(boolean enable) {
        this.enableSight = enable;
    }
    public void setScopeViewRadiusModifier(float scopeViewRadiusModifier) {
        this.scopeViewRadiusModifier = scopeViewRadiusModifier;
    }

    // --------IAttachmentModelObjectRender--------

    @Override
    public void render(PoseStack matrixStack,
                       ItemDisplayContext transformType,
                       RenderType renderType,
                       int light, int overlay,
                       @Nullable ItemStack gunItem, @Nullable ItemStack attachmentItem) {
        _AttachmentModelRender.render(this,
                matrixStack,
                transformType,
                renderType,
                light, overlay,
                gunItem, attachmentItem);
    }

    // --------IModelObjectRender--------

    void super_render(PoseStack matrixStack,
                      ItemDisplayContext transformType,
                      RenderType renderType,
                      int light, int overlay) {
        super.render(matrixStack,
                transformType,
                renderType,
                light, overlay);
    }

    // --------IModelTextConsumer--------

    @Override
    public void setTextShowList(Map<String, _ModelNodeTextDisplay> modelNodeTextDisplay) {
        modelNodeTextDisplay.forEach((name, _pojo) -> {
            this.setFunctionalRenderer(name, bedrockPart -> new TextRender(this, _pojo, this.currentGunItem));
        });
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true) private static final String SCOPE_VIEW_NODE = NodeName.SCOPE_VIEW.getName();
    @Deprecated(forRemoval = true) private static final String SCOPE_BODY_NODE = NodeName.SCOPE_BODY.getName();
    @Deprecated(forRemoval = true) private static final String OCULAR_RING_NODE = NodeName.OCULAR_RING.getName();
    @Deprecated(forRemoval = true) private static final String DIVISION_NODE = NodeName.DIVISION.getName();
    @Deprecated(forRemoval = true) private static final String OCULAR_NODE = NodeName.OCULAR.getName();
    @Deprecated(forRemoval = true) private static final String OCULAR_SIGHT_NODE = NodeName.OCULAR_SIGHT.getName();
    @Deprecated(forRemoval = true) private static final String OCULAR_SCOPE_NODE = NodeName.OCULAR_SCOPE.getName();

    @Deprecated(forRemoval = true) public void setIsScope(boolean isScope) {
        this.setEnableScope(isScope);
    }
    @Deprecated(forRemoval = true) public void setIsSight(boolean isSight) {
        this.setEnableSight(isSight);
    }
    @Deprecated(forRemoval = true) public boolean isScope() {
        return this.getEnableScope();
    }
    @Deprecated(forRemoval = true) public boolean isSight() {
        return this.getEnableSight();
    }

    // --------闲人勿入（内部实现类型）--------

    protected record _OcularNodeEntry(String name, List<BedrockPart> path, boolean enableScope) {}
    protected record _DivisionNodeEntry(String name, List<BedrockPart> path) {}

    @ApiStatus.Internal
    public static class _Division_Ocular_Entry {
        /**
         * 根据{@link _AttachmentSort#IGNORE_NAME_MISMATCH}的值而产生不同的约束力
         */
        @Deprecated(forRemoval = false)
        private final @NotNull String name;

        private final @Nullable List<BedrockPart> ocularNodePath;
        private final boolean enableScope;
        private final @Nullable List<BedrockPart> divisionNodePath;

        protected _Division_Ocular_Entry(@NotNull String name, @NotNull _OcularNodeEntry ocularNodeEntry) {
            this(name, ocularNodeEntry, null);
        }
        protected _Division_Ocular_Entry(@NotNull String name, @NotNull List<BedrockPart> divisionNodePath) {
            this(name, null, divisionNodePath);
        }
        protected _Division_Ocular_Entry(@NotNull String name, @Nullable _OcularNodeEntry ocularNodeEntry, @Nullable List<BedrockPart> divisionNodePath) {
            this.name = name;
            if (ocularNodeEntry != null) {
                this.ocularNodePath = ocularNodeEntry.path;
                this.enableScope = ocularNodeEntry.enableScope;
            } else {
                this.ocularNodePath = null;
                this.enableScope = false;
            }
            this.divisionNodePath = divisionNodePath;
        }

        public @NotNull String getName() {
            return this.name;
        }
        public @Nullable List<BedrockPart> getOcularNodePath() {
            return this.ocularNodePath;
        }
        public boolean getEnableScope() {
            return this.enableScope;
        }
        public @Nullable List<BedrockPart> getDivisionNodePath() {
            return this.divisionNodePath;
        }

        // --------Deprecated--------

        @Deprecated(forRemoval = true) public boolean isScopeOcular() {
            return this.getEnableScope();
        }
    }
}
