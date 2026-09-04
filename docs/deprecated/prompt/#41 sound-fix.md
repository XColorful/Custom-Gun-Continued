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
