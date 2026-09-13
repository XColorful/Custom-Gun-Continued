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

# 
---

```
现在自./docs/deprecated/prompt/#41 sound-fix.md之后，单人游戏是能播放声音了，但是多人游戏里开枪，别的玩家似乎没听到声音，即使离得很近

需要继续检查发包问题并修复。
```

# 

I'll start by reading the referenced sound-fix doc and understanding the current sound/network code.

Now let me look at the sound network path — `SoundManager`, `_ServerMessageSound`, `PlannedRefactor`, and the shoot logic.

Found a likely decode bug already (`writeVarInt` / `readInt` mismatch). Let me verify the send path is reached and check the codec/adapter and sound-type parsing.

Confirmed the codec mismatch vs TaCZ. Now let me check how the adapter handles a decode failure, and confirm the client-side sound path.

Let me check whether this encode/decode mismatch pattern exists elsewhere in the network messages.

Two mismatches found so far. Let me systematically compare encode/decode type sequences across all message classes.

Now let me verify the client-side playback path and `getGunSound`.

Let me confirm the client-side sound lookup works for the `shoot_3p` tag.

Root cause confirmed. The sender writes `distance` as a VarInt but the receiver reads a fixed-width int — the decode throws, Forge's event bus swallows the exception into a log line, and the sound never plays. TaCZ's original uses `writeInt`/`readInt` on both sides.

Now let me audit the rest of the receive path for correctness (arg order, tag constants).

Let me verify the change compiles.

找到了根因，已修复。

## 根因：`S2CMessageResourceSound` 编解码不对称（发包 bug）

[S2CMessageResourceSound.java:33](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/network/message/resource/S2CMessageResourceSound.java:33) 写包用 `writeVarInt(distance)`，而 [第 43 行](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/network/message/resource/S2CMessageResourceSound.java:43) 读包用 `readInt()`。

