> #41 提示词

# 

```
现在需要完成sound系统的修复
- 例如在LocalShooterDraw里_doPutAway调用了SoundPlayManager，但是没播放声音
- 本次修复只需要解决已有的能播放声音的地方的问题，对于待移植的功能，如开枪的逻辑尚未移植，则不需要补充sound相关地方
- IDEA打开了两个窗口，可以浏览tacz源码
- tacz类到cgc类的迁移映射在./docs-tacz/TaCZ Migration Mapping.md
- 目前移植的类命名和接口位置都已经确定，修复后不一定要把类名改回tacz的样子，但是要有原本的功能

我在SoundManagerMixin加了些log，确认声音都有在MOD_SOUNDS_OLD1（旧目录）读取到
- 完成后可以不用删这个日志，我手动discard changes就行

修改完需要说明哪些还没对应上

完成后通知我检查
```

# 

I'll start by understanding the codebase structure and the sound system. Let me read the key documentation and files in parallel.

Let me read the key files: LocalShooterDraw, SoundPlayManager, and related sound classes.

Let me read the prompt doc, SoundPlayManager, and the sound resource manager.

Let me examine the original TaCZ source and the CGC `GunDisplayInstance`.

Let me examine the vanilla Minecraft `SoundManager`, `Sound`, and `AbstractSoundInstance` sources to understand the exact resolution flow.

Now I understand the root cause. Let me implement the fix. First, copy the empty sound file and create the `sounds.json`:

Now fix the `currentSoundInstance` tracking in the 2-arg `playGunSound` overload (used by reload/inspect/bolt — the long, stoppable sounds):

Let me verify the changes compile by building the project:

Let me do a final review of all my changes:

我已完成修复。下面是完整说明。

## 根因

声音文件其实已经通过 `SoundManagerMixin` 正确加载进 sound 缓存了，但 **CGC 项目整个缺少 `sounds.json`** 这个资源文件。

`sounds.json` 负责把注册的 `SoundEvent`（`customgun:gun`）映射到一个真实可播放的声音。没有它，`ModSounds.GUN`（`customgun:gun`）在 `SoundManager.registry` 里查不到 `WeighedSoundEvents`，于是 `AbstractSoundInstance.resolve()` 返回 `null`，`SoundEngine.play()` 直接打印 `Unable to play unknown soundEvent` 并跳过播放——这就是 `_doPutAway` 里调了 `SoundPlayManager` 却听不到声音的原因。所有走 `SoundPlayManager.playClientSound` 的调用点（收枪、切枪、换弹、检视、拉栓、近战、命中音、动画音等）都被同一个问题挡住。

对照 TaCZ：它有 `assets/tacz/sounds.json`，把 `tacz:gun` 映射到一个占位音 `tacz:gun/empty`（配一个真实的空 OGG），这样 `gun` 事件能正常 resolve，之后再由 `ResourceSound`/`TaczSound` 把实际路径重定向到 gunpack 里的音效文件。

## 改动

