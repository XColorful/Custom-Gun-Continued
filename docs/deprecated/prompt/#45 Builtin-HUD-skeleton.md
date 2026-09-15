> #45 提示词

# 

```
内置的\resources\data\customgun\tags\blocks\interact_key\whitelist.json在dev.xcolorful.customgun.client.input.player.InteractKey的判定流程，1.20.4正常读取（持枪时朝着木门和按钮会渲染提示）
但是在1.21.1neoforge（git暂存区有log），InteractFilterData执行到[CGC-DEBUG] 4 default，而whitelist.json里的标签和非标签我都有在游戏内/execute if block ~ ~ ~ #minecraft:开头测试过都还存在

是否是需要把blocks目录改成block等方式？

当前IDEA打开了1.20.4，1.21.1forge/neoforge源码供浏览，目前怀疑仅1.21.1+有这个问题（因为1.20.4正常），所作的修改写在1.21.1forge的窗口里
```

# 

I'll investigate the tag loading path in the 1.21.1forge branch and compare against 1.20.4.

Now let me verify the actual 1.21.1 tag directory names against the vanilla jar rather than trusting memory.

Confirmed from the vanilla 1.21.1 jar: the directories are `tags/block/` and `tags/entity_type/` (singular). Let me verify 1.20.4 for contrast.

Both confirmed. Now let me find every affected tag path across the 1.21.1 worktrees.

No Java code references those folder names — tag resolution is pure vanilla DatapackPath. Let me also rule out a second failure mode: an entry that no longer exists in 1.21.1 would make the *whole* tag file fail to load.

Block tag entries all exist in 1.21.1. Now checking the entity IDs (a bad entry would break that file too).

All entries are valid — the only defect is the directory name. Before renaming, I need to understand how this multi-version repo shares `core` resources, since 1.20.4 has the same path.

All 1.21+/26.x branches share the same wrong path. Let me check the wiki docs copy and pack.mcmeta before editing.

Root cause is confirmed. Let me make the fix in this branch.

Working tree is clean. Applying the rename.

`git mv` won't create the destination directory. Creating it first.

Both renames are staged. Now let me verify the resource actually lands at the new path in the build output.

Both files land at the new path. Let me confirm they make it into the built jar.

The jar task failed on Java toolchain resolution in `--offline` mode (those listed jars are stale artifacts from earlier builds). Retrying with network access.

Root cause confirmed and the fix is applied. Here's what I found.

## Root cause: 1.21 renamed the datapack tag directories

Minecraft 1.21 singularized the tag folder names to match their registry names. I verified this against the actual vanilla jars rather than relying on memory:

| | block | entity |
|---|---|---|
| 1.20.4 `client.jar` | `data/minecraft/tags/`**`blocks`**`/` | `data/minecraft/tags/`**`entity_types`**`/` |
| 1.21.1 `client.jar` | `data/minecraft/tags/`**`block`**`/` | `data/minecraft/tags/`**`entity_type`**`/` |

The mod shipped the 1.20-era layout. In 1.21.1 `data/customgun/tags/blocks/` is never traversed, so `customgun:interact_key/whitelist` in `Registries.BLOCK` resolves to an **empty tag**. That's exactly your symptom: `blockState.is(whitelist)` at [InteractFilterData.java:75](core/src/main/java/dev/xcolorful/customgun/client/config/sync/InteractFilterData.java:75) is always false, so it falls through to `DEFAULT_RESULT` (`false`) at line 77 — the `default` line in your log. The entity path at line 86 is broken the same way.

So yes — **renaming `blocks` → `block` is the correct fix.**

This is not a code bug: [InteractKeyType.java:25](core/src/main/java/dev/xcolorful/customgun/core/api/resource/data/tag/InteractKeyType.java:25) only builds a `TagKey`; vanilla derives the directory from the registry key. I also checked for a second failure mode — a single dead entry makes the *whole* tag file fail to load — by validating every entry against the 1.21.1 jar: all 20 `#minecraft:` block tags exist, and all 22 entity IDs (`boat`, `chest_boat`, `interaction`, …) still exist. Nothing else in the JSON is wrong.

## Applied in 1.21.1forge (staged, not committed)

```
R  data/customgun/tags/blocks/interact_key/whitelist.json -> tags/block/interact_key/whitelist.json
R  data/customgun/tags/entity_types/interact_key/whitelist.json -> tags/entity_type/interact_key/whitelist.json
```

`:core:processResources` now emits `core/build/resources/main/data/customgun/tags/block/interact_key/whitelist.json` — verified.

**What I could not verify:** the full jar. `:core:jar --offline` failed on Java 21 toolchain resolution (there's an interrupted JDK 21 download in `~/.gradle/jdks` — a `.part` plus stale `.lock` files). Unrelated to this change, but it means I have not run the game. The in-game check on neoforge is yours.

## Two things needing your call

1. **Scope.** Every 1.21+/26.x branch has the identical wrong path — `1.21.1neoforge`, `1.21.4neoforge`, `1.21.6neoforge`, `1.21.10neoforge`, `1.21.11neoforge`, `26.1.x/26.1.2/26.2neoforge`. The 1.20.x branches and `core1.20.1` must **keep** the plural form. Since `core` is merged *from* `core1.20.1` into the version branches, this rename is a deliberate version divergence in a shared module — your next merge from `core1.20.1` may try to re-add `tags/blocks`.

2. **`docs/wiki/resource/data/tags/blocks/interact_key/`** mirrors the datapack path in the wiki routing. I left it alone — the page content never names the folder, so it's cosmetic, but if your wiki route is meant to match the datapack layout it's now misleading for 1.21+.

Also worth knowing for later: 1.21 made the same plural→singular change to `items`→`item`, `fluids`→`fluid`, `game_events`→`game_event`, and `functions`→`function`. The mod has no such dirs today, so nothing is broken, but it will bite the same way if any get added.
