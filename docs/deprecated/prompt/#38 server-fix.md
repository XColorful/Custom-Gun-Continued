# 

```
目前遇到以下问题：
- 在连接到多人游戏时，服务端会因为以下问题而断开与客户端的连接：
日志内容为：
[03Sep2026 17:50:21.346] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: XiaoColorful joined the game
[03Sep2026 17:50:21.382] [Server thread/INFO] [net.minecraft.server.network.ServerGamePacketListenerImpl/]: XiaoColorful lost connection: Internal Exception: io.netty.handler.codec.EncoderException: java.lang.NullPointerException: Cannot invoke "net.minecraft.world.item.ItemStack.m_41619_()" because "p_130056_" is null
[03Sep2026 17:50:21.383] [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: XiaoColorful left the game

经测试，1.20.1forge、1.20.2forge/neoforge稳定触发，1.20.4forge/neoforge、1.21.1neoforge、26.2neoforge均不触发

经查询：
- ItemStack.m_41619_()为ItemStack.isEmpty()
- 没在ServerGamePacketListenerImpl里查到p_130056_的位置，估计是在哪个网络包的解析里出问题的

目前初步怀疑是1.20.1在dev.xcolorful.customgun.core.util.NetworkUtils#writeItem引入的参数问题：
- 修改成buffer.writeItemStack(item, true);后，测试仍然稳定触发bug
- 但是1.20.4仍然使用该接口却没问题
- 因此排除该位置

目前打开了4个IDEA窗口：1.20.1、1.20.2、1.20.4、26.2，供对照浏览不同版本源码的实现差异

请排查并修复该问题，修改完后让我测试检查，不要commit
```

# 

I'll investigate this server-side NullPointerException. Let me start by understanding the project structure and the network code involved.

I've found the root cause. Let me verify my understanding of the exact null-flow before making the fix.

I've confirmed the root cause. Now let me apply the fix to `TableResult.java`.

排查完成。问题根因已定位并修复。

## 结论

NPE 不在 `NetworkUtils.writeItem`，而是**配方同步（recipe sync）时序列化了一个 `null` 的 `ItemStack`**。

## 根因

错误信息里的 `p_130056_` 正是 `FriendlyByteBuf.writeItemStack(ItemStack p_130056_, boolean limitedTag)` 的形参（我已通过 IDEA 反编译确认 1.20.1 源码）。服务端在玩家加入时发送 `ClientboundUpdateRecipesPacket`，逐条调用配方的 `toNetwork`，其中模组的 [TableRecipeSerializer.toNetwork](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/TableRecipeSerializer.java:75) 执行：

```java
NetworkUtils.writeItem(buffer, tableResult.getResultItem());  // getResultItem() 返回 null → NPE
```

`getResultItem()` 为 null 的链路：

1. [TableRecipeSerializer._fromJson](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/TableRecipeSerializer.java:43) → `TableRecipe.fromPojo` → [TableResult.fromPojo](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/TableResult.java:42) 执行 `new TableResult(pojo, null, null, null)`，把 `resultItem` 和 `tabGroupLocation` 都设成了 `null`。
2. 本应由 [TableResult.prepare](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/TableResult.java:50) 通过 `_TableResultRaw` 懒解析出结果物品，但 `raw` 字段从未被创建（`_TableResultRaw` 是全项目死代码，`fromPojo` 里传的是 `null`），所以 `prepare()` 是空操作，`resultItem` 一直是 `null`。

于是配方同步时 `writeItemStack(null)` 触发 NPE。（顺带一提，`getTabGroupLocation()` 同样是 null，会在下一行 `writeResourceLocation(null)` 触发第二个 NPE，只是被第一个遮住了。）

你之前把 `writeItemStack(item, false)` 改成 `true` 仍复现，正是因为 `false/true` 只影响 NBT 详情的 `limitedTag` 参数，**NPE 发生在读取该参数之前的 `p_130056_.isEmpty()`**，与它无关。

## 修复

在 [TableResult.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/TableResult.java) 让两个 getter 对 null 兜底：

```java
public ItemStack getResultItem() {
    return this.resultItem != null ? this.resultItem : ItemStack.EMPTY;
}
public ResourceLocation getTabGroupLocation() {
    return this.tabGroupLocation != null ? this.tabGroupLocation : TabGroup.GUN_CUSTOM.registryLocation;
}
```

