### 26.10

#### 26.10.34.1
26.3:
- Fixed gun projectile damage cooldown to living entities

#### 26.10.34
- Fixed attachment modifier cache not refreshing when switching items without using the scroll wheel

26.2+:
- Added non-built-in Mixin injection points to fix first-person texture and shadow rendering issues

#### 26.10.33
26.2+:
- Fixed gun projectiles damaging Sulfur Cubes with absorbed blocks

#### 26.10.32
- Add Designated Marksman Rifle (DMR) to gun category
- Add creative tab items using resource location `customguntab:{tab_category}_icon` (e.g. `customguntab:ar_icon`)
- Add `hide_in_game` to gun index, ammo index

Fix:
- Fixed horizontal mirroring when items are rendered as inventory slot textures in item frames

For Developer:
- Recompile any code that uses `GunCategory`

#### 26.10.31
Performance:
- Fixed attachment model being rendered twice

26.2+:
- Fixed gun model text not rendering
- Known issue: Gun model text renders black when shaders are enabled (Issue #84)

#### 26.10.30
- Add shooter config `CreativeModeInfiniteFeed`
- Add sync config `EnableSprintWhileShooting`, `EnableSprintWhileAiming`, `EnableSprintWhileReloading`
- Enable sprint while shooting by default
- Fixed terrain reducing scope division brightness

1.21.6+:
- Fixed automatic opacity filling for gun model text

26.2+:
- Fixed laser not being culled inside the scope
- Fixed scope division not illuminated

For Developer:
- Implement `ResourcePojo#copy` and replace manually copying with it

#### 26.10.29
1.21.4+:
- Fixed `bypass_bullet` tag not work

#### 26.10.28.2
If the game starts normally, it is not affected:
- Fixed a `NullPointerException` in CGC addon mods triggered by Forge's parallel loading mechanism

For Developers:
- Use `CustomGun#getXXX` directly without manually passing the platform compatibility layer interface

#### 26.10.28.1
For Developer:
- Add `INetworkCacheManager`
- Replace `IMcLogicalSide` with `NetworkContext.direction()` in network messages

#### 26.10.28
Mechanics:
- Gun projectile can only break vanilla glass blocks by default
- Gun projectile will pass through leaves, chains, iron bars, ladder, mangrove roots and scaffolding by default
- Gun projectile cannot damage item frame, painting by default
- Hide guns on the local player's back in first-person view
- Client key bindings now support modifier keys
- Add zoom config `EnableScrollingZoom`, `ScrollingZoomCycle`, `InvertScrollingZoom`
- Enable mouse wheel zoom by default
- Add sync config `EnablePronePitch`, `MaxPronePitch`, `MinPronePitch`

Optimize:
- Remove unnecessary shooter tick overhead for non-player entities

Fix:
- Fixed a memory leak caused by gun entities when using certain shaders
- Fixed scope-in speed affected by frame rate
- Fixed right-click interaction while holding a gun

Resource file:
- Add block tag `glass_block.json`
- Add `bypass_bullet`, `raytrace` tags

Network:
- Update network protocol version to `28`, which prevents connections between new and older versions

For developer:
- Update `ILivingShooter#zoom`
- `InputKey` no longer requires manual modifier key checks, as `InputKeyManager` handles them automatically
- Align the semantics of the server-side GunFireEvent with those of the client-side event

#### 26.10.27
- Disable gun HUD triggers in spectator mode
- Fixed third-person gun rendering when left hand is set to main hand
- Fixed FOV jitter caused by mounting while sprinting holding non-gun items
- Fix being unable to shoot after local player rebuild
- Fix camera tilt not resetting when switching to third person view
- Add render config `GunDescriptionWidth`
- Reduce default gun description width in item tooltip
- Remove built-in BCEL library

For developer:
- Update `IMessage` interface

#### 26.10.26

Add built-in PlayerRevive compatibility:
- Disable shoot, melee, reload, switch fire mode while bleeding

1.20.4neoforge:
- Fixed crash when player respawns

26.3neoforge:
- Update to NeoForge 26.3.0.83

#### 26.10.25
- Fixed gun model not rendering text
- Add built-in placeholder key `gun_ammo`, `local_shooter_name`
- Add legacy placeholder key `ammo_count`, `player_name`

26.3neoforge:
- Update to NeoForge 26.3.0.58-beta
- Fixed default input key binding issue

#### 26.10.24
- Merge mod tags from data packs as vanilla does
- Fix potential RCE vulnerability in attachment data #65

Network:
- Update network protocol version to `24`, which prevents connections between new and older versions

#### 26.10.23
- Fixed gun projectile not dealing damage to entities
- Deprecated built-in PlayerAnimator resource manager (unused)

### 26.9

#### 26.9.22
- Fixed potential crashes caused by configurations
- Fixed list-type configuration items not being saved
- Fixed 5.7° rotation offset on first-person arm
- Fixed first-person arm rendered while invisible
- Add render config `RenderFirstPersonInvisibleArm`

1.20.1-1.21.1:
- Fixed shooter's sleeves not rendering in first-person view

1.21.1forge:
- Fixed crash when modifying configuration

1.21.11+:
- Fixed crash when rendering laser beam

#### 26.9.21
- Fall back to the attachment category defined in attachment index when item NBT lacks attachment category

#### 26.9.20
26.2+:
- Fixed Shoulder Surfing Reloaded compatibility issue

#### 26.9.19.1
26.3neoforge:
- Update to NeoForge 26.3.0.16-beta

#### 26.9.19
- First official release
- Add gun display default animation backward compatibility

1.21.10+:
- Fix survival mode reload issue

### 0.0.x

#### 0.0.18
- Add inventory feed type
- Remake dummy ammo mechanism
- Temporary add legacy ammo explosion
- Limit minimum gun projectile pierce count to 1
- Add gun config `bypassGunFireConsumption`

Client:
- Display dummy ammo in gun HUD
- Force display reserve ammo in gun HUD for inventory feed type
- Add bullet hole particles

#### 0.0.17
- Add built-in third-person living shooter animation
- Fix pitch clamp when prone
- Fix view bobbing affecting recoil
- Add backward compatibility for misconfigured attachment display `scopeViewIndex`

Mod compatibility:
- Apply recoil to Shoulder Surfing
- Apply built-in crosshair to Shoulder Surfing

Network:
- Update network protocol version to `17`, which prevents connections between new and older versions

#### 0.0.16
- Remove several configurations
- Adjust default configuration values
- Add sync config `hideEntityHitbox`
- Fix render config `GunLodRenderDistance` issue

1.20.2+neoforge:
- Fix ammo stacking and projectile issues caused by Mixin configuration

1.21.1+:
- Fix gun model and gun projectile rendering offset when firing while aiming down sights

1.21.10+:
- Correct rendering position of guns on back

26.2neoforge:
- Update to NeoForge 26.2.0.88

#### 0.0.15

- Fix ammo stack size in dedicated server

1.21.1+
- Fix built-in interact key filter not work

#### 0.0.14
- Add vanilla damage type tags
- Fix distance issue with gun sounds from other players

1.21.1+
- Fix gun projectile render offset in first person view

#### 0.0.13
- Increase gun projectile max render distance to 20 chunks (320 blocks)

Fix:
- Fix reloaded ammo count when installed extended magazine
- Fix resource pack script infinite bolt-cycling issue
- Fix resource pack script recursive shoot issue
- Fix gun projectile not rendering

Back compatibility:
- Adjust damage format for split bullets in `GunData`

#### 0.0.12
- Add built-in gun HUD
- Add built-in shooter operation HUD
- Add built-in interact key filter

Network:
- Update network protocol version to `12`, which prevents connections between new and older versions

Fix:
- Prevent accidental firing when clicking to close the screen
- Fix inability to shoot after switching guns while firing

#### 0.0.11
- Add state lock logs for local gun operations

1.20.2+
- Fix Mixin injection issue and inability to use /tp on 1.21.4+

1.21.1+neoforge
- Fix configuration changes not being saved

#### 0.0.10
- Add gun reload, shoot, overheat mechanisms
- Add attachment recoil modifier
- Calculate gun projectile spawn pos with shooter latency
- Add ammo item max stack size
- Fix resource pack sounds not played
- Fix initial shot timestamp offset issue
- Optimize entity hitbox history storage

#### 0.0.9
- Fix connection disconnects caused by recipe serialization when joining dedicated servers
- Fix the mod's data pack recipe not loaded

Back compatibility:
- Fix data pack script API `getAttachment` not return attachment's resource location
- Add `isAiming`, `isOverHeat` to resource pack script API
- Add animation state `INPUT_FIRE_SELECT` to resource pack script

#### 0.0.8
- Press refit key to close gun refit screen
- Add built-in crosshair with vanilla render style
- Add render config `ReplaceVanillaCrosshair`
- Hide built-in crosshair on gun refit screen
- Add assets script location backwork compatibility
- Add `AttachmentDisplay` scopeViewIndex backwork compatibility
- Add assets texture resource location backwork compatibility
- Sync attachment modifier data

A gun mod must render the guns:
- Render gun in first person & third person view
- Render gun, attachment, ammo item in GUI

Fix:
- Correct `GunDisplay` ammo particle validation
- Fix `AttachmentDisplay` enableScope, enableSight not read
- Gun no longer flickering out for 1-2 frames in the gun refit screen

1.21.1+:
- Fix resource pack loading failure on startup

#### 0.0.7
- Add render config `DisableGunTilting`

Resource pack:
- Add animation state machine script backwork compatibility

#### 0.0.6
- Add tooltips for gun, attachment, ammo, ammo box item
- Add search bar to creative tab, thereby hiding vanilla tab category in tooltips
- Add `GunDisplay` backward compatibility check
- Fix `AttachmentIndex` cache validation

Add gun refit screen:
- Add bigger turn-page button
- Expand inventory attachment slots to 9
- Align inventory attachment slots with attachment category slot
- Attachment lock no longer prevent opening the gun refit screen
- Add attachment lock texture

Adjust:
- Invert gun tooltip mask
- Remove default barrel ammo from gun items in creative tab
- Move item resource location to tooltip detail info part
- Asset license updated to `CC-BY-SA-4.0`

#### 0.0.5
- Add mod logo
- High zoom scope no longer reduce recoil
- Rename render config `FirstPersonBulletTracerEnable` to `EnableFirstPersonBulletTracer`
- Fix crosshair type resource location

1.20.x:
- Fix client not receiving item's full NBT

#### 0.0.4
- Rename package xiao.customgun to dev.xcolorful.customgun

#### 0.0.3
- Remove client-side config key

#### 0.0.2
Living Shooter:
- Add Living Shooter aspects (bolt, melee, aim, prone, etc.)
- Add shooter gun modifier
- Add shooter latency compensation
- Auto reload on player respawn
- Add shoot state determination
- Falling from a one-block height will no longer cancel the prone state

Gun script:
- Add attachment modifiers
- Add runtime gun script execution

Client:
- Add client-side key input functions
- Display resource location in advanced tooltip
- Sync server configurations

#### 0.0.1
- Public alpha release
- Documentation synchronized with GitHub Wiki