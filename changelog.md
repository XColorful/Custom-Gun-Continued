### 0.0.x

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