`TabGroup.GUN_CUSTOM` 兜底与 [_TableResultRaw.getEmpty()](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/_TableResultRaw.java:64) 现有逻辑一致。

## 关于版本差异（1.20.4 不触发）

我把 1.20.1/1.20.2/1.20.4 的整个 recipe 包做了逐文件 diff，`TableResult`、`TableRecipe`、`TableRecipeSerializer`、`_TableResultRaw`、`ModRecipe` 在 1.20.2 与 1.20.4 之间**完全相同**，vanilla 的 `ClientboundUpdateRecipesPacket` 也都调用 `serializer.toNetwork`。也就是说「resultItem 为 null」这个缺陷在所有版本代码里都存在，差异只可能出在**配方是否真正被加载进 `RecipeManager`**（1.20.4 的 `fromJson` 不再 `@Override`，改走 `codec()` 路径，实际是否加载成功我无法仅靠静态代码 100% 确定）。

这层版本差异不影响修复方向：只要配方被加载并同步，这个 null 就会把 `writeItemStack(null)` 喂进去；我的修复把 null 变成 `ItemStack.EMPTY`，从根源上消除 NPE。

## 需要你注意的更深层问题

`fromPojo` 没有真正构建配方结果物品（`_TableResultRaw` 未被接线），所以当前配方产物实际是「空」的。我这个修复只让产物「非 null（空）」以避免崩溃，**并没有实现完整的配方产物解析**（那属于 `fromPojo` → `_TableResultRaw` 的接线，是另一个功能点）。如果配方合成需要产出真实枪械/配件/子弹物品，需要另行实现。

## 状态

- 只改了 [TableResult.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/TableResult.java) 一个文件，未 commit。
- 请在 1.20.1 / 1.20.2 连接多人游戏测试确认是否还复现。如果仍复现，请把完整堆栈（`latest.log`/`debug.log` 里 `EncoderException` 上方的 `at ...` 调用链）发我，我可以据此进一步定位是否还有第二个 null 来源。

# 

```
你对比我跟TaCZ的设计差异，看看我怎么改来完成TaCZ已有的功能
不需要把类名、调用链路改成跟TaCZ完全一样
已有的迁移映射见./docs-tacz/TaCZ Migration Mapping.md

TaCZ的源码应该添加到目录，你可以通过IDEA访问源码

修改完后不要commit，我来检查方案是否合适
```

# 

I now understand the TaCZ design fully. The key finding: TaCZ's `GunSmithTableResultSerializer` creates a `RawGunTableResult` (or a direct `ItemStack`) at deserialization time, and `init()` resolves it later — exactly the CGC `_TableResultRaw` + `prepare()` design, but CGC's `TableResult.fromPojo` never wires it up. Let me verify the current state of the files and apply the fix.

Now I'll apply the fix. First, the imports in `TableResult.java`:

Now the ammo count fix in `_TableResultRaw.java`:

I found a critical ordering issue. Let me re-read the current `_AllDataManager.onTagsUpdateEvent` to confirm the exact state.

对比完成，已补齐 CGC 配方结果解析（TaCZ 的 `GunSmithTableResult`/`RawGunTableResult` 对应功能）。共改了 3 个文件，未 commit。

## 设计差异对比

TaCZ 的配方结果分三步，CGC 都有对应物，但**缺了中间的「接线」那一步**：

|步骤|TaCZ|CGC（改前）|
|---|---|---|
|反序列化时生成「结果占位」|`GunSmithTableResultSerializer.deserialize` 读 JSON，GUN/AMMO/ATTACHMENT 时 `new RawGunTableResult(type,id,count)`，CUSTOM 时 `CraftingHelper.getItemStack(item)`|`TableResult.fromPojo` 直接 `new TableResult(pojo, null, null, null)` —— **丢掉了 type/id/count，没建 `_TableResultRaw`**|
|懒解析（reload 完成后）|`GunSmithTableResult.init()` → `RawGunTableResult.init()` 构建 ItemStack|`TableResult.prepare()` → `_TableResultRaw.getTableResultOrEmpty()` —— 已有，但 `raw` 恒为 null，等于空操作|
|触发时机|`CommonAssetsManager.onReload(TagsUpdatedEvent)` → `recipe.init()`|`_AllDataManager.onTagsUpdateEvent` → `tableRecipe.prepare()` —— 已有|

