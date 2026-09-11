> #47 提示词

# 

```
现在需要修复安装了扩容弹夹，却不使用扩容弹夹的容量的问题

当前git暂存区是我临时加的测试日志

- 测试枪械为游戏目录（1.20.1test）下.\saves\新的世界2\datapacks\classic_battleroyale_gun\data\cbrg\data\guns\beryl_m762_data.json
- 配件为.\saves\新的世界2\datapacks\classic_battleroyale_gun\data\cbrg\data\attachments
- 可以通过IDEA的mcp访问tacz源码
- tacz类到cgc的迁移映射在./docs-tacz/TaCZ Migration Mapping.md\extended_quickdraw_mag_rifle_data.json

检查并修复问题，完成后通知我检查
```

# 

I'll start by looking at the current changes and the relevant code.

Let me find the `getMagAmmoLimit` implementation.

Found the bug. Let me confirm the imports in `GunDataAccessor`.

Diagnosis confirmed. Now applying the fix.

Now removing the temporary debug logging.

修复完成。

## 根因

`GunDataAccessor.getMagAmmoLimit()` (core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/GunDataAccessor.java:385) 把**配件索引 location** 当成**配件数据 location** 去查数据：

```java
var attachmentLocation = this.getAttachmentLocation(gunItem, AttachmentCategory.MAGAZINE);
AttachmentData attachmentData = ResourceApi.getAttachmentData(attachmentLocation); // 永远为 null
```

- 枪械 NBT 里存的 `attachment_rl` 是**索引** location（`cbrg:extended_quickdraw_mag_rifle`），来源见 [AttachmentTab.java:45](core/src/main/java/dev/xcolorful/customgun/core/api/minecraft/tab/AttachmentTab.java:45)、[_TableResultRaw.java:112](core/src/main/java/dev/xcolorful/customgun/core/recipe/_TableResultRaw.java:112)。
- 但 `ResourceApi.getAttachmentData()` 按**数据** location 索引，键是 `cbrg:extended_quickdraw_mag_rifle_data`（索引 json 的 `data` 字段，由 `FileToIdConverter` 去前缀去后缀得到）。

于是查不到 → `magazineCategory` 保持 `NONE` → 直接返回 `defaultMagSize`（30），扩容弹夹等级被完全忽略。项目里其它地方（[GunDataAccessor.java:223](core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/GunDataAccessor.java:223) 近战、[_DefaultGunFire.java:104](core/src/main/java/dev/xcolorful/customgun/core/gun/attack/_DefaultGunFire.java:104) 消音器、[LivingShooterAim.java:135](core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterAim.java:135) 瞄具）用的都是 `getAttachmentIndexInstance(location)` → `getAttachmentData()`，只有这两处走错了。

## 改动

1. [GunDataAccessor.java:385](core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/GunDataAccessor.java:385) `getMagAmmoLimit`：改为经 `getAttachmentIndexInstance()` 取数据（沿用项目既有模式）。
2. [IGunScriptBackCompat.java:314](core/src/main/java/dev/xcolorful/customgun/core/api/script/context/IGunScriptBackCompat.java:314) `getMagExtentLevel()`：同一处缺陷（脚本 API 的扩容等级永远返回 0），一并修正；`AttachmentData` import 换成 `AttachmentIndexInstance`。
3. 移除了你临时加的 `[CGC-DEBUG]ammo limit` 日志（[_DefaultGunAction.java](core/src/main/java/dev/xcolorful/customgun/core/gun/action/_DefaultGunAction.java) 已恢复原样）。

修复后 `beryl_m762` 装 `extended_quickdraw_mag_rifle`（`extended_mag_level: 3`）→ `extended_mag_ammo_amount[2]` = 40；装 level 1/2 分别得到 30/42。

IDEA 编译通过（仅剩项目原有的 deprecated 警告）。

## 待确认的一点（未改）

[GunDataAccessor.java:399](core/src/main/java/dev/xcolorful/customgun/core/api/item/gun/GunDataAccessor.java:399) 当扩容等级超出 `extended_mag_ammo_size` 长度时返回 `0`。TaCZ 对应逻辑（`AttachmentDataUtils.getAmmoCountWithAttachment`）在 `extended_mag_ammo_amount` 为 null 时返回 `getAmmoAmount()`。若某把枪允许装扩容弹夹却没写 `extended_mag_ammo_size`，容量会变成 0。当前枪包数据没触发这条分支，所以没动；需要的话我再改。

请检查。