1. **新增 [sounds.json](app://localhost/epitaxy/core/src/main/resources/assets/customgun/sounds.json)** —— 注册 `gun` 事件，映射到占位音 `customgun:gun/empty`（对齐 TaCZ 的 `tacz:gun` → `tacz:gun/empty`）。
2. **新增 [empty.ogg](app://localhost/epitaxy/core/src/main/resources/assets/customgun/sounds/gun/empty.ogg)** —— 从 TaCZ 复制的占位空音（2683 字节，合法 Ogg Vorbis），让 `gun` 事件能通过 `validateSoundResource` 校验。
3. **补充字幕语言键**（[en_us.json](app://localhost/epitaxy/core/src/main/resources/assets/customgun/lang/en_us.json)、[zh_cn.json](app://localhost/epitaxy/core/src/main/resources/assets/customgun/lang/zh_cn.json)）—— `subtitle.customgun.gun`，避免 IDE 环境下 "Missing subtitle" 报错。
4. **修复 [SoundPlayManager.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/sound/SoundPlayManager.java:266)** —— 两参 `playGunSound(soundLocation, localPlayer)` 之前从未给 `currentSoundInstance` 赋值，导致 `stopCurrentSound()` 一直是空操作。现在给它赋值，让换弹/检视/拉栓这类长音效可以被后续的收枪/开火正确打断。

## 还没对应上的（本次未处理）

- **`target_block_hit` 声音事件**：`ModSounds.TARGET_HIT` 已注册，但既没有 `sounds.json` 条目，也没有任何调用点（标靶命中功能整体未移植）。它会产生一条无害的 `Missing sound for event` 警告。按你的要求「未移植的功能不补 sound」，我没有动它。
- **第三人称 / 服务器广播音效**：`SoundManager.sendSoundToNearby` 被 `PlannedRefactor.ON_SEND_SOUND_MESSAGE = false` 提前返回，处于禁用状态（属于未移植的开火/第三人称逻辑的一部分）。客户端接收端 `_ServerMessageSound.playSound` 已就绪，但服务端不会发。
- **`GunSoundPreload`**：`@Deprecated(forRemoval = true)` 的空壳，无任何引用，未接入预加载流程。
- **小项**：收枪/切枪（draw/putAway）这俩短音（~300ms）没有纳入 `currentSoundInstance` 追踪，因为它们走的是 `playClientSound` 而非两参 `playGunSound`；真正需要打断的长音效（换弹/检视/拉栓）已恢复追踪。若需要严格对齐 TaCZ 的 `playDrawSound`/`playPutAwaySound` 追踪行为，可以再加，但这属于可选项。

## 验证

- 修改的 Java 文件经 IDE 检查无编译错误。
- 三个 JSON 文件均通过 JSON 解析校验。
- 完整构建(`build_project`)因超时未返回，但改动本身只是资源文件 + 一行赋值，风险极低。

`SoundManagerMixin` 里你加的日志我没有动，可以按你说的手动 discard。请检查。

# 

```
现在重新阅读我修改后的SoundPlayManager。

把目前调用playClientSound不含isMainTrackSound重载的，都改成带这个bool的，调用处的bool写在boolean trackEntity, int concurrencyLimit的下一行，例如在LocalShooterDraw：
SoundPlayManager.get().playClientSound(gunDisplayInstance.getGunSound(GunSoundType.PUT_AWAY_SOUND),
                1.0f, 1.0f,
                this.localShooter, false,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(),
                true, SoundConfig.DEFAULT_SOUND_CONCURRENCY_LIMIT.get(),
                true);
如果原先tacz里调用的播放会设置tmpSoundInstance，则cgc也设置为true，否则为false

全部完成后，对不带isMainTrackSound的playClientSound的引用数应该为0
```

# 

I'll convert all `playClientSound` call sites to the new `isMainTrackSound` overload. Let me make all the edits:

LocalShooterDraw was already converted by you. Let me verify the full state — grep for remaining old-overload calls:

All call sites are now converted. Let me verify there are no compile errors in the edited files:

已完成。所有 `playClientSound` 调用点都已改为带 `isMainTrackSound` 的重载，参考 TaCZ 是否设置 `tmpSoundInstance` 来定值。

## 改动明细

**`isMainTrackSound = true`**（TaCZ 原实现会设置 `tmpSoundInstance`）：

|位置|说明|
|---|---|
|[SoundPlayManager.java:285](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/sound/SoundPlayManager.java:285) 两参 `playGunSound`|换弹/检视/拉栓（TaCZ `playReloadSound`/`playInspectSound`/`playBoltSound`）|
|[AnimateGeoItemRenderer.java:392](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/item/AnimateGeoItemRenderer.java:392) `DRAW_SOUND`|TaCZ `playDrawSound`|
|[AnimateGeoItemRenderer.java:409](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/item/AnimateGeoItemRenderer.java:409) `PUT_AWAY_SOUND`|TaCZ `playPutAwaySound`|
|[LocalShooterDraw.java:132](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/entity/shooter/LocalShooterDraw.java:132) `PUT_AWAY_SOUND`|TaCZ `playPutAwaySound`（这处你已改好）|

**`isMainTrackSound = false`**（TaCZ 不设置 `tmpSoundInstance`）：

|位置|说明|
|---|---|
|[SoundPlayManager.java:250](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/sound/SoundPlayManager.java:250) `playAnimationSound`|动画音效|
|[SoundPlayManager.java:264](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/sound/SoundPlayManager.java:264) `playerRefitSound`|装配音效|
|[SoundPlayManager.java:276](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/sound/SoundPlayManager.java:276) 五参 `playGunSound`|射击/消音/空击/命中/击杀/近战/切模式|
|[_ServerMessageSound.java:57](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/network/message/_ServerMessageSound.java:57)|服务器广播音效（TaCZ `playMessageSound`）|

其中两参 `playGunSound` 我同时去掉了原来 `this.mainTrackSound =` 的手动赋值，改为直接传 `true`（因为新重载内部已处理 `if (isMainTrackSound) this.mainTrackSound = soundInstance;`）。

## 验证

- 四个改动文件均经 IDE 检查无编译错误。
- 现在对不带 `isMainTrackSound` 的 8 参重载的调用点引用数为 0：剩余出现的 `playClientSound(` 只有 74/86 行两个重载的定义、79 行 8 参重载内部委托给 9 参的 `false`，以及 `_ServerMessageSound` 里 42/49 行两处注释掉的旧代码（非实际调用）。

一处提醒：`sounds.json` 目前指向 `customgun:sound/0.001` 且字幕键为 `subtitle.customgun.sound`，与我上一轮加的 `empty.ogg`（`sounds/gun/empty.ogg`）和 `subtitle.customgun.gun` 已不一致——这是你的改动，我没动；如果需要我把残留的 `empty.ogg`/字幕键对齐到新命名，告诉我即可。

# 

```
tacz的draw sound被移到了AnimateGeoItemRenderer，并且在Simple Bedrock Model里调用，而不在FirstPersonRenderEvent
cgc移除了对SBM的捆绑，恢复了FirstPersonRender
分析tacz+SBM的调用链路，看现在cgc怎么恢复drawSound
```

# 

分析完成。下面是完整的调用链路和恢复方案。

## 一、TaCZ + SBM 的 draw sound 链路

**关键点**：TaCZ 的 draw sound 已经不在 `LocalPlayerDraw.doDraw`（那里只剩被注释掉的 `drawFuture` 预定逻辑），也不在 `FirstPersonRenderEvent`，而是搬进了渲染器，由 SBM 驱动。

1. [AnimateGeoItemRenderer.java:325-387](app://localhost/epitaxy/local_dc120550-c77f-4707-9bd2-df2dd55c99ca) 实现 SBM 的 `IFPGeoItemRenderer.createAnimationInstance()`，返回一个 `IFPAnimationInstance`。
    - `triggerDraw()`：`tryInit()`（draw 动画）+ `SoundPlayManager.playDrawSound()`（draw 音效），用 `drawn` 标志幂等。
    - `triggerPutAway()`：`tryExit()`（收枪动画）+ `playPutAwaySound()`。
2. SBM 的 [FirstPersonRenderHandler.java](app://localhost/epitaxy/local_dc120550-c77f-4707-9bd2-df2dd55c99ca) 负责驱动：
    - `onClientTick` 检测到物品/变体切换 → `beginSwitch` → `previousInstance.triggerPutAway()`（收枪音效+动画）。
    - `tickAnimation`（渲染 tick，每帧）→ `tickHandAnimation` → `activeInstance.triggerDraw()`（draw 音效+动画，靠 `drawn` 幂等）。

即：**draw 音效 = 渲染 tick 里、与 draw 动画（`tryInit`）捆绑触发**。

## 二、CGC 现状

1. CGC 移除了 SBM 依赖。`AnimateGeoItemRenderer.createAnimationInstance()` 现在返回 `Object`（不再是 `IFPAnimationInstance`），里面的 `triggerDraw()`/`triggerPutAway()`/`getPutAwayDuration()`/`isSameItem()`/`blockOffhandRender()` 全成了没有接口、没有调用方的**死代码**（`@Override` 都注释掉了）。draw 音效逻辑就死在这里。
2. CGC 恢复了 [FirstPersonRender.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/shooter/FirstPersonRender.java)，注册到 `RENDER_HAND_EVENT`（第一人称手部渲染）。它已经做了 draw **动画**：[第 100-102 行](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/shooter/FirstPersonRender.java:100) 在 `needReInit` 时调用 `renderer.tryInit(...)`。
3. 收枪（put-away）音效已恢复：`LocalShooterDraw._doPutAway` 里 `tryExit` + play put-away sound。
4. **draw 音效未恢复**：`_doDraw` 的预定逻辑注释掉了，`triggerDraw` 是孤儿死代码。

## 三、恢复方案

把死代码 `triggerDraw` 里的音效逻辑，搬到 `FirstPersonRender.onRenderHand` 的 `tryInit` 旁边——这正好对应 TaCZ 的 `tickHandAnimation → triggerDraw`：

```java
if (!iGun.switchItemNeedReset(player.getMainHandItem(), gunItem) && renderer.needReInit(gunItem)) {
    renderer.tryInit(gunItem, player, event.getPartialTick());

    @Nullable GunDisplayInstance display = ClientResourceApi.getGunDisplayInstance(gunItem);
    if (display != null) {
        SoundPlayManager.get().stopMainTrackSound();
        SoundPlayManager.get().playClientSound(display.getGunSound(GunSoundType.DRAW_SOUND),
                1.0f, 1.0f, player, false,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(),
                true, SoundConfig.DEFAULT_SOUND_CONCURRENCY_LIMIT.get(),
                true);
    }
}
```

幂等性已经由 `needReInit` 保证：`tryInit` 后状态机 `isInitialized()==true`，下一帧 `needReInit` 返回 false，所以 draw 音效每个掏枪周期只播一次，无需再引入 `drawn` 标志。

## 四、需要注意的两点

1. **第三人称**：CGC 把第一人称(`FirstPersonRender`/`RENDER_HAND_EVENT`)和第三人称(`_LocalAnimHandler._tickAnimRender`/`RENDER_FRAME_EVENT`)拆开了，两处都调用 `tryInit`。TaCZ 里 SBM 在渲染 tick 每帧触发、不分人称。如果要对齐「第三人称也播 draw sound」，`_LocalAnimHandler.java:132` 的 `tryInit` 旁边也要补一份同样逻辑；否则只有第一人称有声。
2. **死代码清理**：`AnimateGeoItemRenderer` 里那段返回 `Object` 的 `createAnimationInstance` 以及 `getPutAwayDuration`/`isSameItem`/`blockOffhandRender` 是 SBM 时代的残留，恢复 draw sound 后可以整个删掉，避免和新逻辑重复。

需要我直接按方案在 `FirstPersonRender.onRenderHand`（以及第三人称的 `_LocalAnimHandler`）落地 draw sound 并清掉死代码吗？

# 

```
AnimateGeoItemRenderer里注释掉的死逻辑不要移除

落地draw sound
```

# 

I'll implement the draw sound in both the first-person (`FirstPersonRender`) and third-person (`_LocalAnimHandler`) render paths. Let me make the edits:

Now let me verify there are no compile errors in the modified files:

已完成 draw sound 的落地，两处渲染路径都已补上，死代码未动。

## 改动

**1. 第一人称 [FirstPersonRender.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/renderer/shooter/FirstPersonRender.java:99)**

在 `renderer.tryInit(...)`（draw 动画）之后补上 draw 音效，对齐 `_doPutAway` 的收枪音效写法：

```java
if (!iGun.switchItemNeedReset(player.getMainHandItem(), gunItem) && renderer.needReInit(gunItem)) {
    renderer.tryInit(gunItem, player, event.getPartialTick());

    @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
    if (gunDisplayInstance != null) {
        SoundPlayManager.get().stopMainTrackSound();
        SoundPlayManager.get().playClientSound(gunDisplayInstance.getGunSound(GunSoundType.DRAW_SOUND),
                1.0f, 1.0f, player, false,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(),
                true, SoundConfig.DEFAULT_SOUND_CONCURRENCY_LIMIT.get(),
                true);
    }
}
```

**2. 第三人称 [_LocalAnimHandler.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/entity/shooter/player/_LocalAnimHandler.java:130)**

同样在 `_tickAnimRender` 里的 `tryInit` 之后补上同一段 draw 音效（TaCZ 里 SBM 在渲染 tick 每帧触发 `triggerDraw`，不分人称）。

## 关键点

- **幂等**：靠 `needReInit` 保证——`tryInit` 后状态机 `isInitialized()==true`，下一帧 `needReInit` 返回 false，所以每个掏枪周期只播一次，无需 `drawn` 标志。
- **参数对齐**：`DRAW_SOUND` 走 `isMainTrackSound=true`（对应 TaCZ 的 `playDrawSound` 会设置 `tmpSoundInstance`），与 `_doPutAway` 的 `PUT_AWAY_SOUND` 完全对称；`stopMainTrackSound()` 在播放前打断上一个主音轨音效。
- **两处互斥无重复**：`FirstPersonRender` 只在第一人称（`RENDER_HAND_EVENT`）触发，`_LocalAnimHandler._tickAnimRender` 在第一人称时直接 return，二者不会同时播。
- **未动死代码**：`AnimateGeoItemRenderer` 里那段注释掉的 `createAnimationInstance`/`triggerDraw`/`triggerPutAway` 等按你的要求原样保留。

两个改动文件经 IDE 检查均无编译错误。

# 

```
现在第一人称切枪会正常播放
当第三人称，且在两把枪之间切的时候，第二把枪的抬枪动画的声音没有播放；从非枪械切到枪械时，会播放掏枪声音
检查一下问题并修复。
```

# 

I found the root cause. Let me verify the exact current state of the relevant code sections before editing:

修复完成。

## 根因

第三人称的 `_LocalAnimHandler` 与第一人称的 `FirstPersonRender` 用了**不同的物品来源**：

- **第一人称**（正常）：`FirstPersonRender.onRenderHand` 用的是 `event.getItemStack()`，即 `KeepingItemRenderer` 保留的「正在渲染的物品」。切枪期间这个物品还是旧枪 A，配合 `switchItemNeedReset` 守卫，`tryInit`（掏枪 + draw 音效）会被推迟到收枪过渡结束、真正渲染新枪 B 时才执行。
- **第三人称**（出问题）：`_LocalAnimHandler.onRenderFrame` 用的是 `player.getMainHandItem()`，切枪后立刻就是新枪 B。于是 `tryInit(B)` + draw 音效在收枪过渡刚开始时就触发了（此时画面还显示旧枪 A），导致第二把枪的抬枪音效「没有」在抬枪时播放。

这也解释了为什么「非枪械→枪械」正常：那个场景没有 `_doPutAway`，没有 `keep`，保留物品直接就是新枪 B，所以 draw 音效照常。

## 改动（[`_LocalAnimHandler.java`](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/entity/shooter/player/_LocalAnimHandler.java)）

1. `onRenderFrame` 里把物品来源从 `player.getMainHandItem()` 改成 `KeepingItemRenderer.cgc$getRenderer().cgc$getCurrentItem()`（保留物品），与第一人称路径对齐。
2. `_tickAnimRender` 的 `tryInit` 条件加上 `!iGun.switchItemNeedReset(player.getMainHandItem(), gunItem)` 守卫，与 `FirstPersonRender` 一致，避免过渡期间误触发旧枪的重新初始化。

改动后第三人称的 draw 触发时机与第一人称一致：收枪过渡结束后、真正渲染新枪时才初始化状态机并播放 draw 音效。

文件经 IDE 检查无编译错误。