所以根因是：`_TableResultRaw` 和 `prepare()` 这套骨架早就在，只是 `fromPojo` 从没把 `raw` 接上。

## 改动

**1. [TableResult.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/TableResult.java) — 补上 `fromPojo` 接线**

```java
public static TableResult fromPojo(_TableResultData pojo) {
    RecipeResultType resultType = pojo.getResultType();
    if (resultType == null) {
        return new TableResult(pojo, ItemStack.EMPTY, pojo.getTabGroupLocation(), null);
    }
    int resultCount = Math.max(1, pojo.getResultCount());
    return switch (resultType) {
        case GUN, ATTACHMENT, AMMO -> {           // 引用型：建 raw，交给 prepare() 懒解析
            ResourceLocation pojoLocation = pojo.getPojoLocation();
            if (pojoLocation == null) yield new TableResult(pojo, ItemStack.EMPTY, pojo.getTabGroupLocation(), null);
            yield new TableResult(pojo, null, pojo.getTabGroupLocation(),
                    new _TableResultRaw(resultType.toString(), pojoLocation, resultCount));
        }
        case CUSTOM -> new TableResult(pojo, buildCustomResult(pojo, resultCount), pojo.getTabGroupLocation(), null); // 直接物品
    };
}
```

`buildCustomResult` 从 `_ResultItemData`（item_location + item_nbt）直接构造 ItemStack，对应 TaCZ 的 CUSTOM 分支（`CraftingHelper.getItemStack`）。

**2. [_TableResultRaw.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/recipe/_TableResultRaw.java) — 补子弹数量**

`getAmmoItemOrEmpty()` 漏了 `ammoItem.setCount(this.resultCount)`（枪和配件都有，唯独子弹没有），对应 TaCZ `RawGunTableResult.getAmmoStack()` 的 `.setCount(count)`。

**3. [_AllDataManager.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/resource/_AllDataManager.java) — 修正初始化顺序**

这是接线后暴露出来的**连带 bug**：`prepare()` 通过 `ResourceApi.getGunIndexInstance()` 读 `_DataInstanceManager` 里的**实例**，但原代码里 `_DataInstanceManager.reload()`（构建实例）排在 `tableRecipe.prepare()` **之后**，导致 `prepare()` 解析时索引实例还是空的，结果永远解析成空物品。已把 `reload()` 提前到 `prepare()` 之前。

## 关于 Deprecated 的部分

迁移映射里 `RawGunTableResult.extraData`（`GunResult`：ammoCount + attachments）和 `nbt` 都标注了 `_Deprecated_`，CGC 的 `_TableResultData`/文档里也把它们划掉了（`~~ammo_count~~`、`~~nbt~~`），所以我没有实现它们——只补了 `type/id/count` 这条主路径。如果后续需要「配方产物预装子弹/预装配件」，需要另行把 `GunResult` 接回来。

## 说明

- 只改了当前 1.20.1 分支（按 CLAUDE.md 不切分支、不 commit）。recipe 这套代码在 1.20.1/1.20.2/1.20.4 之间是同一份，这三处改动对其它分支同样适用。
- 建议测试点：加载带 `table_recipe` 配方 JSON 的枪包，`/reload` 后看服务端日志，再连接客户端确认配方同步不再 NPE、且产物物品是真实枪械/配件/子弹（不再是空物品）。

# 

```
我提交了commit，你重新阅读最新commit修改的内容，研究一下_AllDataManager和_TableResultRaw里新增的TODO提到的问题，并尝试修复。
```

# 

I've done extensive research. Now let me verify my understanding is complete before cleaning up. The core finding is that the raw-POJO approach is actually correct, and the "instances empty" is expected behavior (the `*IndexInstance` does shooting-specific validation). Let me confirm the `_TableResultRaw` methods are otherwise sound by checking `GunData.getDefaultFireModeType` null-safety.

I've confirmed the root cause. Now let me clean up the TODOs and debug logs, since the raw-POJO approach the user already switched to is actually the correct fix.

