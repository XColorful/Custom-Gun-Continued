/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.resource.assets.model.bedrock.geometry;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum NodeName implements ResourceTag.ConstantTag, INodeNameMatcher {
    CAMERA(_BoneTag.Node.CAMERA),
    CONSTRAINT(_BoneTag.Node.CONSTRAINT),
    IDLE_VIEW(_BoneTag.Node.IDLE_VIEW),
    // ----Gun model----
    /**
     * 枪管中的子弹，用于闭膛待击枪械的渲染，枪管中没有子弹时隐藏该组
     */
    BULLET_IN_BARREL(_BoneTag.Node.BULLET_IN_BARREL),
    /**
     * 弹匣内的子弹，会在弹匣打空时隐藏该组
     */
    BULLET_IN_MAG(_BoneTag.Node.BULLET_IN_MAG),
    /**
     * 弹链，多用于机枪，在子弹打空时隐藏
     */
    BULLET_CHAIN(_BoneTag.Node.BULLET_CHAIN),
    /**
     * 无瞄具时可见，通常用于 M4 上
     */
    CARRY(_BoneTag.Node.CARRY),
    /**
     * 安装一级扩容弹匣时显示
     */
    MAG_EXTENDED_1(_BoneTag.Node.MAG_EXTENDED_1),
    /**
     * 安装二级扩容弹匣时显示
     */
    MAG_EXTENDED_2(_BoneTag.Node.MAG_EXTENDED_2),
    /**
     * 安装三级扩容弹匣时显示
     */
    MAG_EXTENDED_3(_BoneTag.Node.MAG_EXTENDED_3),
    /**
     * 没有安装扩容弹匣时显示
     */
    MAG_STANDARD(_BoneTag.Node.MAG_STANDARD),
    /**
     * 有瞄具时显示，用于放瞄具的导轨（如 AKM 的导轨）
     */
    MOUNT(_BoneTag.Node.MOUNT),
    /**
     * 无瞄具时可见，机械瞄具
     */
    SIGHT(_BoneTag.Node.SIGHT),
    /**
     * 有瞄具时显示，折叠的机械瞄具
     */
    SIGHT_FOLDED(_BoneTag.Node.SIGHT_FOLDED),
    /**
     * 可以被理解为：在玩家用枪械的机械瞄具瞄准时，玩家眼球的位置和朝向
     */
    IRON_VIEW(_BoneTag.Node.IRON_VIEW),
    /**
     * 默认的改装界面定位组
     */
    REFIT_VIEW(_BoneTag.Node.REFIT_VIEW),
    /**
     * 第三人称枪械定位组
     */
    THIRD_PERSON_HAND_ORIGIN(_BoneTag.Node.THIRD_PERSON_HAND_ORIGIN),
    /**
     * 展示框定位组
     */
    FIXED_ORIGIN(_BoneTag.Node.FIXED_ORIGIN),
    /**
     * 掉落物定位组
     */
    GROUND_ORIGIN(_BoneTag.Node.GROUND_ORIGIN),
    /**
     * 抛壳起点定位组
     */
    SHELL_ORIGIN(_BoneTag.Node.SHELL_ORIGIN),
    /**
     * 枪口火焰定位组
     */
    MUZZLE_FLASH_ORIGIN(_BoneTag.Node.MUZZLE_FLASH_ORIGIN),
    /**
     * 第一人称左手手臂组
     */
    LEFTHAND_POS(_BoneTag.Node.LEFTHAND_POS),
    /**
     * 第一人称右手手臂组
     */
    RIGHTHAND_POS(_BoneTag.Node.RIGHTHAND_POS),
    /**
     * 弹匣定位组
     */
    MAG_NORMAL(_BoneTag.Node.MAG_NORMAL),
    /**
     * 换弹时第二个弹匣定位组
     */
    MAG_ADDITIONAL(_BoneTag.Node.MAG_ADDITIONAL),
    /**
     * 配件转接口
     */
    ATTACHMENT_ADAPTER(_BoneTag.Node.ATTACHMENT_ADAPTER),
    /**
     * 默认护木
     */
    HANDGUARD_DEFAULT(_BoneTag.Node.HANDGUARD_DEFAULT),
    /**
     * 战术护木
     */
    HANDGUARD_TACTICAL(_BoneTag.Node.HANDGUARD_TACTICAL),
    /**
     * 根组
     */
    ROOT(_BoneTag.Node.ROOT),
    // ----Attachment model----
    SCOPE_VIEW(_BoneTag.Node.SCOPE_VIEW),
    SCOPE_BODY(_BoneTag.Node.SCOPE_BODY),
    OCULAR_RING(_BoneTag.Node.OCULAR_RING),
    DIVISION(_BoneTag.Node.DIVISION),
    OCULAR(_BoneTag.Node.OCULAR),
    OCULAR_SIGHT(_BoneTag.Node.OCULAR_SIGHT),
    OCULAR_SCOPE(_BoneTag.Node.OCULAR_SCOPE),
    LASER_BEAM(_BoneTag.Node.LASER_BEAM);

    public final String typeName;
    NodeName(final String typeName) {
        this.typeName = typeName;
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getConstantName() {
        return this.typeName;
    }

    @Override
    public String getName() {
        return this.typeName;
    }
    @Override
    public boolean matches(@Nullable String name) {
        return name != null && name.equals(this.typeName);
    }
    @Override
    public @Nullable String getStrippedIfMatches(@Nullable String name) {
        return this.matches(name) ? "" : null;
    }

    private static final Map<String, NodeName> NODE_NAMES = new HashMap<>();

    static {
        for (NodeName name : NodeName.values()) {
            NODE_NAMES.put(name.typeName, name);
        }
    }

    public static @Nullable NodeName fromString(String name) {
        return name != null ? NODE_NAMES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }

    public enum Prefix implements ResourceTag.ConstantTag, INodeNameMatcher {
        /**
         * 抛壳起点定位组
         */
        SHELL_ORIGIN(_BoneTag.Node.SHELL_ORIGIN),
        SCOPE_VIEW(_BoneTag.Node.SCOPE_VIEW),
        DIVISION(_BoneTag.Node.DIVISION),
        OCULAR(_BoneTag.Node.OCULAR),
        OCULAR_SIGHT(_BoneTag.Node.OCULAR_SIGHT),
        OCULAR_SCOPE(_BoneTag.Node.OCULAR_SCOPE),
        LASER_BEAM(_BoneTag.Node.LASER_BEAM),
        /**
         * 改装界面视角的定位组前缀，实际名称为：前缀 + 配件名（小写）+ 后缀
         */
        REFIT_VIEW(_BoneTag.Node.Prefix.REFIT_VIEW);

        public final String typeName;
        public final String prefix;
        Prefix(final String typeName) {
            this.typeName = typeName;
            this.prefix = typeName + "_";
        }
        @Override public String getTagName() {
            return this.typeName;
        }
        @Override public String getConstantName() {
            return this.typeName;
        }

        @Override
        public String getName() {
            return this.prefix;
        }
        @Override
        public boolean matches(@Nullable String name) {
            return name != null && name.startsWith(this.prefix);
        }
        @Override
        public @Nullable String getStrippedIfMatches(@Nullable String name) {
            if (name == null || !name.startsWith(this.prefix)) {
                return null;
            }
            return name.substring(this.prefix.length());
        }

        private static final Map<String, Prefix> PREFIX_NAMES = new HashMap<>();

        static {
            for (Prefix name : Prefix.values()) {
                PREFIX_NAMES.put(name.typeName, name);
            }
        }

        public static @Nullable Prefix fromString(String name) {
            return name != null ? PREFIX_NAMES.get(name) : null;
        }

        @Override
        public String toString() {
            return this.typeName;
        }
    }
    public enum Suffix implements ResourceTag.ConstantTag, INodeNameMatcher {
        ILLUMINATE(_BoneTag.Node.Suffix.ILLUMINATE),
        /**
         * 配件定位组后缀，实际名称为配件名（小写）加上这个
         */
        ATTACHMENT_POS(_BoneTag.Node.Suffix.ATTACHMENT_POS),
        /**
         * 默认配件组后缀，会在安装配件后隐藏，实际名称为配件名（小写）加上这个
         */
        DEFAULT_ATTACHMENT(_BoneTag.Node.Suffix.DEFAULT_ATTACHMENT),
        /**
         * 改装界面视角的定位组后缀，实际名称为：前缀 + 配件名（小写）+ 后缀
         */
        REFIT_VIEW(_BoneTag.Node.Suffix.REFIT_VIEW);

        public final String typeName;
        public final String suffix;
        Suffix(final String typeName) {
            this.typeName = typeName;
            this.suffix = "_" + typeName;
        }
        @Override public String getTagName() {
            return this.typeName;
        }
        @Override public String getConstantName() {
            return this.typeName;
        }

        @Override
        public String getName() {
            return this.suffix;
        }
        @Override
        public boolean matches(@Nullable String name) {
            return name != null && name.endsWith(this.suffix);
        }
        @Override
        public @Nullable String getStrippedIfMatches(@Nullable String name) {
            if (name == null || !name.endsWith(this.suffix)) {
                return null;
            }
            return name.substring(0, name.length() - this.suffix.length());
        }

        private static final Map<String, Suffix> SUFFIX_NAMES = new HashMap<>();

        static {
            for (Suffix name : Suffix.values()) {
                SUFFIX_NAMES.put(name.typeName, name);
            }
        }

        public static @Nullable Suffix fromString(String name) {
            return name != null ? SUFFIX_NAMES.get(name) : null;
        }

        @Override
        public String toString() {
            return this.typeName;
        }
    }
}
