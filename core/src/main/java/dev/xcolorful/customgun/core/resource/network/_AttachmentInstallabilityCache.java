package dev.xcolorful.customgun.core.resource.network;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.gun.GunDataAccessor;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource._DataInstanceManager;
import dev.xcolorful.customgun.core.resource.data.modtags.AttachmentTagData;
import dev.xcolorful.customgun.core.resource.data.modtags.GunAttachmentData;
import dev.xcolorful.customgun.core.util.ClassUtils;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 查询声明的Attachment安装关系
 * <ul>
 *     <li>仅判断是否包含 "Attachment->Gun" 映射</li>
 *     <li>实际 "Gun->Attachment" 应使用{@link IGun}加一层动态拦截, 如{@link GunDataAccessor#canInstallAttachment}</li>
 * </ul>
 */
public class _AttachmentInstallabilityCache {

    public static final String TAG_PREFIX = "#";
    public static final int TAG_PREFIX_LENGTH = TAG_PREFIX.length();
    private final Map<Identifier, ClassUtils.ArraySet<Identifier>> attachmentInstallability;

    @ApiStatus.Internal
    public _AttachmentInstallabilityCache() {
        this.attachmentInstallability = new HashMap<>();
    }

    /**
     * 主线程操作(线程不安全)
     * <p>
     * 命名跟{@link _DataInstanceManager#clear()}保持同构
     */
    public void clear() {
        this.attachmentInstallability.clear();
    }
    /**
     * 主线程操作(线程不安全)
     * <p>
     * 命名跟{@link _DataInstanceManager#clear()}保持同构
     */
    public void reload() {
        long t0 = System.nanoTime();
        this.rebuildCache();
        long t1 = System.nanoTime();
        CustomGun.LOGGER.debug("_AttachmentInstallabilityCache: rebuildCache {} ({} side): {} ms",
                this.attachmentInstallability.size(),
                CustomGun.getSideExecutor().getLogicalSide().isClient() ? "client" : "server",
                (t1 - t0) / 1_000_000.0);
    }

    // --------Getter--------

    public boolean hasAttachmentInstallability(Identifier attachmentLocation, Identifier gunLocation) {
        var installability = this.getAttachmentInstallability(attachmentLocation);
        return installability != null && installability.contains(gunLocation);
    }
    public @Nullable ClassUtils.ArraySet<Identifier> getAttachmentInstallability(Identifier attachmentLocation) {
        return this.attachmentInstallability.get(attachmentLocation);
    }

    // --------答辩区--------

    private void rebuildCache() {
        IMcRegistry mcRegistry = CustomGun.getMcRegistry();
        Collection<Map.Entry<Identifier, AttachmentTagData>> allTagData = ResourceApi.getAllAttachmentTagData();

        // ----单次循环提取直接配件和子标签----
        int initialSize = allTagData.size();
        Map<Identifier, List<Identifier>> tagToDirectAttachments = new HashMap<>(initialSize); // 直接写attachment
        Map<Identifier, List<Identifier>> tagToSubTags = new HashMap<>(initialSize); // 用#开头继续链接

        // 遍历配件tag (Pojo)
        int initialCapacity = AttachmentCategory.values().length;
        for (Map.Entry<Identifier, AttachmentTagData> entry : allTagData) {
            var tagRl = entry.getKey();
            List<String> rawTags = entry.getValue().getTags();
            List<Identifier> directs = new ArrayList<>(rawTags.size());
            List<Identifier> subs = new ArrayList<>(initialCapacity); // 默认最多就每个类型链接一个 (group性质的pojo tag)

            for (String s : rawTags) {
                if (s.startsWith(TAG_PREFIX)) { // 标签
                    var subRl = mcRegistry.createResourceLocation(s.substring(TAG_PREFIX_LENGTH));
                    if (subRl != null) subs.add(subRl);
                    else CustomGun.LOGGER.warn("_AttachmentInstallabilityCache: Failed to create sub ResourceLocation for tag: {}", s);
                } else { // 直写
                    var directRl = mcRegistry.createResourceLocation(s);
                    if (directRl != null) directs.add(directRl);
                    else CustomGun.LOGGER.warn("_AttachmentInstallabilityCache: Failed to create direct ResourceLocation for attachment: {}", s);
                }
            }
            tagToDirectAttachments.put(tagRl, directs);
            if (!subs.isEmpty()) tagToSubTags.put(tagRl, subs);
        }

        // ----展开tag进行一层去重与内存分流----
        Map<Identifier, Collection<Identifier>> tagFullMap = new HashMap<>(initialSize);
        for (var tagRl : tagToDirectAttachments.keySet()) {
            List<Identifier> directs = tagToDirectAttachments.get(tagRl);
            List<Identifier> subs = tagToSubTags.get(tagRl);

            if (subs == null) {
                // 无子标签，直接复用 ArrayList 引用，零内存分配
                tagFullMap.put(tagRl, directs);
            } else {
                // 有子标签，合并去重（限制在一层）
                Set<Identifier> fullSet = new HashSet<>(directs);
                for (var subTagRl : subs) {
                    List<Identifier> subDirects = tagToDirectAttachments.get(subTagRl);
                    if (subDirects != null) {
                        fullSet.addAll(subDirects);
                    }
                }
                tagFullMap.put(tagRl, fullSet);
            }
        }

        // ----反向映射 Attachment -> Set<Gun>----
        Map<Identifier, Set<Identifier>> attachmentToGuns = new HashMap<>();
        Collection<Map.Entry<Identifier, GunAttachmentData>> allGunData = ResourceApi.getAllGunAttachmentData();

        // 遍历枪械tag (Pojo)
        for (Map.Entry<Identifier, GunAttachmentData> entry : allGunData) {
            var gunRl = entry.getKey();
            List<String> rawTags = entry.getValue().getTags();

            for (String s : rawTags) {
                if (s.startsWith(TAG_PREFIX)) { // 标签
                    var tagRl = mcRegistry.createResourceLocation(s.substring(TAG_PREFIX_LENGTH));
                    if (tagRl == null) {
                        CustomGun.LOGGER.warn("_AttachmentInstallabilityCache: Failed to create ResourceLocation for gun tag: {}", s);
                        continue;
                    }
                    Collection<Identifier> attachments = tagFullMap.get(tagRl);
                    if (attachments != null) {
                        for (var attRl : attachments) {
                            Set<Identifier> guns = attachmentToGuns.computeIfAbsent(attRl, k -> new HashSet<>());
                            guns.add(gunRl);
                        }
                    }
                } else { // 直写
                    var attRl = mcRegistry.createResourceLocation(s);
                    if (attRl != null) attachmentToGuns.computeIfAbsent(attRl, k -> new HashSet<>()).add(gunRl);
                    else CustomGun.LOGGER.warn("_AttachmentInstallabilityCache: Failed to create direct ResourceLocation for gun attachment: {}", s);
                }
            }
        }

        // ----排序并写入最终缓存----
        Map<Identifier, Integer> gunSorts = ResourceApi.getAllGunSort();
        attachmentToGuns.forEach((attRl, gunSet) -> {
            ClassUtils.ArraySet<Identifier> gunArraySet = new ClassUtils.ArraySet<>();

            // 只有1把枪适配可跳过new和排序
            if (gunSet.size() > 1) {
                List<Identifier> sortedGuns = new ArrayList<>(gunSet);
                sortedGuns.sort(Comparator.comparingInt(rl -> gunSorts.getOrDefault(rl, Integer.MAX_VALUE)));
                gunArraySet.addAll(sortedGuns);
            } else {
                gunArraySet.addAll(gunSet);
            }

            this.attachmentInstallability.put(attRl, gunArraySet);
        });
    }
}