研究完成。我读了最新 commit `3b04fbe1` 的改动，定位了这两个 TODO 背后的根因，并做了清理修复。

## 根因分析

两个 TODO 本质是同一个疑问：**为什么 `onTagsUpdateEvent` 里 `_DataInstanceManager.reload()` 之后，`getAllGunIndexInstance()` 还是 0？**

我沿着调用链追下去，结论是：**实例其实是能构建的，只是被你看到的「0」误导了**——真正的原因是 `GunIndexInstance` 在构建实例时做了一轮「射击向」的二次校验：

- [GunIndexInstance.fromPojo](core/src/main/java/dev/xcolorful/customgun/core/resource/instance/data/GunIndexInstance.java:39) → `isPojoValid()` → 除了 `pojo.isValid()` + `resetCache()`，还额外检查 `defaultMagSize >= 1`、`rpm >= 1`、`fireModeTypes` 非空、`recoilData` 合法（[第 84-95 行](core/src/main/java/dev/xcolorful/customgun/core/resource/instance/data/GunIndexInstance.java:84)）。
- 这些是**射击逻辑**需要的约束（弹匣为 0 的枪没法打），但对**配方产物**而言是过严的——一把 magSize=0 的展示枪/测试枪，仍然应该能被配方造出来。

所以你在 `_TableResultRaw` 里改成直接读原始 Pojo（`ResourceApi.getGunIndex` + `getGunData`）是**正确**的：配方产物只用到「位置 / 开火模式 / 分类」这几个基础字段，而这些原始 Pojo 在 reload 的 `apply()` 阶段（`TagsUpdatedEvent` 之前）就已经就绪，不需要也不应该经过射击向的实例校验。

时序上我也确认了没有真正的 bug：`TagsUpdatedEvent` 由 `ReloadableServerResources.updateRegistryTags` 发出，发生在 `loadResources`（含所有 PojoManager 的 `prepare()`/`apply()`）**之后**，所以 `reload()` 读到的 `pojoMap` 是完整加载好的。

## 改动

**1. [\_TableResultRaw.java](core/src/main/java/dev/xcolorful/customgun/core/recipe/_TableResultRaw.java)** —— 去掉 TODO 和调试日志，把「为什么读原始 Pojo」这个设计决策固化下来：

```java
/**
 * 这里读原始 Pojo 而非 {@code *IndexInstance}：配方产物只依赖枪械/配件/子弹的基础字段
 * （位置、开火模式、分类），不依赖 Instance 针对射击逻辑的二次校验（弹匣容量、射速等）。
 */
```

同时删除了 4 处 `测试log，待删` 的 debug 日志、注释掉的 `getAllGunIndexInstance()` 调试行，以及不再使用的 `CustomGun` import。

**2. [\_AllDataManager.java](core/src/main/java/dev/xcolorful/customgun/core/resource/_AllDataManager.java)** —— 删掉那个「prepare 应该能拿到 instance 数据但貌似没有」的 TODO，并把注释改成准确表述（`reload` 读的是 `apply` 阶段写入的 `pojoMap`）。保留了 `reload()` 在 `prepare()` 之前的顺序（无害，且语义上更清楚）。

## 需要你注意的一点

这次是**清理**，不是新的功能修复——`_TableResultRaw` 读原始 Pojo 的方案本身已经是正确的（这也是你 commit 标题「Fix table recipe not loaded」生效的原因）。

如果你测试时发现**配方仍然解析成空物品**，那大概率是数据层的引用对不上，而不是代码问题，可以用你加的 `/customgun debug testAllRecipes` 确认，并重点检查两个引用：

1. 配方 JSON 的 `pojo_location`（`id`）是否等于枪械索引的 key（索引文件路径去掉前缀后的 ResourceLocation）；
2. 枪械索引 JSON 的 `data_location` 是否等于枪械 data 的 key。

若这两处对不上，`getGunIndex` / `getGunData` 会返回 null，配方回落到空物品——这是数据一致性要求，TaCZ 也是同样的约束。

未 commit，等你检查。

# 

```
我添加了_AllDataManager的测试日志，现在发现无论是客户端单人游戏，还是专用服务端多人游戏，都显示逻辑侧是client
这可能是潜在的问题需要修复
修复之后把我刚在_TableResultRaw添加的测试日志也删掉
```