package dev.xcolorful.customgun.core.resource.network;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.event.ITagsUpdatedEvent;
import dev.xcolorful.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import dev.xcolorful.customgun.core.resource._DataInstanceManager;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/*
文档译名：配件修饰缓存 (XiaoColorful译)
 */
/**
 * 对{@link AttachmentData}中非null的项构建modifier缓存
 * <ul>
 *     <li>代替在{@link AttachmentData}内使用{@code Map}</li>
 * </ul>
 */
public class _AttachmentModifierCache {

    private final Map<Identifier, Map<AttachmentModifierType, Object>> attachmentModifiers;

    @ApiStatus.Internal
    public _AttachmentModifierCache() {
        this.attachmentModifiers = new HashMap<>();
    }

    /**
     * 主线程操作(线程不安全)
     * <li>命名跟{@link _DataInstanceManager#clear()}保持同构</li>
     */
    public void clear() {
        this.attachmentModifiers.clear();
    }
    /**
     * 主线程操作(线程不安全)
     * <li>命名跟{@link _DataInstanceManager#clear()}保持同构</li>
     * <li>必须由调用方传入自己这一侧的索引实例，原因见 {@link ITagsUpdatedEvent}</li>
     */
    public void reload(@NotNull Map<Identifier, AttachmentIndexInstance> attachmentIndexInstances) {
        long t0 = System.nanoTime();
        this.rebuildCache(attachmentIndexInstances);
        long t1 = System.nanoTime();
        CustomGun.LOGGER.debug("_AttachmentModifierCache: rebuildCache {} ({} side): {} ms",
                this.attachmentModifiers.size(),
                CustomGun.getSideExecutor().getLogicalSide().isClient() ? "client" : "server",
                (t1 - t0) / 1_000_000.0);
    }

    // --------Getter--------

    /**
     * @return 该配件对应的 modifier（{@link AttachmentModifierType} -> 原始 modifier 值），无则返回 null
     */
    public @Nullable Map<AttachmentModifierType, Object> getModifiers(@NotNull Identifier attachmentLocation) {
        return this.attachmentModifiers.get(attachmentLocation);
    }

    // --------答辩区--------

    private void rebuildCache(@NotNull Map<Identifier, AttachmentIndexInstance> attachmentIndexInstances) {
        for (Map.Entry<Identifier, AttachmentIndexInstance> entry : attachmentIndexInstances.entrySet()) {
            this.recompute(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 计算配件对应的 modifier 缓存
     * <li>使用调用方自己的索引实例，原因见 {@link ITagsUpdatedEvent}</li>
     */
    @ApiStatus.Internal
    public @Nullable Map<AttachmentModifierType, Object> recompute(@NotNull Identifier attachmentLocation,
                                                                   @Nullable AttachmentIndexInstance attachmentIndexInstance) {
        @Nullable Map<AttachmentModifierType, Object> modifiers = null; {
            if (attachmentIndexInstance != null) {
                AttachmentData attachmentData = attachmentIndexInstance.getAttachmentData();
                modifiers = _computeModifiers(attachmentData);
            }
        }

        this.attachmentModifiers.put(attachmentLocation, modifiers);
        return modifiers;
    }
    private static final AttachmentModifierType[] ATTACHMENT_MODIFIER_TYPES = AttachmentModifierType.values();
    private static @Nullable Map<AttachmentModifierType, Object> _computeModifiers(AttachmentData attachmentData) {
        @Nullable Map<AttachmentModifierType, Object> result = null;
        for (AttachmentModifierType type : ATTACHMENT_MODIFIER_TYPES) {
            @Nullable Object rawModifier = type.getModifier().getModifier(attachmentData);
            if (rawModifier == null) continue;

            if (result == null) result = new HashMap<>();
            result.put(type, rawModifier);
        }
        return result;
    }
}