TaCZ 原版 [`ServerMessageSound`](app://localhost/epitaxy/local_7e61cce8-9c87-4b99-9620-be00b211ec82) 两端都是 `writeInt`/`readInt`——这是移植时只改了写侧。

后果是确定性的：`distance` 通常约 48，`writeVarInt` 只写 **1 字节**，`readInt()` 却要读 **4 字节** → netty 抛 `IndexOutOfBoundsException`。我确认过 `FriendlyByteBuf.readInt()` 直接委托给底层 `ByteBuf.readInt()`（[FriendlyByteBuf.java:1236](app://localhost/epitaxy/core/build/tmp/.cache/expanded/zip_360104c6fc04675d4421fcf7cf7ba91a/net/minecraft/network/FriendlyByteBuf.java:1236)）。

## 为什么表现为"没声音"而不是报错

Forge 的 [`NetworkInstance.handleError`](app://localhost/epitaxy/local_7e61cce8-9c87-4b99-9620-be00b211ec82) 是**空方法**——event bus 把解码时抛出的异常静默吞掉了，既不打印日志也不断连。所以接收方客户端只是安静地什么都没发生。

## 为什么单人游戏正常

发送端在 `ServerLevel` 里，且过滤掉 `sourceEntity` 自己：

- 单人游戏里没有任何"别的玩家"可发，这条发包路径根本没被走过；
- 自己能听见的是本地 `SHOOT_SOUND`（`playShootSound`），走本地资源路径，不经过这个包。

所以这个 bug 只在多人下暴露，且**只要有人开枪，其他人就永远听不到**.

## 修复

一行：`writeVarInt` → `writeInt`，与 TaCZ 对齐。

## 排查时确认没问题的地方

- `PlannedRefactor.ON_SEND_SOUND_MESSAGE = false` 并**没有**禁用发送。`if (PlannedRefactor.ON_SEND_SOUND_MESSAGE) return;` 在 false 时不会返回——#41 文档里"被提前返回、处于禁用状态"那句是误读。这个 flag 的语义是"重构完成后跳过旧逻辑"，翻成 true 才会真的断掉声音，别动它。
- 消息注册方向、`chunkMap.getPlayers` + 距离过滤（与 TaCZ 一致）、`_S2CMessageResourceSound` 里 `getGunDisplayInstance(display, gun)` 的回退参数顺序、`"shoot_3p"`/`"silence_3p"` 与 `GunSoundType` 的映射、`playClientSound` 的 `trackEntity`/`concurrencyLimit`/`relative` 取值——均与 TaCZ 对齐，无需改动。

## 另外发现一个同类隐患（未改动）

[S2CMessageGunLevelUp.java:29](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/network/message/gun/S2CMessageGunLevelUp.java:29) 也是 `writeVarInt(level)` / `readInt()`，TaCZ 两端同样是 `writeInt`/`readInt`。我把所有消息类都扫了一遍，编解码类型序列不匹配的只有它和上面修掉的那个。它目前**没有任何发送方**（等级系统未实现），所以暂无实际影响。要一并修掉吗？

## 验证

- 修改文件经 IDEA 编译通过（只有与本次改动无关的既有 deprecation 警告）。
- 字节层面的推理是确定的，但我没有真实的双客户端环境实跑。请你在多人（LAN 或专用服）里复测：另一名玩家近距离应能听到开枪声，且关闭消音器/装消音器时要分别听到 `shoot_3p` / `silence_3p`。

改动已就绪，未提交。

# 

```
还是没声音，继续排查问题并修复，可以加log测试

另外，message不对称的我已经commit
```

# 

Let me look at the client-side sound resource loading, which is the next link in the chain.

Let me check how the 3P sound distance value is actually computed — this looks like it could be a multiplier, not a distance.

Found the likely real cause — the sound distance is a **multiplier** (default 1.0), not a distance. Let me confirm against the shooter's own-sound call.

The distance value is being used raw. Let me check what it should be, against TaCZ's data model.

Let me check `EntityTrackingSoundInstance` before finalizing.

Found it. Now let me apply the fix plus temporary debug logs.

修复完成并编译通过。先说清一件事：**我上一轮的结论不完整**，抱歉。

## 为什么修了 codec 还是没声音

那个 `writeVarInt`/`readInt` 不对称确实是真的、也确实是 bug，但在当时的代码状态下**根本没有包发出去**——所以修它不会有任何变化。真正卡住的是另一个问题。

## 真正的原因：把"倍率"当成了"距离"

[_FireSoundData.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/resource/data/data/gun/_FireSoundData.java) 里：

```java
private float normalMultiplier = 1.0F;
private float silencedMultiplier = 1.0F;
```

是**倍率**，默认 `1.0`。而 [_DefaultGunFire.java:100](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/gun/attack/_DefaultGunFire.java:100) 直接把它当绝对距离用了，发送时再 `(int)`：

```java
context.soundDistance = fireSoundData.getNormalMultiplier();      // = 1.0f
...
SoundManager.sendSoundToNearby(livingShooter, (int) context.soundDistance, ...)  // = 1
```

两个后果，正好对应你看到的现象：

1. [SoundManager.java:118](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/sound/SoundManager.java:118) 的半径过滤是 `distanceToSqr(...) < distance * distance`，`distance = 1` → **只有 1 格内的玩家会收到包**，实际就是没人收到。这就是"即使离得很近"也没声。
2. 即使发出去了，包里的 `distance = 1`，客户端 [ResourceSoundInstance.java:73](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/sound/ResourceSoundInstance.java:73) 会按 `1 - 距离/1` 算音量 → 听到也是 0。

对照射手自己听到的声音，[LocalShooterShoot.java:355](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/entity/shooter/LocalShooterShoot.java:355) 用的是：

```java
GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get() * gunData.getFireSoundData().getNormalMultiplier()
```

—— 服务端这条路径漏了乘 `GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE`（默认 **64**）。TaCZ 原版的 `sound_distance` 属性也是绝对距离，不是倍率。

## 改动

**[_DefaultGunFire.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/gun/attack/_DefaultGunFire.java:98)** —— 让 `soundDistance` 真正是"格数"：

```java
int defaultDistance = GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get();
context.soundDistance = defaultDistance * fireSoundData.getNormalMultiplier();   // 普通
case SILENCED -> context.soundDistance = defaultDistance * fireSoundData.getSilencedMultiplier();
case MUTED -> context.soundDistance = 0;                                         // 仍然不发包
```

发送处 `(int) context.soundDistance` 不用动。两个 bug 是**缺一不可**的关系：之前包发不出去（掩盖了 codec bug），现在包真的飞了，codec 那处修复才真正起作用。

## 临时日志（4 处，都标了 `TODO(debug)`，可随时 discard）

|位置|输出|
|---|---|
|[_DefaultGunFire.java:236](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/gun/attack/_DefaultGunFire.java:236)|服务端算出来的距离和消音标志|
|[SoundManager.java:120](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/sound/SoundManager.java:120)|距离、**实际发给了几个玩家**|
|[S2CMessageResourceSound.java:44](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/network/message/resource/S2CMessageResourceSound.java:44)|接收端解码出的字段|
|[_S2CMessageResourceSound.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/client/network/message/resource/_S2CMessageResourceSound.java:25)|在哪一步被丢掉，或最终播放的音效路径|

## 请这样测

两名玩家靠近（几格内），一人开枪，然后在**服务端日志**里找：

```
[SOUND-DBG] doGunFire 3P: distance=64.0 silent=false
[SOUND-DBG] sendSoundToNearby shooter=... sound=shoot_3p distance=64 targets=1
```

另一名玩家的**客户端日志**里应该紧跟着：

```
[SOUND-DBG] decode: ... sound=shoot_3p ... distance=64
[SOUND-DBG] client play: Shoot3P -> <gunpack 音效路径> distance=64
```

- `targets=0` → 距离过滤器/区块还没覆盖到，把日志发我；
- `client drop: no sound for Shoot3P in gun ...` → gunpack 这个枪的 display 里没定义 `shoot_3p`（资源包问题，不是代码问题）；
- 装消音器时应该变成 `SILENCE_3P_SOUND` + `silencedMultiplier`。

编译通过（IDEA 单文件编译，无错误）。改动未提交。

# 

```
现在有声音了，日志如下，如果没问题就把日志清了

主机：
[14Sep2026 00:14:12.203] [Server thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] doGunFire 3P: distance=64.0 silent=false
[14Sep2026 00:14:12.205] [Server thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] sendSoundToNearby shooter=1 sound=shoot_3p distance=64 targets=1
[14Sep2026 00:14:13.988] [Server thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] doGunFire 3P: distance=64.0 silent=false
[14Sep2026 00:14:13.988] [Server thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] sendSoundToNearby shooter=1 sound=shoot_3p distance=64 targets=1
[14Sep2026 00:14:18.741] [Server thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] doGunFire 3P: distance=64.0 silent=false
[14Sep2026 00:14:18.741] [Server thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] sendSoundToNearby shooter=1 sound=shoot_3p distance=64 targets=1
[14Sep2026 00:14:19.341] [Server thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] doGunFire 3P: distance=64.0 silent=false
[14Sep2026 00:14:19.341] [Server thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] sendSoundToNearby shooter=1 sound=shoot_3p distance=64 targets=1
[14Sep2026 00:14:19.747] [Server thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] doGunFire 3P: distance=64.0 silent=false
[14Sep2026 00:14:19.747] [Server thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] sendSoundToNearby shooter=1 sound=shoot_3p distance=64 targets=1
[14Sep2026 00:14:49.659] [Server thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] doGunFire 3P: distance=64.0 silent=false
[14Sep2026 00:14:49.659] [Server thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] sendSoundToNearby shooter=3 sound=shoot_3p distance=64 targets=1
[14Sep2026 00:14:49.660] [Netty Local Client IO #0/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] decode: entityId=3 gun=cbrg:slr display=cbrg:slr_display sound=shoot_3p volume=0.8 pitch=0.9938605 distance=64
[14Sep2026 00:14:49.666] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] client play: shoot_3p -> cbrg:slr/slr_shoot distance=64 at (4.5, -60.0, -4.5)
[14Sep2026 00:14:50.256] [Server thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] doGunFire 3P: distance=64.0 silent=false
[14Sep2026 00:14:50.256] [Server thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] sendSoundToNearby shooter=3 sound=shoot_3p distance=64 targets=1
[14Sep2026 00:14:50.256] [Netty Local Client IO #0/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] decode: entityId=3 gun=cbrg:slr display=cbrg:slr_display sound=shoot_3p volume=0.8 pitch=0.94272226 distance=64
[14Sep2026 00:14:50.259] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] client play: shoot_3p -> cbrg:slr/slr_shoot distance=64 at (4.5, -60.0, -4.5)

其他玩家：
[14Sep2026 00:14:12.613] [Netty Client IO #1/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] decode: entityId=1 gun=cbrg:slr display=cbrg:slr_display sound=shoot_3p volume=0.8 pitch=0.99381745 distance=64
[14Sep2026 00:14:12.616] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] client play: shoot_3p -> cbrg:slr/slr_shoot distance=64 at (7.372362832196393, -60.0, -6.311197232410021)
[14Sep2026 00:14:14.400] [Netty Client IO #1/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] decode: entityId=1 gun=cbrg:slr display=cbrg:slr_display sound=shoot_3p volume=0.8 pitch=0.9766456 distance=64
[14Sep2026 00:14:14.407] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] client play: shoot_3p -> cbrg:slr/slr_shoot distance=64 at (6.408703149800386, -60.0, -4.686156909957091)
[14Sep2026 00:14:19.150] [Netty Client IO #1/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] decode: entityId=1 gun=cbrg:slr display=cbrg:slr_display sound=shoot_3p volume=0.8 pitch=0.95351267 distance=64
[14Sep2026 00:14:19.156] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] client play: shoot_3p -> cbrg:slr/slr_shoot distance=64 at (6.280029296875, -60.0, -4.46484375)
[14Sep2026 00:14:19.747] [Netty Client IO #1/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] decode: entityId=1 gun=cbrg:slr display=cbrg:slr_display sound=shoot_3p volume=0.8 pitch=1.0034751 distance=64
[14Sep2026 00:14:19.750] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] client play: shoot_3p -> cbrg:slr/slr_shoot distance=64 at (6.280029296875, -60.0, -4.46484375)
[14Sep2026 00:14:20.154] [Netty Client IO #1/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] decode: entityId=1 gun=cbrg:slr display=cbrg:slr_display sound=shoot_3p volume=0.8 pitch=0.9440898 distance=64
[14Sep2026 00:14:20.161] [Render thread/INFO] [dev.xcolorful.customgun.CustomGun/]: [SOUND-DBG] client play: shoot_3p -> cbrg:slr/slr_shoot distance=64 at (6.280029296875, -60.0, -4.46484375)
```

# 

Logs confirm the whole chain works end to end. Removing the debug logs now.

日志已全部清除，只剩修复本身。

## 最终改动

只有一个文件、4 处行：[_DefaultGunFire.java:98](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/gun/attack/_DefaultGunFire.java:98) 加上 `GunConfig` 的 import，把 `soundDistance` 从"倍率"改成"格数"：

```java
int defaultDistance = GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get();   // 默认 64
context.soundDistance = defaultDistance * fireSoundData.getNormalMultiplier();
case SILENCED -> context.soundDistance = defaultDistance * fireSoundData.getSilencedMultiplier();
case MUTED -> context.soundDistance = 0;
```

其余三个文件（`SoundManager`、`S2CMessageResourceSound`、`_S2CMessageResourceSound`）已还原成和 HEAD 完全一致，没有残留临时日志或顺带重构。IDEA 单文件编译通过，无错误无警告。

## 从日志确认的行为

- 服务端：`distance=64 targets=1` —— 半径过滤和发包都正常。
- 接收端：`decode` → `client play: shoot_3p -> cbrg:slr/slr_shoot distance=64` —— codec 解码正确、gunpack 音效解析成功。
- 顺带一提：这个包里 `shoot_3p` 和 `shoot` 指向同一个文件 `cbrg:slr/slr_shoot`，所以远处听到的枪声和射手自己听到的是同一个音。如果想要第三人称有独立的枪声，那是资源包侧加一个 `shoot_3p` 音频的事，代码这边不用动。
- `silent=true` 的路径这次测试没覆盖到，如果之后装消音器，预期是 `sound=silence_3p` + `distance = 64 × silencedMultiplier`。

改动已就绪，未提交，等你 review。
