package dev.xcolorful.customgun.core.api.minecraft.tab;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.item.AttachmentProperty;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.builder.AttachmentBuilder;
import dev.xcolorful.customgun.core.api.minecraft.IMcRegistry;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import dev.xcolorful.customgun.core.resource.data.index.AttachmentIndex;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AttachmentTab {

    public static Comparator<Map.Entry<Identifier, AttachmentIndexInstance>> indexSort() {
        return Comparator.comparingInt(entry -> entry.getValue().getPojo().getSlotSort());
    }

    public static List<ItemStack> buildAttachmentItems(AttachmentCategory attachmentCategory) {
        List<ItemStack> attachmentItems = new ArrayList<>();
        ResourceApi.getAllAttachmentIndexInstance().stream().sorted(indexSort()).forEach(entry -> {
            @NotNull AttachmentIndexInstance attachmentIndexInstance = entry.getValue();

            AttachmentIndex attachmentIndex = attachmentIndexInstance.getPojo();
            if (attachmentIndex.getHideInGame()) {
                return;
            }

            if (attachmentIndex.getAttachmentCategory() == attachmentCategory) {
                ItemStack attachmentItem = buildAttachmentItem(entry.getKey(),
                        attachmentCategory);
                attachmentItems.add(attachmentItem);
            }
        });
        return attachmentItems;
    }

    public static ItemStack buildAttachmentItem(Identifier attachmentLocation, AttachmentCategory attachmentCategory) {
        return AttachmentBuilder.create(ModItems.ATTACHMENT.get())
                // 配件ResourceLocation
                .setProperty(AttachmentProperty.ATTACHMENT_LOCATION,
                        Identifier.class,
                        attachmentLocation)
                // 配件类型
                .setProperty(AttachmentProperty.ATTACHMENT_CATEGORY,
                        AttachmentCategory.class,
                        attachmentCategory)
                .build();
    }

    public static @NotNull Identifier MUZZLE_ICON_LOCATION;
    public static @NotNull Identifier LASER_ICON_LOCATION;
    public static @NotNull Identifier GRIP_ICON_LOCATION;
    public static @NotNull Identifier MAGAZINE_ICON_LOCATION;
    public static @NotNull Identifier SCOPE_ICON_LOCATION;
    public static @NotNull Identifier STOCK_ICON_LOCATION;
    static {
        IMcRegistry mcRegistry = CustomGun.getMcRegistry();
        MUZZLE_ICON_LOCATION = mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.TAB_ID, "muzzle_icon"));
        LASER_ICON_LOCATION = mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.TAB_ID, "laser_icon"));
        GRIP_ICON_LOCATION = mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.TAB_ID, "grip_icon"));
        MAGAZINE_ICON_LOCATION = mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.TAB_ID, "magazine_icon"));
        SCOPE_ICON_LOCATION = mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.TAB_ID, "scope_icon"));
        STOCK_ICON_LOCATION = mcRegistry.createResourceLocation(String.format("%s:%s", CustomGun.TAB_ID, "stock_icon"));
    }

    public static ItemStack buildMuzzleTabItem() {
        // mixin注入点
        return buildAttachmentItem(MUZZLE_ICON_LOCATION,
                AttachmentCategory.MUZZLE);
    }
    public static ItemStack buildLaserTabItem() {
        // mixin注入点
        return buildAttachmentItem(LASER_ICON_LOCATION,
                AttachmentCategory.LASER);
    }
    public static ItemStack buildGripTabItem() {
        // mixin注入点
        return buildAttachmentItem(GRIP_ICON_LOCATION,
                AttachmentCategory.GRIP);
    }
    public static ItemStack buildMagazineTabItem() {
        // mixin注入点
        return buildAttachmentItem(MAGAZINE_ICON_LOCATION,
                AttachmentCategory.MAGAZINE);
    }
    public static ItemStack buildScopeTabItem() {
        // mixin注入点
        return buildAttachmentItem(SCOPE_ICON_LOCATION,
                AttachmentCategory.SCOPE);
    }
    public static ItemStack buildStockTabItem() {
        // mixin注入点
        return buildAttachmentItem(STOCK_ICON_LOCATION,
                AttachmentCategory.STOCK);
    }
}
